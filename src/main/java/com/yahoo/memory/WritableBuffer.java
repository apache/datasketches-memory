/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;

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
   * start, position, end and capacity.
   * @return a writable duplicate view of this Buffer with the same but independent values of
   * start, position, end and capacity.
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
   * Puts the boolean value at the current position. Increments the position by <i>Boolean.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putBoolean(boolean value);

  /**
   * Puts the boolean array at the current position. Increments the position by <i>Boolean.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putBooleanArray(boolean[] srcArray, int srcOffset,
      int length);

  /**
   * Puts the byte value at the current position. Increments the position by <i>Byte.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putByte(byte value);

  /**
   * Puts the byte array at the current position. Increments the position by <i>Byte.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putByteArray(byte[] srcArray, int srcOffset,
      int length);

  /**
   * Puts the char value at the current position. Increments the position by <i>Char.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putChar(char value);

  /**
   * Puts the char array at the current position. Increments the position by <i>Char.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putCharArray(char[] srcArray, int srcOffset,
      int length);

  /**
   * Puts the double value at the current position. Increments the position by <i>Double.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putDouble(double value);

  /**
   * Puts the double array at the current position. Increments the position by <i>Double.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putDoubleArray(double[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the float value at the current position. Increments the position by <i>Float.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putFloat(float value);

  /**
   * Puts the float array at the current position. Increments the position by <i>Float.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putFloatArray(float[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the int value at the current position. Increments the position by <i>Int.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putInt(int value);

  /**
   * Puts the int array at the current position. Increments the position by <i>Int.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putIntArray(int[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the long value at the current position. Increments the position by <i>Long.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putLong(long value);

  /**
   * Puts the long array at the current position. Increments the position by <i>Long.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putLongArray(long[] srcArray,
      final int srcOffset, final int length);

  /**
   * Puts the short value at the current position. Increments the position by <i>Short.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putShort(short value);

  /**
   * Puts the short array at the current position. Increments the position by <i>Short.BYTES * (length - dstOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void putShortArray(short[] srcArray,
      final int srcOffset, final int length);

  //Atomic Methods XXX
  //Use WritableMemory for atomic methods

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
   * Clears all bytes of this Buffer from position to end to zero. The position will be set to end.
   */
  public abstract void clear();

  /**
   * Fills this Buffer from position to end with the given byte value. The position will be set to end.
   * @param value the given byte value
   */
  public abstract void fill(byte value);

}
