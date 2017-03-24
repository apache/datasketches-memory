/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides read and write, positional primitive and primitive array access to any of the four
 * resources mentioned at the package level.
 *
 * @author Lee Rhodes
 */
public abstract class WritableBuffer extends Buffer {

  WritableBuffer(final ResourceState state) {
    super(state);
  }

  //BYTE BUFFER XXX
  /**
   * Accesses the given ByteBuffer for write operations.
   * @param byteBuf the given ByteBuffer
   * @return the given ByteBuffer for write operations.
   */
  public static WritableBuffer wrap(final ByteBuffer byteBuf) {
    if (byteBuf.isReadOnly()) {
      throw new ReadOnlyException("ByteBuffer is read-only.");
    }
    if (byteBuf.order() != ByteOrder.nativeOrder()) {
      throw new IllegalArgumentException(
          "Buffer does not support " + (byteBuf.order().toString()));
    }
    final ResourceState state = new ResourceState();
    state.putByteBuffer(byteBuf);
    AccessByteBuffer.wrap(state);
    return new WritableBufferImpl(state);
  }

  //MAP XXX
  //Use WritableMemory for mapping files

  //ALLOCATE DIRECT XXX
  //Use WritableMemory to allocate direct memory

  //REGIONS/DUPLICATES XXX
  /**
   * Returns a writable duplicate view of this Buffer with the same but independent values of
   * low, pos, high and capacity.
   * @return a writable duplicate view of this Buffer with the same but independent values of
   * low, pos, high and capacity.
   */
  public abstract WritableBuffer writableDuplicate();

  /**
   * Returns a writable region of this WritableBuffer
   * @return a writable region of this WritableBuffer
   */
  public abstract WritableBuffer writableRegion();

  /**
   * Returns a writable region of this WritableBuffer
   * @param offsetBytes the starting offset with respect to this WritableBuffer
   * @param capacityBytes the capacity of the region in bytes
   * @return a writable region of this WritableBuffer
   */
  public abstract WritableBuffer writableRegion(long offsetBytes, long capacityBytes);

  //MEMORY XXX
  /**
   * Convert this WritableBuffer to a WritableMemory
   * @return WritableMemory
   */
  public abstract WritableMemory asWritableMemory();

  //ALLOCATE HEAP VIA AUTOMATIC BYTE ARRAY XXX
  /**
   * Creates on-heap WritableBuffer with the given capacity
   * @param capacityBytes the given capacity in bytes
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer allocate(final int capacityBytes) {
    final byte[] arr = new byte[capacityBytes];
    return new WritableBufferImpl(new ResourceState(arr, Prim.BYTE, arr.length));
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //ACCESS PRIMITIVE HEAP ARRAYS for write XXX
  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final boolean[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.BOOLEAN, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final byte[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.BYTE, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final char[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.CHAR, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final short[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.SHORT, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final int[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.INT, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final long[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.LONG, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final float[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.FLOAT, arr.length));
  }

  /**
   * Wraps the given primitive array for write operations
   * @param arr the given primitive array
   * @return WritableBuffer for write operations
   */
  public static WritableBuffer wrap(final double[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.DOUBLE, arr.length));
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putXXX() and putXXXArray() XXX
  /**
   * Puts the boolean value at the current position
   * @param value the value to put
   */
  public abstract void putBoolean(boolean value);

  /**
   * Puts the boolean array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putBooleanArray(boolean[] srcArray, int srcOffset,
      int length);

  /**
   * Puts the byte value at the current position
   * @param value the value to put
   */
  public abstract void putByte(byte value);

  /**
   * Puts the byte array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putByteArray(byte[] srcArray, int srcOffset,
      int length);

  /**
   * Puts the char value at the current position
   * @param value the value to put
   */
  public abstract void putChar(char value);

  /**
   * Puts the char array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putCharArray(char[] srcArray, int srcOffset,
      int length);

  /**
   * Puts the double value at the current position
   * @param value the value to put
   */
  public abstract void putDouble(double value);

  /**
   * Puts the double array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putDoubleArray(double[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the float value at the current position
   * @param value the value to put
   */
  public abstract void putFloat(float value);

  /**
   * Puts the float array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putFloatArray(float[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the int value at the current position
   * @param value the value to put
   */
  public abstract void putInt(int value);

  /**
   * Puts the int array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putIntArray(int[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the long value at the current position
   * @param value the value to put
   */
  public abstract void putLong(long value);

  /**
   * Puts the long array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putLongArray(long[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the short value at the current position
   * @param value the value to put
   */
  public abstract void putShort(short value);

  /**
   * Puts the short array at the current position
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putShortArray(short[] srcArray,
      final int srcOffset, final int length);

  //Atomic Methods XXX
  /**
   * Atomically adds the given value to the long located at offsetBytes.
   * @param delta the amount to add
   * @return the modified value
   */
  public abstract long getAndAddLong(long delta);

  /**
   * Atomically sets the current value at the memory location to the given updated value
   * if and only if the current value {@code ==} the expected value.
   * @param expect the expected value
   * @param update the new value
   * @return {@code true} if successful. False return indicates that
   * the current value at the memory location was not equal to the expected value.
   */
  public abstract boolean compareAndSwapLong(long expect, long update);

  /**
   * Atomically exchanges the given value with the current value located at offsetBytes.
   * @param newValue new value
   * @return the previous value
   */
  public abstract long getAndSetLong(long newValue);

  //OTHER WRITE METHODS XXX
  /**
   * Returns the primitive backing array, otherwise null.
   * @return the primitive backing array, otherwise null.
   */
  public abstract Object getArray();

  /**
   * Clears all bytes of this Buffer from position to limit to zero
   */
  public abstract void clear();

  /**
   * Clears the bits defined by the bitMask
   * @param bitMask the bits set to one will be cleared
   */
  public abstract void clearBits(byte bitMask);

  /**
   * Fills this Buffer from position to limit with the given byte value.
   * @param value the given byte value
   */
  public abstract void fill(byte value);

  /**
   * Sets the bits defined by the bitMask
   * @param bitMask the bits set to one will be set
   */
  public abstract void setBits(byte bitMask);

  //OTHER XXX
  /**
   * Returns a MemoryRequest or null
   * @return a MemoryRequest or null
   */
  public abstract MemoryRequest getMemoryRequest();

}
