/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static com.yahoo.memory4.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.BOOLEAN_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.BYTE_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory4.UnsafeUtil.SHORT_SHIFT;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Lee Rhodes
 */
public abstract class WritableMemory extends Memory {

  //BYTE BUFFER

  /**
   * Accesses the given ByteBuffer for write operations.
   * @param byteBuf the given ByteBuffer
   * @return the given ByteBuffer for write operations.
   */
  public static WritableMemory wrap(final ByteBuffer byteBuf) {
    if (byteBuf.isReadOnly()) {
      throw new ReadOnlyMemoryException("ByteBuffer is read-only.");
    }
    if (byteBuf.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException(
          "Memory does not support " + (byteBuf.order().toString()));
    }
    final MemoryState state = new MemoryState();
    state.putByteBuffer(byteBuf);
    return AccessByteBuffer.wrap(state);
  }

  //MAP
  /**
   * Allocates direct memory used to memory map files for write operations
   * (including those &gt; 2GB).
   * @param file the given file to map
   * @return WritableResourceHandler for managing this map
   * @throws Exception file not found or RuntimeException, etc.
   */
  public static WritableResourceHandler map(final File file) throws Exception {
    return map(file, 0, file.length());
  }

  /**
   * Allocates direct memory used to memory map files for write operations
   * (including those &gt; 2GB).
   * @param file the given file to map
   * @param fileOffset the position in the given file
   * @param capacity the size of the allocated direct memory
   * @return WritableResourceHandler for managing this map
   * @throws Exception file not found or RuntimeException, etc.
   */
  public static WritableResourceHandler map(final File file, final long fileOffset,
      final long capacity) throws Exception {
    final MemoryState state = new MemoryState();
    state.putFile(file);
    state.putFileOffset(fileOffset);
    state.putCapacity(capacity);
    return AllocateDirectWritableMap.map(state);
  }

  //ALLOCATE DIRECT

  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the WritableMemory API.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param capacityBytes the size of the desired memory in bytes
   * @return WritableResourceHandler for managing this off-heap resource
   */
  public static WritableResourceHandler allocateDirect(final long capacityBytes) {
    return allocateDirect(capacityBytes, null);
  }

  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the WritableMemory API.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param capacityBytes the size of the desired memory in bytes
   * @param memReq optional callback
   * @return WritableResourceHandler for managing this off-heap resource
   */
  public static WritableResourceHandler allocateDirect(final long capacityBytes,
      final MemoryRequest memReq) {
    final MemoryState state = new MemoryState();
    state.putCapacity(capacityBytes);
    state.putMemoryRequest(memReq);
    return (WritableResourceHandler) AllocateDirect.allocDirect(state);
  }

  //REGIONS
  /**
   * Returns a writable region of this WritableMemory
   * @param offsetBytes the starting offset with respect to this WritableMemory
   * @param capacityBytes the capacity of the region in bytes
   * @return a writable region of this WritableMemory
   */
  public abstract WritableMemory writableRegion(long offsetBytes, long capacityBytes);

  /**
   * Returns a read-only version of this memory
   * @return a read-only version of this memory
   */
  public abstract Memory asReadOnly();

  //ALLOCATE HEAP VIA AUTOMATIC BYTE ARRAY
  /**
   * Creates on-heap WritableMemory with the given capacity
   * @param capacityBytes the given capacity in bytes
   * @return WritableMemory for write operations
   */
  public static WritableMemory allocate(final int capacityBytes) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(new byte[capacityBytes]);
    state.putUnsafeObjectHeader(ARRAY_BYTE_BASE_OFFSET);
    state.putCapacity(capacityBytes);
    return new WritableMemoryImpl(state);
  }

  //ACCESS PRIMITIVE HEAP ARRAYS for write

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final boolean[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_BOOLEAN_BASE_OFFSET);
    state.putCapacity(arr.length << BOOLEAN_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final byte[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_BYTE_BASE_OFFSET);
    state.putCapacity(arr.length << BYTE_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final char[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_CHAR_BASE_OFFSET);
    state.putCapacity(arr.length << CHAR_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final short[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_SHORT_BASE_OFFSET);
    state.putCapacity(arr.length << SHORT_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final int[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_INT_BASE_OFFSET);
    state.putCapacity(arr.length << INT_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final long[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_LONG_BASE_OFFSET);
    state.putCapacity(arr.length << LONG_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final float[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_FLOAT_BASE_OFFSET);
    state.putCapacity(arr.length << FLOAT_SHIFT);
    return new WritableMemoryImpl(state);
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableMemory for write operations
   */
  public static WritableMemory wrap(final double[] arr) {
    final MemoryState state = new MemoryState();
    state.putUnsafeObject(arr);
    state.putUnsafeObjectHeader(ARRAY_DOUBLE_BASE_OFFSET);
    state.putCapacity(arr.length << DOUBLE_SHIFT);
    return new WritableMemoryImpl(state);
  }
  //END OF CONSTRUCTOR-TYPE METHODS


  //PRIMITIVE putXXX() and putXXXArray() //XXX

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
   * @param length number of array units to transfer
   */
  public abstract void putBooleanArray(long offsetBytes, boolean[] srcArray, int srcOffset,
      int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putByteArray(long offsetBytes, byte[] srcArray, int srcOffset,
      int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putCharArray(long offsetBytes, char[] srcArray, int srcOffset,
      int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putDoubleArray(long offsetBytes, double[] srcArray,
      final int srcOffset, final int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putFloatArray(long offsetBytes, float[] srcArray,
      final int srcOffset, final int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putIntArray(long offsetBytes, int[] srcArray,
      final int srcOffset, final int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putLongArray(long offsetBytes, long[] srcArray,
      final int srcOffset, final int length);

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
   * @param length number of array units to transfer
   */
  public abstract void putShortArray(long offsetBytes, short[] srcArray,
      final int srcOffset, final int length);

  //Atomic Methods //XXX

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

  //OTHER WRITE METHODS //XXX

  /**
   * Returns the primitive backing array, otherwise null.
   * @return the primitive backing array, otherwise null.
   */
  public abstract Object getArray();

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


  //OTHER //XXX

  /**
   * Returns a MemoryRequest or null
   * @return a MemoryRequest or null
   */
  public abstract MemoryRequest getMemoryRequest();

}
