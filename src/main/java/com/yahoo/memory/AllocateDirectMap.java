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

  final long capacityBytes; //in

  final Cleaner cleaner;
  private final Deallocator deallocator;
  final RandomAccessFile raf;
  final long nativeBaseOffset;
  final boolean resourceReadOnly;

  //called from map below and from AllocateDirectWritableMap constructor
  AllocateDirectMap(final File file, final long fileOffset, final long capacityBytes) {
    this.capacityBytes = capacityBytes;
    resourceReadOnly = isFileReadOnly(file);
    raf = mapper(file, fileOffset, capacityBytes, resourceReadOnly);
    nativeBaseOffset = map(raf.getChannel(), resourceReadOnly, fileOffset, capacityBytes);
    deallocator = new Deallocator(nativeBaseOffset, capacityBytes, raf);
    cleaner = Cleaner.create(this, deallocator);
    ResourceState.currentDirectMemoryMapAllocations_.incrementAndGet();
    ResourceState.currentDirectMemoryMapAllocated_.addAndGet(capacityBytes);
  }

  /**
   * Factory method for memory mapping a file for read-only access.
   *
   * <p>Memory maps a file directly in off heap leveraging native map0 method used in
   * FileChannelImpl.c. (see reference at top of class)
   * The owner will have read access to that address space.</p>
   *
   * @param file the file to map to native memory
   * @param fileOffset the starting byte offset into the file
   * @param capacityBytes the capacity of the memory mapped region
   * @return A new AllocateDirectMap
   */
  static AllocateDirectMap map(final File file, final long fileOffset, final long capacityBytes)  {
    return new AllocateDirectMap(file, fileOffset, capacityBytes);
  }

  @Override
  public void load() {
    madvise();
    // Read a byte from each page to bring it into memory.
    final int ps = NioBits.pageSize();
    final int count = NioBits.pageCount(capacityBytes);
    long offset = nativeBaseOffset;
    for (int i = 0; i < count; i++) {
      unsafe.getByte(offset);
      offset += ps;
    }
  }

  @Override
  public boolean isLoaded() {
    try {
      final int pageCount = NioBits.pageCount(capacityBytes);
      return (boolean) MAPPED_BYTE_BUFFER_ISLOADED0_METHOD //isLoaded0 is effectively static
          .invoke(AccessByteBuffer.ZERO_DIRECT_BUFFER,     // so this is not modified
              nativeBaseOffset,
              capacityBytes,
              pageCount);
    } catch (final Exception e) {
      throw new RuntimeException(
              String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  @Override
  public void close() {
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
      MAPPED_BYTE_BUFFER_LOAD0_METHOD                //load0 is effectively static
        .invoke(AccessByteBuffer.ZERO_DIRECT_BUFFER, // so this is not modified
            nativeBaseOffset,
            capacityBytes);
    } catch (final Exception e) {
      throw new RuntimeException(
          String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  //Does the actual mapping work, resourceReadOnly must already be set
  static final RandomAccessFile mapper(final File file, final long fileOffset,
      final long capacityBytes, final boolean resourceReadOnly)  {

    final String mode = resourceReadOnly ? "r" : "rw";
    final RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, mode);
      if ((fileOffset + capacityBytes) > raf.length()) {
        if (resourceReadOnly) {
          throw new IllegalStateException(
              "File is shorter than the region that is requested to be mapped: file length="
             + raf.length() + ", mapping offset=" + fileOffset + ", mapping size=" + capacityBytes);
        } else {
          raf.setLength(fileOffset + capacityBytes);
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
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
  private static final long map(final FileChannel fileChannel, final boolean resourceReadOnly,
      final long position, final long lengthBytes) {
    final int pagePosition = (int) (position % unsafe.pageSize());
    final long mapPosition = position - pagePosition;
    final long mapSize = lengthBytes + pagePosition;
    final int mapMode = resourceReadOnly ? MAP_RO : MAP_RW;
    try {
      final long nativeBaseOffset =
          (long) FILE_CHANNEL_IMPL_MAP0_METHOD.invoke(fileChannel, mapMode, mapPosition, mapSize);
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

  void setStepBoolean(final StepBoolean valid) {
    deallocator.setStepBoolean(valid);
  }

  private static final class Deallocator implements Runnable {
    private final RandomAccessFile myRaf;
    private final FileChannel myFc;
    //This is the only place the actual native offset is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long actualNativeBaseOffset;
    private final long myCapacity;
    private StepBoolean valid;

    private Deallocator(final long nativeBaseOffset, final long capacityBytes,
        final RandomAccessFile raf) {
      myRaf = raf;
      assert (myRaf != null);
      myFc = myRaf.getChannel();
      actualNativeBaseOffset = nativeBaseOffset;
      assert (actualNativeBaseOffset != 0);
      myCapacity = capacityBytes;
      assert (myCapacity != 0);
    }

    void setStepBoolean(final StepBoolean valid) {
      this.valid = valid;
    }

    @Override
    public void run() {
      if (myFc != null) {
        unmap();
      }
      actualNativeBaseOffset = 0L;
      valid.change(); //set invalid
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
