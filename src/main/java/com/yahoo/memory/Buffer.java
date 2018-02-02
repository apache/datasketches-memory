/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides read-only, positional primitive and primitive array methods to any of the four resources
 * mentioned in the package level documentation.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 *
 * @see com.yahoo.memory
 */
public abstract class Buffer extends BaseBuffer {

  Buffer(final ResourceState state) {
    super(state);
  }

  //BYTE BUFFER XXX
  /**
   * Accesses the given ByteBuffer for read-only operations.
   * @param byteBuf the given ByteBuffer, must not be null.
   * @return the given ByteBuffer for read-only operations.
   */
  public static Buffer wrap(final ByteBuffer byteBuf) {
    if (byteBuf.capacity() == 0) {
      return WritableBufferImpl.ZERO_SIZE_BUFFER;
    }
    final ResourceState state = new ResourceState();
    state.putByteBuffer(byteBuf);
    AccessByteBuffer.wrap(state);
    final WritableBufferImpl impl = new WritableBufferImpl(state);
    impl.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    return impl;
  }

  //MAP XXX
  //Use Memory for mapping files and the asBuffer()

  //DUPLICATES & REGIONS XXX
  /**
   * Returns a read only duplicate view of this Buffer with the same but independent values of
   * start, position, and end.
   * @return a read only duplicate view of this Buffer with the same but independent values of
   * start, position, and end.
   */
  public abstract Buffer duplicate();

  /**
   * Returns a read only region of this Buffer starting at position ending at end.
   * The region start and position will be zero, the region end and capacity will be this
   * buffer's end minus position.
   * @return a read only region of this Buffer.
   */
  public abstract Buffer region();

  //MEMORY XXX
  /**
   * Convert this Buffer to a Memory. The current start, position and end are ignored.
   * @return Memory
   */
  public abstract Memory asMemory();

  //ACCESS PRIMITIVE HEAP ARRAYS for readOnly XXX
  // use Memory or WritableMemory and then asBuffer().
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE getXXX() and getXXXArray() //XXX
  /**
   * Gets the boolean value at the current position.
   * Increments the position by <i>Boolean.BYTES</i>.
   * @return the boolean at the current position
   */
  public abstract boolean getBoolean();

  /**
   * Gets the boolean value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the boolean at the given offset
   */
  public abstract boolean getBoolean(long offsetBytes);

  /**
   * Gets the boolean array at the current position.
   * Increments the position by <i>Boolean.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getBooleanArray(boolean[] dstArray, int dstOffset, int length);

  /**
   * Gets the byte value at the current position.
   * Increments the position by <i>Byte.BYTES</i>.
   * @return the byte at the current position
   */
  public abstract byte getByte();

  /**
   * Gets the byte value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the byte at the given offset
   */
  public abstract byte getByte(long offsetBytes);

  /**
   * Gets the byte array at the current position.
   * Increments the position by <i>Byte.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getByteArray(byte[] dstArray, int dstOffset, int length);

  /**
   * Gets the char value at the current position.
   * Increments the position by <i>Char.BYTES</i>.
   * @return the char at the current position
   */
  public abstract char getChar();

  /**
   * Gets the char value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the char at the given offset
   */
  public abstract char getChar(long offsetBytes);

  /**
   * Gets the char array at the current position.
   * Increments the position by <i>Char.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getCharArray(char[] dstArray, int dstOffset, int length);

  /**
   * Gets the double value at the current position.
   * Increments the position by <i>Double.BYTES</i>.
   * @return the double at the current position
   */
  public abstract double getDouble();

  /**
   * Gets the double value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the double at the given offset
   */
  public abstract double getDouble(long offsetBytes);

  /**
   * Gets the double array at the current position.
   * Increments the position by <i>Double.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getDoubleArray(double[] dstArray, int dstOffset, int length);

  /**
   * Gets the float value at the current position.
   * Increments the position by <i>Float.BYTES</i>.
   * @return the float at the current position
   */
  public abstract float getFloat();

  /**
   * Gets the float value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the float at the given offset
   */
  public abstract float getFloat(long offsetBytes);

  /**
   * Gets the float array at the current position.
   * Increments the position by <i>Float.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getFloatArray(float[] dstArray, int dstOffset, int length);

  /**
   * Gets the int value at the current position.
   * Increments the position by <i>Int.BYTES</i>.
   * @return the int at the current position
   */
  public abstract int getInt();

  /**
   * Gets the int value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the int at the given offset
   */
  public abstract int getInt(long offsetBytes);

  /**
   * Gets the int array at the current position.
   * Increments the position by <i>Int.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getIntArray(int[] dstArray, int dstOffset, int length);

  /**
   * Gets the long value at the current position.
   * Increments the position by <i>Long.BYTES</i>.
   * @return the long at the current position
   */
  public abstract long getLong();

  /**
   * Gets the long value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the long at the given offset
   */
  public abstract long getLong(long offsetBytes);

  /**
   * Gets the long array at the current position.
   * Increments the position by <i>Long.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getLongArray(long[] dstArray, int dstOffset, int length);

  /**
   * Gets the short value at the current position.
   * Increments the position by <i>Short.BYTES</i>.
   * @return the short at the current position
   */
  public abstract short getShort();

  /**
   * Gets the short value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the short at the given offset
   */
  public abstract short getShort(long offsetBytes);

  /**
   * Gets the short array at the current position.
   * Increments the position by <i>Short.BYTES * (length - dstOffset)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffset offset in array units
   * @param length number of array units to transfer
   */
  public abstract void getShortArray(short[] dstArray, int dstOffset, int length);

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  /**
   * Compares the bytes of this Buffer to <i>that</i> Buffer.
   * This uses absolute offsets not the start, position and end.
   * Returns <i>(this &lt; that) ? -1 : (this &gt; that) ? 1 : 0;</i>.
   * If all bytes are equal up to the shorter of the two lengths, the shorter length is
   * considered to be less than the other.
   * @param thisOffsetBytes the starting offset for <i>this Buffer</i>
   * @param thisLengthBytes the length of the region to compare from <i>this Buffer</i>
   * @param that the other Buffer to compare with
   * @param thatOffsetBytes the starting offset for <i>that Buffer</i>
   * @param thatLengthBytes the length of the region to compare from <i>that Buffer</i>
   * @return <i>(this &lt; that) ? -1 : (this &gt; that) ? 1 : 0;</i>
   */
  public abstract int compareTo(long thisOffsetBytes, long thisLengthBytes, Buffer that,
          long thatOffsetBytes, long thatLengthBytes);

  //OTHER READ METHODS XXX
  /**
   * Checks that the specified range of bytes is within bounds of this Memory object, throws
   * {@link IllegalArgumentException} if it's not: i. e. if offsetBytes &lt; 0, or length &lt; 0,
   * or offsetBytes + length &gt; {@link #getCapacity()}.
   * @param offsetBytes the offset of the range of bytes to check
   * @param length the length of the range of bytes to check
   */
  public abstract void checkValidAndBounds(final long offsetBytes, final long length);

  /**
   * Gets the capacity of this Buffer in bytes
   * @return the capacity of this Buffer in bytes
   */
  public abstract long getCapacity();

  /**
   * Returns the cumulative offset in bytes of this Buffer from the backing resource
   * including the Java object header, if any.
   *
   * @return the cumulative offset in bytes of this Buffer.
   */
  public abstract long getCumulativeOffset();

  /**
   * Returns the ByteOrder for the backing resource.
   * @return the ByteOrder for the backing resource.
   */
  public abstract ByteOrder getResourceOrder();

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
   * Returns true if the backing resource of <i>this</i> is identical with the backing resource
   * of <i>that</i>. If the backing resource is a heap array or ByteBuffer, the offset and
   * capacity must also be identical.
   * @param that A different given Buffer object
   * @return true if the backing resource of <i>this</i> is identical with the backing resource
   * of <i>that</i>.
   */
  public abstract boolean isSameResource(Buffer that);

  /**
   * Returns true if this Buffer is valid() and has not been closed.
   * @return true if this Buffer is valid() and has not been closed.
   */
  public abstract boolean isValid();

  /**
   * Return true if bytes <b>need</b> to be swapped based on resource ByteOrder.
   * This is a convenience method to indicate that the wrapped ByteBuffer has a Byte Order that
   * is different from the native Byte Order. It is up to the user to perform the byte swapping
   * if necessary.
   * @return true if bytes need to be swapped based on resource ByteOrder.
   */
  public abstract boolean swapBytes();

  /**
   * Returns a formatted hex string of a range of this Buffer.
   * Used primarily for testing.
   * @param header descriptive header
   * @param offsetBytes offset bytes relative to this Buffer start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  public abstract String toHexString(String header, long offsetBytes, int lengthBytes);

  abstract ResourceState getResourceState();
}
