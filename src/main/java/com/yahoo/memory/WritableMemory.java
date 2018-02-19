/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.Util.zeroCheck;
import static com.yahoo.memory.WritableMemoryImpl.ZERO_SIZE_MEMORY;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides read and write primitive and primitive array access to any of the four resources
 * mentioned at the package level.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public abstract class WritableMemory extends Memory {

  //BYTE BUFFER XXX
  /**
   * Accesses the given ByteBuffer for write operations.
   *
   * <p>Note that if the ByteBuffer capacity is zero this will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @param byteBuf the given ByteBuffer
   * @return the given ByteBuffer for write operations.
   */
  public static WritableMemory wrap(final ByteBuffer byteBuf) {
    if (byteBuf.isReadOnly()) {
      throw new ReadOnlyException("ByteBuffer is read-only.");
    }
    return wrapBB(byteBuf);
  }

  static WritableMemory wrapBB(final ByteBuffer byteBuf) {
    if (byteBuf.capacity() == 0) { return ZERO_SIZE_MEMORY; }
    final ResourceState state = new ResourceState();
    state.putByteBuffer(byteBuf);
    AccessByteBuffer.wrap(state);
    final BaseWritableMemoryImpl impl = new WritableMemoryImpl(state);
    return impl;
  }

  //MAP XXX
  /**
   * Allocates direct memory used to memory map files for write operations
   * (including those &gt; 2GB). This assumes that the file was written using native byte ordering.
   * @param file the given file to map
   * @return WritableMemoryMapHandler for managing this map
   * @throws Exception file not found or RuntimeException, etc.
   */
  public static WritableMapHandle writableMap(final File file) throws Exception {
    return writableMap(file, 0, file.length(), ByteOrder.nativeOrder());
  }

  /**
   * Allocates direct memory used to memory map files for write operations
   * (including those &gt; 2GB).
   * @param file the given file to map. It may not be null.
   * @param fileOffsetBytes the position in the given file in bytes. It may not be negative.
   * @param capacityBytes the size of the allocated direct memory. It may not be negative or zero.
   * @param byteOrder the endianness of the given file. It may not be null.
   * @return WritableMemoryMapHandler for managing this map
   * @throws Exception file not found or RuntimeException, etc.
   */
  public static WritableMapHandle writableMap(final File file, final long fileOffsetBytes,
          final long capacityBytes, final ByteOrder byteOrder) throws Exception {
    zeroCheck(capacityBytes, "Capacity");
    final ResourceState state = new ResourceState();
    state.putFile(file);
    state.putFileOffset(fileOffsetBytes);
    state.putCapacity(capacityBytes);
    state.order(byteOrder);
    return WritableMapHandle.map(state);
  }

  //ALLOCATE DIRECT XXX
  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the WritableMemory API. The allocated memory will be 8-byte aligned, but may not
   * be page aligned.
   *
   * <p><b>NOTE:</b> Native/Direct memory acquired using Unsafe may have garbage in it.
   * It is the responsibility of the using class to clear this memory, if required,
   * and to call <i>close()</i> when done.</p>
   *
   * @param capacityBytes the size of the desired memory in bytes.
   * If capacityBytes is zero the WritableDirectHandle get() will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemoryMapHandler for this off-heap resource
   */
  public static WritableDirectHandle allocateDirect(final long capacityBytes) {
    if (capacityBytes == 0) {
      return new WritableDirectHandle(null, ZERO_SIZE_MEMORY);
    }
    final MemoryManager memMgr = DefaultMemoryManager.getInstance();
    return memMgr.allocateDirect(capacityBytes);
  }

  //DUPLICATES & REGIONS XXX
  /**
   * Returns a writable duplicate view of this Memory.
   * @return a writable duplicate view of this Memory
   */
  public abstract WritableMemory writableDuplicate();

  /**
   * Returns a writable region of this WritableMemory
   * @param offsetBytes the starting offset with respect to this WritableMemory
   * @param capacityBytes the capacity of the region in bytes.
   * If capacityBytes is zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return a writable region of this WritableMemory
   */
  public abstract WritableMemory writableRegion(long offsetBytes, long capacityBytes);

  //BUFFER XXX
  /**
   * Convert this WritableMemory to a WritableBuffer.
   * The <i>start</i>, <i>position</i> and <i>end</i> are set to zero, zero, and <i>capacity</i>,
   * respectively.
   * @return WritableBuffer
   */
  public abstract WritableBuffer asWritableBuffer();

  //ALLOCATE HEAP VIA AUTOMATIC BYTE ARRAY XXX
  /**
   * Creates on-heap WritableMemory with the given capacity
   * @param capacityBytes the given capacity in bytes.
   * If capacityBytes is zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory allocate(final int capacityBytes) {
    if (capacityBytes == 0) { return ZERO_SIZE_MEMORY; }
    final byte[] arr = new byte[capacityBytes];
    return new WritableMemoryImpl(new ResourceState(arr, Prim.BYTE, arr.length));
  }

  //ACCESS PRIMITIVE HEAP ARRAYS for write XXX
  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final boolean[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.BOOLEAN, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final byte[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.BYTE, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final char[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.CHAR, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final short[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.SHORT, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final int[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.INT, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final long[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.LONG, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final float[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.FLOAT, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array.
   * If the array is size zero this method will
   * return a WritableMemory backed by a heap byte array of size zero.
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final double[] arr) {
    if (arr.length == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(new ResourceState(arr, Prim.DOUBLE, arr.length));
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putXXX() and putXXXArray() XXX
  /**
   * Puts the boolean value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putBoolean(long offsetBytes, boolean value);

  /**
   * Puts the boolean array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthBooleans number of array units to transfer
   */
  public abstract void putBooleanArray(long offsetBytes, boolean[] srcArray, int srcOffset,
          int lengthBooleans);

  /**
   * Puts the byte value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putByte(long offsetBytes, byte value);

  /**
   * Puts the byte array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthBytes number of array units to transfer
   */
  public abstract void putByteArray(long offsetBytes, byte[] srcArray, int srcOffset,
          int lengthBytes);

  /**
   * Puts the char value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putChar(long offsetBytes, char value);

  /**
   * Puts the char array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthChars number of array units to transfer
   */
  public abstract void putCharArray(long offsetBytes, char[] srcArray, int srcOffset,
          int lengthChars);

  /**
   * Encodes characters from the given CharSequence into UTF-8 bytes and puts them into this
   * <i>WritableMemory</i> begining at the given offsetBytes.
   * This is specifically designed to reduce the production of intermediate objects (garbage),
   * thus significantly reducing pressure on the JVM Garbage Collector.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param src The source CharSequence to be encoded and put into this WritableMemory. It is
   * the responsibility of the caller to provide sufficient capacity in this
   * <i>WritableMemory</i> for the encoded Utf8 bytes. Characters outside the ASCII range can
   * require 2, 3 or 4 bytes per character to encode.
   * @return the number of bytes encoded
   */
  public abstract long putCharsToUtf8(long offsetBytes, CharSequence src);

  /**
   * Puts the double value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putDouble(long offsetBytes, double value);

  /**
   * Puts the double array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  public abstract void putDoubleArray(long offsetBytes, double[] srcArray,
          final int srcOffset, final int lengthDoubles);

  /**
   * Puts the float value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putFloat(long offsetBytes, float value);

  /**
   * Puts the float array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthFloats number of array units to transfer
   */
  public abstract void putFloatArray(long offsetBytes, float[] srcArray,
          final int srcOffset, final int lengthFloats);

  /**
   * Puts the int value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putInt(long offsetBytes, int value);

  /**
   * Puts the int array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthInts number of array units to transfer
   */
  public abstract void putIntArray(long offsetBytes, int[] srcArray,
          final int srcOffset, final int lengthInts);

  /**
   * Puts the long value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putLong(long offsetBytes, long value);

  /**
   * Puts the long array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthLongs number of array units to transfer
   */
  public abstract void putLongArray(long offsetBytes, long[] srcArray,
          final int srcOffset, final int lengthLongs);

  /**
   * Puts the short value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putShort(long offsetBytes, short value);

  /**
   * Puts the short array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthShorts number of array units to transfer
   */
  public abstract void putShortArray(long offsetBytes, short[] srcArray,
          final int srcOffset, final int lengthShorts);

  //Atomic Methods XXX
  /**
   * Atomically adds the given value to the long located at offsetBytes.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param delta the amount to add
   * @return the modified value
   */
  public abstract long getAndAddLong(long offsetBytes, long delta);

  /**
   * Atomically sets the current value at the memory location to the given updated value
   * if and only if the current value {@code ==} the expected value.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param expect the expected value
   * @param update the new value
   * @return {@code true} if successful. False return indicates that
   * the current value at the memory location was not equal to the expected value.
   */
  public abstract boolean compareAndSwapLong(long offsetBytes, long expect, long update);

  /**
   * Atomically exchanges the given value with the current value located at offsetBytes.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param newValue new value
   * @return the previous value
   */
  public abstract long getAndSetLong(long offsetBytes, long newValue);

  //OTHER WRITE METHODS XXX
  /**
   * Returns the primitive backing array, otherwise null.
   * @return the primitive backing array, otherwise null.
   */
  public abstract Object getArray();

  /**
   * Returns the backing ByteBuffer if it exists, otherwise returns null.
   * @return the backing ByteBuffer if it exists, otherwise returns null.
   */
  public abstract ByteBuffer getByteBuffer();

  /**
   * Clears all bytes of this Memory to zero
   */
  public abstract void clear();

  /**
   * Clears a portion of this Memory to zero.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param lengthBytes the length in bytes
   */
  public abstract void clear(long offsetBytes, long lengthBytes);

  /**
   * Clears the bits defined by the bitMask
   * @param offsetBytes offset bytes relative to this Memory start.
   * @param bitMask the bits set to one will be cleared
   */
  public abstract void clearBits(long offsetBytes, byte bitMask);

  /**
   * Fills all bytes of this Memory region to the given byte value.
   * @param value the given byte value
   */
  public abstract void fill(byte value);

  /**
   * Fills a portion of this Memory region to the given byte value.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param lengthBytes the length in bytes
   * @param value the given byte value
   */
  public abstract void fill(long offsetBytes, long lengthBytes, byte value);

  /**
   * Sets the bits defined by the bitMask
   * @param offsetBytes offset bytes relative to this Memory start
   * @param bitMask the bits set to one will be set
   */
  public abstract void setBits(long offsetBytes, byte bitMask);

  //OTHER XXX
  /**
   * Returns a MemoryRequest or null
   * @return a MemoryRequest or null
   */
  public abstract MemoryRequestServer getMemoryRequestServer();

  /**
   * Returns the offset of the start of this WritableMemory from the backing resource
   * including the given offsetBytes, but not including any Java object header.
   *
   * @param offsetBytes the given offset in bytes
   * @return the offset of the start of this WritableMemory from the backing resource.
   */
  public abstract long getRegionOffset(long offsetBytes);

  /**
   * Sets a MemoryRequest for this WritableMemory
   * @param memReqSvr the given MemoryRequest
   */
  public abstract void setMemoryRequest(MemoryRequestServer memReqSvr);

  public abstract WritableDirectHandle getHandle();

  public abstract void setHandle(WritableDirectHandle handle);

}
