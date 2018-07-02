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

  //Pass-through ctor
  Buffer(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  //BYTE BUFFER
  /**
   * Accesses the given ByteBuffer for read-only operations. The returned Buffer object has the
   * same byte order, as the given ByteBuffer, unless the capacity of the given ByteBuffer is zero,
   * then byte order of the returned Buffer object (as well as backing storage) is unspecified.
   * @param byteBuf the given ByteBuffer, must not be null.
   * @return a new Buffer for read-only operations on the given ByteBuffer.
   */
  public static Buffer wrap(final ByteBuffer byteBuf) {
    return wrap(byteBuf, byteBuf.order());
  }

  /**
   * Accesses the given ByteBuffer for read-only operations. The returned Buffer object has
   * the given byte order, ignoring the byte order of the given ByteBuffer. If the capacity of
   * the given ByteBuffer is zero the byte order of the returned Buffer object
   * (as well as backing storage) is unspecified.
   * @param byteBuf the given ByteBuffer, must not be null
   * @param byteOrder the byte order to be used, which may be independent of the byte order
   * state of the given ByteBuffer
   * @return a new Buffer for read-only operations on the given ByteBuffer.
   */
  public static Buffer wrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    final BaseWritableMemoryImpl wmem =
        BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, true, byteOrder);
    final WritableBuffer wbuf = wmem.asWritableBufferImpl(true, byteOrder);
    wbuf.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    return wbuf;
  }

  //MAP
  //Use Memory for mapping files and the asBuffer()

  //DUPLICATES
  /**
   * Returns a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = this object's <i>start</i></li>
   * <li>Returned object's <i>position</i> = this object's <i>position</i></li>
   * <li>Returned object's <i>end</i> = this object's <i>end</i></li>
   * <li>Returned object's <i>capacity</i> = this object' <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @return a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  public abstract Buffer duplicate();

  /**
   * Returns a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>, but with the specified byteOrder.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = this object's <i>start</i></li>
   * <li>Returned object's <i>position</i> = this object's <i>position</i></li>
   * <li>Returned object's <i>end</i> = this object's <i>end</i></li>
   * <li>Returned object's <i>capacity</i> = this object' <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @param byteOrder the given <i>ByteOrder</i>.
   * @return a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  public abstract Buffer duplicate(ByteOrder byteOrder);

  //REGIONS
  /**
   * A region is a read-only view of this object.
   * <ul>
   * <li>Returned object's origin = this object's <i>position</i></li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's (<i>end</i> - <i>position</i>)</li>
   * <li>Returned object's <i>capacity</i> = this object's (<i>end</i> - <i>position</i>)</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @return a new <i>Buffer</i> representing the defined region based on the current
   * <i>position</i> and <i>end</i>.
   */
  public abstract Buffer region();

  /**
   * A region is a read-only view of this object.
   * <ul>
   * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = <i>capacityBytes</i></li>
   * <li>Returned object's <i>capacity</i> = <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * <li>Returned object's byte order = <i>byteOrder</i></li>
   * </ul>
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   *
   * <p><b>Note: The Memory returned with </b><i>asMemory()</i> will have the originating
   * <i>Memory</i> byte order.</p>
   *
   * @param offsetBytes the starting offset with respect to the origin of this <i>WritableBuffer</i>
   * @param capacityBytes the <i>capacity</i> of the returned region in bytes
   * @param byteOrder the given byte order
   * @return a new <i>WritableBuffer</i> representing the defined writable region
   * based on the current <i>position</i>, <i>end</i> and byteOrder.
   */
  public abstract Buffer region(long offsetBytes, long capacityBytes,
      ByteOrder byteOrder);

  //MEMORY
  /**
   * Convert this Buffer to a Memory. The current <i>start</i>, <i>position</i> and <i>end</i>
   * are ignored.
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing resource and byte order are unspecified.
   * @return Memory
   */
  public abstract Memory asMemory();

  //ACCESS PRIMITIVE HEAP ARRAYS for readOnly
  // use Memory or WritableMemory and then asBuffer().
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE getX() and getXArray()
  /**
   * Gets the boolean value at the current position.
   * Increments the position by 1.
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
   * Increments the position by <i>lengthBooleans - dstOffsetBooleans</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetBooleans offset in array units
   * @param lengthBooleans number of array units to transfer
   */
  public abstract void getBooleanArray(boolean[] dstArray, int dstOffsetBooleans,
      int lengthBooleans);

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
   * Increments the position by <i>Byte.BYTES * (lengthBytes - dstOffsetBytes)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  public abstract void getByteArray(byte[] dstArray, int dstOffsetBytes, int lengthBytes);

  /**
   * Gets the char value at the current position.
   * Increments the position by <i>Character.BYTES</i>.
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
   * Increments the position by <i>Character.BYTES * (lengthChars - dstOffsetChars)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  public abstract void getCharArray(char[] dstArray, int dstOffsetChars, int lengthChars);

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
   * Increments the position by <i>Double.BYTES * (lengthDoubles - dstOffsetDoubles)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  public abstract void getDoubleArray(double[] dstArray, int dstOffsetDoubles, int lengthDoubles);

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
   * Increments the position by <i>Float.BYTES * (lengthFloats - dstOffsetFloats)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  public abstract void getFloatArray(float[] dstArray, int dstOffsetFloats, int lengthFloats);

  /**
   * Gets the int value at the current position.
   * Increments the position by <i>Integer.BYTES</i>.
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
   * Increments the position by <i>Integer.BYTES * (lengthInts - dstOffsetInts)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  public abstract void getIntArray(int[] dstArray, int dstOffsetInts, int lengthInts);

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
   * Increments the position by <i>Long.BYTES * (lengthLongs - dstOffsetLongs)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  public abstract void getLongArray(long[] dstArray, int dstOffsetLongs, int lengthLongs);

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
   * Increments the position by <i>Short.BYTES * (lengthShorts - dstOffsetShorts)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  public abstract void getShortArray(short[] dstArray, int dstOffsetShorts, int lengthShorts);

  //SPECIAL PRIMITIVE READ METHODS: compareTo
  /**
   * Compares the bytes of this Buffer to <i>that</i> Buffer.
   * This uses absolute offsets not the start, position and end.
   * Returns <i>(this &lt; that) ? (some negative value) : (this &gt; that) ? (some positive value)
   * : 0;</i>.
   * If all bytes are equal up to the shorter of the two lengths, the shorter length is
   * considered to be less than the other.
   * @param thisOffsetBytes the starting offset for <i>this Buffer</i>
   * @param thisLengthBytes the length of the region to compare from <i>this Buffer</i>
   * @param that the other Buffer to compare with
   * @param thatOffsetBytes the starting offset for <i>that Buffer</i>
   * @param thatLengthBytes the length of the region to compare from <i>that Buffer</i>
   * @return <i>(this &lt; that) ? (some negative value) : (this &gt; that) ? (some positive value)
   * : 0;</i>
   */
  public abstract int compareTo(long thisOffsetBytes, long thisLengthBytes, Buffer that,
          long thatOffsetBytes, long thatLengthBytes);

}
