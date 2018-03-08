/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import sun.misc.Cleaner;
import sun.nio.ch.FileChannelImpl;

/**
 * Allocates direct memory used to memory map files for read operations.
 * (including those &gt; 2GB).
 *
 * <p>Reference code for map0:
 * <a href="http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/f940e7a48b72/src/solaris/native/sun/nio/ch/FileChannelImpl.c">
 * FileChannelImpl.c</a></p>
 *
 * <p>Reference code for load0(), isLoaded0(), and force0():
 * <a href="http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/f940e7a48b72/src/solaris/native/java/nio/MappedByteBuffer.c">
 * MappedByteBuffer.c</a></p>
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 * @author Praveenkumar Venkatesan
 */
class AllocateDirectMap implements Map {
  private static final int MAP_RO = 0;
  private static final int MAP_RW = 1;

  final ResourceState state;
  final Cleaner cleaner;

  final RandomAccessFile raf;
  final MappedByteBuffer mbb;

  //called from map below and from AllocateDirectWritableMap constructor
  AllocateDirectMap(final ResourceState state) {
    this.state = state;
    raf = mapper(state);
    mbb = createDummyMbbInstance(state.getNativeBaseOffset());
    cleaner = Cleaner.create(this, new Deallocator(state, raf));
    ResourceState.currentDirectMemoryMapAllocations_.incrementAndGet();
    ResourceState.currentDirectMemoryMapAllocated_.addAndGet(state.getCapacity());
  }

  /**
   * Factory method for memory mapping a file for read-only access.
   *
   * <p>Memory maps a file directly in off heap leveraging native map0 method used in
   * FileChannelImpl.c. (see reference at top of class)
   * The owner will have read access to that address space.</p>
   *
   * @param state the ResourceState that already has the file read-only state set.
   * @return A new AllocateDirectMap
   * @throws IOException file not found or RuntimeException, etc.
   */
  static AllocateDirectMap map(final ResourceState state)  {
    return new AllocateDirectMap(state); //file, fileOffset, cap, BO
  }

  @Override
  public void load() {
    madvise();
    // Read a byte from each page to bring it into memory.
    final int ps = unsafe.pageSize();
    final int count = pageCount(ps, state.getCapacity());
    long nativeBaseOffset = state.getNativeBaseOffset();
    for (int i = 0; i < count; i++) {
      unsafe.getByte(nativeBaseOffset);
      nativeBaseOffset += ps;
    }
  }

  @Override
  public boolean isLoaded() {
    final int ps = unsafe.pageSize();
    try {
      final long capacity = state.getCapacity();
      final int pageCount = pageCount(ps, capacity);
      final Method method =
              MappedByteBuffer.class.getDeclaredMethod("isLoaded0", long.class, long.class, int.class);
      method.setAccessible(true);
      return (boolean) method.invoke(mbb, state.getNativeBaseOffset(),
          capacity, pageCount);
    } catch (final Exception e) {
      throw new RuntimeException(
              String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  @Override
  public void close() {
    if (state.isValid()) {
      ResourceState.currentDirectMemoryMapAllocations_.decrementAndGet();
      ResourceState.currentDirectMemoryMapAllocated_.addAndGet(-state.getCapacity());
    }
    cleaner.clean(); //sets invalid
  }

  // Restricted methods

  //Does the actual mapping work, resourceReadOnly must already be set
  //state enters with file, fileOffset, capacity, RRO. Exits with nativeBaseOffset
  @SuppressWarnings("resource")
  static final RandomAccessFile mapper(final ResourceState state)  {
    final long fileOffset = state.getFileOffset();
    final long capacity = state.getCapacity();
    final File file = state.getFile();
    final boolean readOnlyFile = state.isResourceReadOnly(); //set by map above
    final String mode = readOnlyFile ? "r" : "rw";
    final RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, mode);
      if ((fileOffset + capacity) > raf.length()) {
        if (readOnlyFile) {
          throw new IllegalStateException(
              "File is shorter than the region that is requested to be mapped: file length="
             + raf.length() + ", mapping offset=" + fileOffset + ", mapping size=" + capacity);
        } else {
          raf.setLength(fileOffset + capacity);
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    final FileChannel fc = raf.getChannel();
    final int mapMode = readOnlyFile ? MAP_RO : MAP_RW;
    final long nativeBaseOffset = map(fc, mapMode, fileOffset, capacity);
    state.putNativeBaseOffset(nativeBaseOffset);
    return raf;
  }

  static final int pageCount(final int ps, final long capacity) { //avail for test
    return (int) ( (capacity == 0) ? 0 : ((capacity - 1L) / ps) + 1L);
  }

  //Note: DirectByteBuffer extends MappedByteBuffer, which extends ByteBuffer
  private static final MappedByteBuffer createDummyMbbInstance(final long nativeBaseAddress)
          throws RuntimeException {
    try {
      final Class<?> dbbClazz = Class.forName("java.nio.DirectByteBuffer");
      final Constructor<?> dbbCtor =
          dbbClazz.getDeclaredConstructor(int.class, long.class, FileDescriptor.class, Runnable.class);
      dbbCtor.setAccessible(true);
      //note no Cleaner object is created because the Runnable unmapper is null.
      final MappedByteBuffer mbb = (MappedByteBuffer) dbbCtor.newInstance(0, // some junk capacity
          nativeBaseAddress, null, null); //null FileDescriptor, null Runnable unmapper
      return mbb;
    } catch (final Exception e) {
      throw new RuntimeException(
          "Could not create Dummy MappedByteBuffer instance: " + e.getClass()
          + UnsafeUtil.tryIllegalAccessPermit);
    }
  }

  /**
   * called by load(). Calls the native method load0 in MappedByteBuffer.java, implemented
   * in MappedByteBuffer.c. See reference at top of class.
   */
  void madvise() {
    try {
      final Method method =
          MappedByteBuffer.class.getDeclaredMethod("load0", long.class, long.class);
      method.setAccessible(true);
      method.invoke(mbb, state.getNativeBaseOffset(),
          state.getCapacity());
    } catch (final Exception e) {
      throw new RuntimeException(
          String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  /**
   * Creates a mapping of the FileChannel starting at position and of size length to pages
   * in the OS. This may throw OutOfMemory error if you have exhausted memory.
   * You can try to force garbage collection and re-attempt.
   *
   * <p>map0 is a native method of FileChannelImpl.java implemented in FileChannelImpl.c.
   * See reference at top of class.</p>
   *
   * @param fileChannel the FileChannel
   * @param position the offset in bytes into the FileChannel
   * @param lengthBytes the length in bytes
   * @return the native base offset address
   * @throws RuntimeException Encountered an exception while mapping
   */
  private static final long map(final FileChannel fileChannel, final int mode, final long position,
      final long lengthBytes) throws RuntimeException {
    final int pagePosition = (int) (position % unsafe.pageSize());
    final long mapPosition = position - pagePosition;
    final long mapSize = lengthBytes + pagePosition;

    try {
      final Method method =
          FileChannelImpl.class.getDeclaredMethod("map0", int.class, long.class, long.class);
      method.setAccessible(true);
      final long nativeBaseOffset = (long) method.invoke(fileChannel, mode, mapPosition, mapSize);
      return nativeBaseOffset;
    } catch (final InvocationTargetException e) {
      throw new RuntimeException("Exception while mapping", e.getTargetException());
    } catch (final NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException("Exception while mapping", e);
    }
  }


  static final boolean isFileReadOnly(final File file) {
    if (System.getProperty("os.name").startsWith("Windows")) {
      return !file.canWrite();
    }
    //All Unix-like OSes
    final Path path = Paths.get(file.getAbsolutePath());
    PosixFileAttributes attributes = null;
    try {
      attributes = Files.getFileAttributeView(path, PosixFileAttributeView.class).readAttributes();
    } catch (final IOException e) {
      // File presence is guaranteed. Ignore
      e.printStackTrace();
    }
    if (attributes == null) { return false; }

    // A file is read-only in Linux-derived OSes only when it has 0444 permissions.
    final Set<PosixFilePermission> permissions = attributes.permissions();
    int bits = 0;
    bits |= ((permissions.contains(PosixFilePermission.OWNER_READ))     ? 1 << 8 : 0);
    bits |= ((permissions.contains(PosixFilePermission.OWNER_WRITE))    ? 1 << 7 : 0);
    bits |= ((permissions.contains(PosixFilePermission.OWNER_EXECUTE))  ? 1 << 6 : 0);
    bits |= ((permissions.contains(PosixFilePermission.GROUP_READ))     ? 1 << 5 : 0);
    bits |= ((permissions.contains(PosixFilePermission.GROUP_WRITE))    ? 1 << 4 : 0);
    bits |= ((permissions.contains(PosixFilePermission.GROUP_EXECUTE))  ? 1 << 3 : 0);
    bits |= ((permissions.contains(PosixFilePermission.OTHERS_READ))    ? 1 << 2 : 0);
    bits |= ((permissions.contains(PosixFilePermission.OTHERS_WRITE))   ? 1 << 1 : 0);
    bits |= ((permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) ? 1      : 0);
    // Here we are going to ignore the Owner Write & Execute bits to allow root/owner testing.
    return ((bits & 0477) == 0444);
  }

  private static final class Deallocator implements Runnable {
    private final RandomAccessFile myRaf;
    private final FileChannel myFc;
    //This is the only place the actual native offset is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long actualNativeBaseOffset;
    private final long myCapacity;
    private final ResourceState parentStateRef;

    private Deallocator(final ResourceState state, final RandomAccessFile raf) {
      myRaf = raf;
      assert (myRaf != null);
      myFc = myRaf.getChannel();
      actualNativeBaseOffset = state.getNativeBaseOffset();
      assert (actualNativeBaseOffset != 0);
      myCapacity = state.getCapacity();
      assert (myCapacity != 0);
      parentStateRef = state;
    }

    @Override
    public void run() {
      if (myFc != null) {
        unmap();
      }
      actualNativeBaseOffset = 0L;
      parentStateRef.setInvalid();
    }

    /**
     * Removes existing mapping.  <i>unmap0</i> is a native method in FileChannelImpl.c. See
     * reference at top of class.
     */
    private void unmap() throws RuntimeException {
      try {
        final Method method = FileChannelImpl.class.getDeclaredMethod("unmap0", long.class,
            long.class);
        method.setAccessible(true);
        method.invoke(myFc, actualNativeBaseOffset, myCapacity);
        myRaf.close();
      } catch (final Exception e) {
        throw new RuntimeException(
            String.format("Encountered %s exception while freeing memory", e.getClass()));
      }
    }
  } //End of class Deallocator

}
