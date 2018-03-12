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
 * <p>Reference native code for map0, unmap0:
 * <a href="http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/f940e7a48b72/src/solaris/native/sun/nio/ch/FileChannelImpl.c">
 * FileChannelImpl.c</a></p>
 *
 * <p>Reference native code for load0(), isLoaded0(), and force0():
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

  private static final Method FILE_CHANNEL_IMPL_MAP0_METHOD;
  private static final Method FILE_CHANNEL_IMPL_UNMAP0_METHOD;

  private static final Method MAPPED_BYTE_BUFFER_LOAD0_METHOD;
  private static final Method MAPPED_BYTE_BUFFER_ISLOADED0_METHOD;
  static final Method MAPPED_BYTE_BUFFER_FORCE0_METHOD;

  final ResourceState state;
  final Cleaner cleaner;

  final RandomAccessFile raf;
  final MappedByteBuffer mbb;

  static {
    try {
      FILE_CHANNEL_IMPL_MAP0_METHOD = FileChannelImpl.class
          .getDeclaredMethod("map0", int.class, long.class, long.class);
      FILE_CHANNEL_IMPL_MAP0_METHOD.setAccessible(true);

      FILE_CHANNEL_IMPL_UNMAP0_METHOD = FileChannelImpl.class
          .getDeclaredMethod("unmap0", long.class, long.class);
      FILE_CHANNEL_IMPL_UNMAP0_METHOD.setAccessible(true);

      MAPPED_BYTE_BUFFER_LOAD0_METHOD = MappedByteBuffer.class
          .getDeclaredMethod("load0", long.class, long.class);
      MAPPED_BYTE_BUFFER_LOAD0_METHOD.setAccessible(true);

      MAPPED_BYTE_BUFFER_ISLOADED0_METHOD = MappedByteBuffer.class
          .getDeclaredMethod("isLoaded0", long.class, long.class, int.class);
      MAPPED_BYTE_BUFFER_ISLOADED0_METHOD.setAccessible(true);

      MAPPED_BYTE_BUFFER_FORCE0_METHOD = MappedByteBuffer.class
          .getDeclaredMethod("force0", FileDescriptor.class, long.class, long.class);
      MAPPED_BYTE_BUFFER_FORCE0_METHOD.setAccessible(true);
    } catch (final Exception e) {
      throw new RuntimeException("Could not reflect static methods: " + e);
    }
  }

  //called from map below and from AllocateDirectWritableMap constructor
  AllocateDirectMap(final ResourceState state, final File file, final long fileOffset) {
    this.state = state;
    raf = mapper(state, file, fileOffset);
    //Note: DirectByteBuffer extends MappedByteBuffer, which extends ByteBuffer
    mbb = (MappedByteBuffer) AccessByteBuffer.ZERO_DIRECT_BUFFER;
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
  static AllocateDirectMap map(final ResourceState state, final File file, final long fileOffset)  {
    return new AllocateDirectMap(state, file, fileOffset); //state: RRO, capacity, BO
  }

  @Override
  public void load() {
    madvise();
    // Read a byte from each page to bring it into memory.
    final int ps = NioBits.pageSize();
    final int count = NioBits.pageCount(state.getCapacity());
    long nativeBaseOffset = state.getNativeBaseOffset();
    for (int i = 0; i < count; i++) {
      unsafe.getByte(nativeBaseOffset);
      nativeBaseOffset += ps;
    }
  }

  @Override
  public boolean isLoaded() {
    try {
      final long capacity = state.getCapacity();
      final int pageCount = NioBits.pageCount(capacity);
      return (boolean) MAPPED_BYTE_BUFFER_ISLOADED0_METHOD.invoke(mbb, state.getNativeBaseOffset(),
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
  /**
   * called by load(). Calls the native method load0 in MappedByteBuffer.java, implemented
   * in MappedByteBuffer.c. See reference at top of class. load0 allows setting a mapping length
   * of greater than 2GB.
   */
  void madvise() {
    try {
      MAPPED_BYTE_BUFFER_LOAD0_METHOD.invoke(mbb, state.getNativeBaseOffset(),
          state.getCapacity());
    } catch (final Exception e) {
      throw new RuntimeException(
          String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  //Does the actual mapping work, resourceReadOnly must already be set
  //state enters with capacity, RRO. adds nativeBaseOffset
  @SuppressWarnings("resource")
  static final RandomAccessFile mapper(final ResourceState state, final File file,
      final long fileOffset)  {
    final long capacity = state.getCapacity();
    final boolean readOnlyFile = state.isResourceReadOnly();
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
  private static final long map(final FileChannel fileChannel, final int mode,
      final long position, final long lengthBytes) {
    final int pagePosition = (int) (position % unsafe.pageSize());
    final long mapPosition = position - pagePosition;
    final long mapSize = lengthBytes + pagePosition;

    try {
      final long nativeBaseOffset =
          (long) FILE_CHANNEL_IMPL_MAP0_METHOD.invoke(fileChannel, mode, mapPosition, mapSize);
      return nativeBaseOffset;
    } catch (final InvocationTargetException e) {
      throw new RuntimeException("Exception while mapping", e.getTargetException());
    } catch (final IllegalAccessException e) {
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
        FILE_CHANNEL_IMPL_UNMAP0_METHOD.invoke(myFc, actualNativeBaseOffset, myCapacity);
        myRaf.close();
      } catch (final Exception e) {
        throw new RuntimeException(
            String.format("Encountered %s exception while freeing memory", e.getClass()));
      }
    }
  } //End of class Deallocator

}
