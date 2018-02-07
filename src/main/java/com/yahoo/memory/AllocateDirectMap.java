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
 * @author Roman Leventov
 * @author Lee Rhodes
 * @author Praveenkumar Venkatesan
 */
class AllocateDirectMap implements Map {
  final ResourceState state;
  final Cleaner cleaner;

  AllocateDirectMap(final ResourceState state) {
    this.state = state;
    cleaner = Cleaner.create(this, new Deallocator(state));
    ResourceState.currentDirectMemoryMapAllocations_.incrementAndGet();
    ResourceState.currentDirectMemoryMapAllocated_.addAndGet(state.getCapacity());
  }

  /**
   * Factory method for memory mapping a file for read-only access.
   *
   * <p>Memory maps a file directly in off heap leveraging native map0 method used in
   * FileChannelImpl.c. The owner will have read access to that address space.</p>
   *
   * @param state the ResourceState
   * @return A new AllocateDirectMap
   * @throws Exception file not found or RuntimeException, etc.
   */
  static AllocateDirectMap map(final ResourceState state) throws Exception {
    return new AllocateDirectMap(mapper(state));
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
    final long nativeBaseOffset = state.getNativeBaseOffset();
    try {

      final int pageCount = pageCount(ps, state.getCapacity());
      final Method method =
              MappedByteBuffer.class.getDeclaredMethod("isLoaded0", long.class, long.class, int.class);
      method.setAccessible(true);
      return (boolean) method.invoke(state.getMappedByteBuffer(), nativeBaseOffset,
              state.getCapacity(), pageCount);
    } catch (final Exception e) {
      throw new RuntimeException(
              String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  @Override
  public void close() {
    try {
      if (state.isValid()) {
        ResourceState.currentDirectMemoryMapAllocations_.decrementAndGet();
        ResourceState.currentDirectMemoryMapAllocated_.addAndGet(-state.getCapacity());
      }
      cleaner.clean(); //sets invalid
    } catch (final Exception e) {
      throw e;
    }
  }

  // Restricted methods

  //Does the actual mapping work
  @SuppressWarnings("resource")
  static final ResourceState mapper(final ResourceState state) throws Exception {
    final long fileOffset = state.getFileOffset();
    final long capacity = state.getCapacity();

    final File file = state.getFile();

    if (isFileReadOnly(file)) {
      state.setResourceReadOnly(); //The file itself could be writable
    }

    final String mode = "rw"; //we can't map it unless we use rw mode
    final RandomAccessFile raf = new RandomAccessFile(file, mode);
    state.putRandomAccessFile(raf);
    final FileChannel fc = raf.getChannel();
    final long nativeBaseOffset = map(fc, fileOffset, capacity);
    state.putNativeBaseOffset(nativeBaseOffset);

    // length can be set more than the file.length
    raf.setLength(capacity);
    final MappedByteBuffer mbb = createDummyMbbInstance(nativeBaseOffset);
    state.putMappedByteBuffer(mbb);
    return state;
  }

  static final int pageCount(final int ps, final long capacity) {
    return (int) ( (capacity == 0) ? 0 : ((capacity - 1L) / ps) + 1L);
  }

  static final MappedByteBuffer createDummyMbbInstance(final long nativeBaseAddress)
          throws RuntimeException {
    try {
      final Class<?> cl = Class.forName("java.nio.DirectByteBuffer");
      final Constructor<?> ctor =
              cl.getDeclaredConstructor(int.class, long.class, FileDescriptor.class, Runnable.class);
      ctor.setAccessible(true);
      final MappedByteBuffer mbb = (MappedByteBuffer) ctor.newInstance(0, // some junk capacity
              nativeBaseAddress, null, null);
      return mbb;
    } catch (final Exception e) {
      throw new RuntimeException(
              "Could not create Dummy MappedByteBuffer instance: " + e.getClass());
    }
  }

  /**
   * madvise is a system call made by load0 native method
   */
  void madvise() throws RuntimeException {
    try {
      final Method method = MappedByteBuffer.class.getDeclaredMethod("load0", long.class, long.class);
      method.setAccessible(true);
      method.invoke(state.getMappedByteBuffer(), state.getNativeBaseOffset(),
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
   * @param fileChannel the FileChannel
   * @param position the offset in bytes into the FileChannel
   * @param lengthBytes the length in bytes
   * @return the native base offset address
   * @throws RuntimeException Encountered an exception while mapping
   */
  private static final long map(final FileChannel fileChannel, final long position,
      final long lengthBytes) throws RuntimeException {
    final int pagePosition = (int) (position % unsafe.pageSize());
    final long mapPosition = position - pagePosition;
    final long mapSize = lengthBytes + pagePosition;

    try {
      final Method method =
              FileChannelImpl.class.getDeclaredMethod("map0", int.class, long.class, long.class);
      method.setAccessible(true);
      final long nativeBaseOffset = (long) method.invoke(fileChannel, 1, mapPosition, mapSize);
      return nativeBaseOffset;
    } catch (final Exception e) {
      throw new RuntimeException(
              String.format("Encountered %s exception while mapping", e.getClass()));
    }
  }

  private static final class Deallocator implements Runnable {
    private final RandomAccessFile myRaf;
    private final FileChannel myFc;
    //This is the only place the actual native offset is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long actualNativeBaseOffset;
    private final long myCapacity;
    private final ResourceState parentStateRef;

    private Deallocator(final ResourceState state) {
      myRaf = state.getRandomAccessFile();
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
     * Removes existing mapping
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
    //System.out.println(Util.zeroPad(Integer.toBinaryString(bits), 32));
    //System.out.println(Util.zeroPad(Integer.toOctalString(bits), 4));
    // Here we are going to ignore the Owner Write & Execute bits to allow root/owner testing.
    return ((bits & 0477) == 0444);
  }

}
