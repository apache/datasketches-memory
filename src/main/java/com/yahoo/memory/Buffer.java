/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

//import static com.yahoo.memory.UnsafeUtil.LS;
//import static com.yahoo.memory.UnsafeUtil.assertBounds;
//import static com.yahoo.memory.UnsafeUtil.unsafe;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides read-only, positional primitive and primitive array methods to any of the four resources
 * mentioned at the package level.
 *
 * @author Lee Rhodes
 */
public abstract class Buffer extends BaseBuffer {

  Buffer(final ResourceState state) {
    super(state);
  }

  //BYTE BUFFER XXX
  /**
   * Accesses the given ByteBuffer for read-only operations.
   * @param byteBuf the given ByteBuffer
   * @return the given ByteBuffer for read-only operations.
   */
  public static Buffer wrap(final ByteBuffer byteBuf) {
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
  //Use Memory for mapping files

  //REGIONS/DUPLICATES XXX
  /**
   * Returns a read only duplicate view of this Buffer with the same but independent values of
   * low, pos, high and capacity.
   * @return a read only duplicate view of this Buffer with the same but independent values of
   * low, pos, high and capacity.
   */
  public abstract Buffer duplicate();

  /**
   * Returns a read only region of this Buffer starting at position ending at limit.
   * @return a read only region of this Buffer
   */
  public abstract Buffer region();

  //MEMORY XXX
  /**
   * Convert this Buffer to a Memory
   * @return Memory
   */
  public abstract Memory asMemory();

  //ACCESS PRIMITIVE HEAP ARRAYS for readOnly XXX
  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final boolean[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.BOOLEAN, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final byte[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.BYTE, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final char[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.CHAR, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final short[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.SHORT, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final int[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.INT, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final long[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.LONG, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final float[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.FLOAT, arr.length));
  }

  /**
   * Wraps the given primitive array for read operations
   * @param arr the given primitive array
   * @return Buffer for read operations
   */
  public static Buffer wrap(final double[] arr) {
    return new WritableBufferImpl(new ResourceState(arr, Prim.DOUBLE, arr.length));
  }

  //PRIMITIVE getXXX() and getXXXArray() //XXX
  /**
   * Gets the boolean value at the current position
   * @return the boolean at the current position
   */
  public abstract boolean getBoolean();

  /**
   * Gets the boolean array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getBooleanArray(boolean[] dstArray, int dstOffset, int length);

  /**
   * Gets the byte value at the current position
   * @return the byte at the current position
   */
  public abstract byte getByte();

  /**
   * Gets the byte array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getByteArray(byte[] dstArray, int dstOffset, int length);

  /**
   * Gets the char value at the current position
   * @return the char at the current position
   */
  public abstract char getChar();

  /**
   * Gets the char array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getCharArray(char[] dstArray, int dstOffset, int length);

  /**
   * Gets the double value at the current position
   * @return the double at the current position
   */
  public abstract double getDouble();

  /**
   * Gets the double array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getDoubleArray(double[] dstArray, int dstOffset, int length);

  /**
   * Gets the float value at the current position
   * @return the float at the current position
   */
  public abstract float getFloat();

  /**
   * Gets the float array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getFloatArray(float[] dstArray, int dstOffset, int length);

  /**
   * Gets the int value at the current position
   * @return the int at the current position
   */
  public abstract int getInt();

  /**
   * Gets the int array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getIntArray(int[] dstArray, int dstOffset, int length);

  /**
   * Gets the long value at the current position
   * @return the long at the current position
   */
  public abstract long getLong();

  /**
   * Gets the long array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getLongArray(long[] dstArray, int dstOffset, int length);

  /**
   * Gets the short value at the current position
   * @return the short at the current position
   */
  public abstract short getShort();

  /**
   * Gets the short array at the current position
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getShortArray(short[] dstArray, int dstOffset, int length);

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  /**
   * Compares the bytes of this Memory to <i>that</i> Memory.  This uses absolute offsets not
   * the start, position and end.
   * Returns <i>(this &lt; that) ? -1 : (this &gt; that) ? 1 : 0;</i>.
   * If all bytes are equal up to the shorter of the two lengths, the shorter length is considered
   * to be less than the other.
   * @param thisOffsetBytes the starting offset for <i>this Memory</i>
   * @param thisLengthBytes the length of the region to compare from <i>this Memory</i>
   * @param that the other Memory to compare with
   * @param thatOffsetBytes the starting offset for <i>that Memory</i>
   * @param thatLengthBytes the length of the region to compare from <i>that Memory</i>
   * @return <i>(this &lt; that) ? -1 : (this &gt; that) ? 1 : 0;</i>
   */
  public abstract int compareTo(long thisOffsetBytes, long thisLengthBytes, Buffer that,
      long thatOffsetBytes, long thatLengthBytes);

  //OTHER READ METHODS XXX
  /**
   * Gets the capacity of this Buffer in bytes
   * @return the capacity of this Buffer in bytes
   */
  public abstract long getCapacity();

  /**
   * Returns the cumulative offset in bytes of this Buffer at the current position.
   *
   * @return the cumulative offset in bytes of this Buffer at the current position.
   */
  public abstract long getCumulativeOffset();

  /**
   * Returns true if this Buffer is backed by an on-heap primitive array
   * @return true if this Buffer is backed by an on-heap primitive array
   */
  public abstract boolean hasArray();

  /**
   * Returns true if this Buffer is backed by a ByteBuffer
   * @return true if this Buffer is backed by a ByteBuffer
   */
  public abstract boolean hasByteBuffer();

  /**
   * Returns true if the backing memory is direct (off-heap) memory.
   * @return true if the backing memory is direct (off-heap) memory.
   */
  public abstract boolean isDirect();

  /**
   * Returns true if the backing resource is read only
   * @return true if the backing resource is read only
   */
  public abstract boolean isResourceReadOnly();

  /**
   * Returns true if this Buffer is valid() and has not been closed.
   * @return true if this Buffer is valid() and has not been closed.
   */
  public abstract boolean isValid();

  /**
   * Returns a formatted hex string of a range of this Buffer.
   * Used primarily for testing.
   * @param header descriptive header
   * @param offsetBytes offset bytes relative to this Buffer start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  public abstract String toHexString(String header, long offsetBytes, int lengthBytes);


}
