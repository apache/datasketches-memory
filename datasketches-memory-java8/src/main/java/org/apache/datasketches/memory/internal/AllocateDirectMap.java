/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import org.apache.datasketches.memory.Map;
import org.apache.datasketches.memory.MemoryCloseException;

import sun.nio.ch.FileChannelImpl;

/**
 * Allocates direct memory used to memory map files for read operations.
 * (including those &gt; 2GB).
 *
 * <p>To understand how it works, reference native code for map0, unmap0:
 * <a href="http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/f940e7a48b72/src/solaris/native/sun/nio/ch/FileChannelImpl.c">
 * FileChannelImpl.c</a></p>
 *
 * <p>To understand how it works, reference native code for load0(), isLoaded0(), and force0():
 * <a href="http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/f940e7a48b72/src/solaris/native/java/nio/MappedByteBuffer.c">
 * MappedByteBuffer.c</a></p>
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 * @author Praveenkumar Venkatesan
 */
@SuppressWarnings("restriction")
class AllocateDirectMap implements Map {
  static final Logger LOG = Logger.getLogger(AllocateDirectMap.class.getCanonicalName());

  private static final int MAP_RO = 0;
  private static final int MAP_RW = 1;

  private static final Method FILE_CHANNEL_IMPL_MAP0_METHOD;
  static final Method FILE_CHANNEL_IMPL_UNMAP0_METHOD;

  private static final Method MAPPED_BYTE_BUFFER_LOAD0_METHOD;
  private static final Method MAPPED_BYTE_BUFFER_ISLOADED0_METHOD;
  static final Method MAPPED_BYTE_BUFFER_FORCE0_METHOD;

  static {
    try { //The FileChannelImpl methods map0 and unmap0 still exist in 16
      FILE_CHANNEL_IMPL_MAP0_METHOD = FileChannelImpl.class
          .getDeclaredMethod("map0", int.class, long.class, long.class); //JDK14 add boolean.class
      FILE_CHANNEL_IMPL_MAP0_METHOD.setAccessible(true);

      FILE_CHANNEL_IMPL_UNMAP0_METHOD = FileChannelImpl.class
          .getDeclaredMethod("unmap0", long.class, long.class); //OK through jDK16
      FILE_CHANNEL_IMPL_UNMAP0_METHOD.setAccessible(true);


      //The MappedByteBuffer methods load0, isLoaded0 and force0 are removed in 15
      MAPPED_BYTE_BUFFER_LOAD0_METHOD = MappedByteBuffer.class
          .getDeclaredMethod("load0", long.class, long.class); //JDK15 removed
      MAPPED_BYTE_BUFFER_LOAD0_METHOD.setAccessible(true);

      MAPPED_BYTE_BUFFER_ISLOADED0_METHOD = MappedByteBuffer.class
          .getDeclaredMethod("isLoaded0", long.class, long.class, int.class); //JDK15 removed
      MAPPED_BYTE_BUFFER_ISLOADED0_METHOD.setAccessible(true);

      MAPPED_BYTE_BUFFER_FORCE0_METHOD = MappedByteBuffer.class
          .getDeclaredMethod("force0", FileDescriptor.class, long.class, long.class); //JDK15 removed
      MAPPED_BYTE_BUFFER_FORCE0_METHOD.setAccessible(true);
    } catch (final SecurityException | NoSuchMethodException e) {
      throw new RuntimeException("Could not reflect static methods: " + e);
    }
  }

  private final Deallocator deallocator;
  private final MemoryCleaner cleaner;

  final long capacityBytes;
  final RandomAccessFile raf;
  final long nativeBaseOffset;
  final boolean resourceReadOnly;

  //called from AllocateDirectWritableMap constructor
  @SuppressWarnings("resource")
  AllocateDirectMap(final File file, final long fileOffsetBytes, final long capacityBytes,
      final boolean localReadOnly) {
    this.capacityBytes = capacityBytes;
    resourceReadOnly = isFileReadOnly(file);
    final long fileLength = file.length();
    if ((localReadOnly || resourceReadOnly) && fileOffsetBytes + capacityBytes > fileLength) {
      throw new IllegalArgumentException(
          "Read-only mode and requested map length is greater than current file length: "
          + "Requested Length = " + (fileOffsetBytes + capacityBytes)
          + ", Current File Length = " + fileLength);
    }
    raf = mapper(file, fileOffsetBytes, capacityBytes, resourceReadOnly);
    nativeBaseOffset = map(raf.getChannel(), resourceReadOnly, fileOffsetBytes, capacityBytes);
    deallocator = new Deallocator(nativeBaseOffset, capacityBytes, raf);
    cleaner = new MemoryCleaner(this, deallocator);
  }

  //Map Interface

  @Override
  public void load() {
    madvise();
    // Performance optimization. Read a byte from each page to bring it into memory.
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
      return (boolean) MAPPED_BYTE_BUFFER_ISLOADED0_METHOD
          //isLoaded0 is effectively static, so ZERO_READ_ONLY_DIRECT_BYTE_BUFFER is not modified
          .invoke(AccessByteBuffer.ZERO_READ_ONLY_DIRECT_BYTE_BUFFER,
              nativeBaseOffset,
              capacityBytes,
              pageCount);
    } catch (final  IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(
              String.format("Encountered %s exception while loading", e.getClass()));
    }
  }
  // End Map Interface

  @Override
  public void close() {
    doClose("AllocateDirectMap");
  }

  boolean doClose(final String resource) {
    try {
      if (deallocator.deallocate(false)) {
        // This Cleaner.clean() call effectively just removes the Cleaner from the internal linked
        // list of all cleaners. It will delegate to Deallocator.deallocate() which will be a no-op
        // because the valid state is already changed.
        cleaner.clean();
        return true;
      }
      return false;
    } catch (final Exception e) {
        throw new MemoryCloseException(resource);
    } finally {
      BaseStateImpl.reachabilityFence(this);
    }
  }

  StepBoolean getValid() {
    return deallocator.getValid();
  }

  // Private methods
  /**
   * called by load(). Calls the native method load0 in MappedByteBuffer.java, implemented
   * in MappedByteBuffer.c. See reference at top of class. load0 allows setting a mapping length
   * of greater than 2GB.
   */
  private void madvise() {
    try {
      MAPPED_BYTE_BUFFER_LOAD0_METHOD
        //load0 is effectively static, so ZERO_READ_ONLY_DIRECT_BYTE_BUFFER is not modified
        .invoke(AccessByteBuffer.ZERO_READ_ONLY_DIRECT_BYTE_BUFFER,
            nativeBaseOffset,
            capacityBytes);
    } catch (final  IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(
          String.format("Encountered %s exception while loading", e.getClass()));
    }
  }

  //Does the actual mapping work, resourceReadOnly must already be set
  private static RandomAccessFile mapper(final File file, final long fileOffset,
      final long capacityBytes, final boolean resourceReadOnly)  {

    final String mode = resourceReadOnly ? "r" : "rw";
    final RandomAccessFile raf;
    try {
      raf = new RandomAccessFile(file, mode);
      if (fileOffset + capacityBytes > raf.length()) {
        raf.setLength(fileOffset + capacityBytes);
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
  private static long map(final FileChannel fileChannel, final boolean resourceReadOnly,
      final long position, final long lengthBytes) {
    final int pagePosition = (int) (position % unsafe.pageSize());
    final long mapPosition = position - pagePosition;
    final long mapSize = lengthBytes + pagePosition;
    final int mapMode = resourceReadOnly ? MAP_RO : MAP_RW;
    //final boolean isSync = true; //required as of JDK14, but it is more complex
    try {
      final long nativeBaseOffset = //JDK14 add isSync
        (long) FILE_CHANNEL_IMPL_MAP0_METHOD.invoke(fileChannel, mapMode, mapPosition, mapSize);
      return nativeBaseOffset;
    } catch (final InvocationTargetException e) {
      throw new RuntimeException("Exception while mapping", e.getTargetException());
    } catch (final IllegalAccessException e) {
      throw new RuntimeException("Exception while mapping", e);
    }
  }

  public static boolean isFileReadOnly(final File file) {
    return (!file.canWrite());
  }

  private static final class Deallocator implements Runnable {
    private final RandomAccessFile myRaf;
    private final FileChannel myFc;
    //This is the only place the actual native offset is kept for use by unsafe.freeMemory();
    private final long actualNativeBaseOffset;
    private final long myCapacity;
    private final StepBoolean valid = new StepBoolean(true); //only place for this

    Deallocator(final long nativeBaseOffset, final long capacityBytes,
        final RandomAccessFile raf) {
      BaseStateImpl.currentDirectMemoryMapAllocations_.incrementAndGet();
      BaseStateImpl.currentDirectMemoryMapAllocated_.addAndGet(capacityBytes);
      myRaf = raf;
      assert myRaf != null;
      myFc = myRaf.getChannel();
      actualNativeBaseOffset = nativeBaseOffset;
      assert actualNativeBaseOffset != 0;
      myCapacity = capacityBytes;
      assert myCapacity != 0;
    }

    StepBoolean getValid() {
      return valid;
    }

    @Override
    public void run() {
      deallocate(true);
    }

    boolean deallocate(final boolean calledFromCleaner) {
      if (valid.change()) {
        if (calledFromCleaner) {
          // Warn about non-deterministic resource cleanup.
          LOG.warning("A WritableMapHandleImpl was not closed manually");
        }
        try {
          unmap();
        }
        finally {
          BaseStateImpl.currentDirectMemoryMapAllocations_.decrementAndGet();
          BaseStateImpl.currentDirectMemoryMapAllocated_.addAndGet(-myCapacity);
        }
        return true;
      }
      return false;
    }

    /**
     * Removes existing mapping.  <i>unmap0</i> is a native method in FileChannelImpl.c. See
     * reference at top of class.
     */
    private void unmap() throws RuntimeException {
      try {
        FILE_CHANNEL_IMPL_UNMAP0_METHOD.invoke(myFc, actualNativeBaseOffset, myCapacity);
        myRaf.close();
      } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
        throw new RuntimeException(
            String.format("Encountered %s exception while freeing memory", e.getClass()));
      }
    }
  } //End of class Deallocator

}
