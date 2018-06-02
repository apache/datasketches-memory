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
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public abstract class WritableBuffer extends Buffer {

  //Pass-through ctor for all parameters
  WritableBuffer(
      final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes, final boolean readOnly, final ByteOrder byteOrder,
      final ByteBuffer byteBuf, final StepBoolean valid) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, readOnly, byteOrder,
        byteBuf, valid);
  }

  //BYTE BUFFER XXX
  /**
   * Accesses the given ByteBuffer for write operations. The returned WritableBuffer object has
   * the same byte order, as the given ByteBuffer, unless the capacity of the given ByteBuffer is
   * zero, then endianness of the returned WritableBuffer object, as well as backing storage and
   * read-only status are unspecified.
   * @param byteBuf the given ByteBuffer, must not be null.
   * @return a new WritableBuffer for write operations on the given ByteBuffer.
   */
  public static WritableBuffer wrap(final ByteBuffer byteBuf) {
    return wrap(byteBuf, byteBuf.order());
  }

  /**
   * Accesses the given ByteBuffer for write operations. The returned WritableBuffer object has
   * the given byte order, ignoring the byte order of the given ByteBuffer. If the capacity of
   * the given ByteBuffer is zero the endianness of the returned WritableBuffer object
   * (as well as backing storage) is unspecified.
   * @param byteBuf the given ByteBuffer, must not be null
   * @param byteOrder the byte order to be used, which may be independent of the byte order
   * state of the given ByteBuffer
   * @return a new WritableBuffer for write operations on the given ByteBuffer.
   */
  public static WritableBuffer wrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    final BaseWritableMemoryImpl wmem =
        BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, false, byteOrder);
    final WritableBuffer wbuf = wmem.asWritableBufferImpl(false);
    wbuf.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    return wbuf;
  }

  //MAP XXX
  //Use WritableMemory for mapping files and then asWritableBuffer()

  //ALLOCATE DIRECT XXX
  //Use WritableMemory to allocate direct memory and then asWritableBuffer().

  //DUPLICATES XXX
  /**
   * Returns a duplicate writable view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and endianness are unspecified.
   * @return a duplicate writable view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  public abstract WritableBuffer writableDuplicate();

  //REGIONS XXX
  /**
   * A writable region is a writable view of the backing store of this object.
   * This returns a new <i>WritableBuffer</i> representing the defined writable region.
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
   * the backing storage and endianness are unspecified.
   * @return a new <i>WritableBuffer</i> representing the defined writable region.
   */
  public abstract WritableBuffer writableRegion();

  /**
   * A writable region is a writable view of the backing store of this object.
   * This returns a new <i>WritableBuffer</i> representing the defined writable region.
   * <ul>
   * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = <i>capacityBytes</i></li>
   * <li>Returned object's <i>capacity</i> = <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and endianness are unspecified.
   * @param offsetBytes the starting offset with respect to the origin of this <i>WritableBuffer</i>
   * @param capacityBytes the <i>capacity</i> of the returned region in bytes
   * @return a new <i>WritableBuffer</i> representing the defined writable region.
   */
  public abstract WritableBuffer writableRegion(long offsetBytes, long capacityBytes);

  //AS MEMORY XXX
  /**
   * Convert this WritableBuffer to a WritableMemory.
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and endianness are unspecified.
   * @return WritableMemory
   */
  public abstract WritableMemory asWritableMemory();

  //ACCESS PRIMITIVE HEAP ARRAYS for write XXX
  //use WritableMemory and then asWritableBuffer().
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putXXX() and putXXXArray() XXX
  /**
   * Puts the boolean value at the current position.
   * Increments the position by 1.
   * @param value the value to put
   */
  public abstract void putBoolean(boolean value);

  /**
   * Puts the boolean value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start.
   * @param value the value to put
   */
  public abstract void putBoolean(long offsetBytes, boolean value);

  /**
   * Puts the boolean array at the current position.
   * Increments the position by <i>lengthBooleans - srcOffsetBooleans</i>.
   * @param srcArray The source array.
   * @param srcOffsetBooleans offset in array units
   * @param lengthBooleans number of array units to transfer
   */
  public abstract void putBooleanArray(boolean[] srcArray, int srcOffsetBooleans,
      int lengthBooleans);

  /**
   * Puts the byte value at the current position.
   * Increments the position by <i>Byte.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putByte(byte value);

  /**
   * Puts the byte value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putByte(long offsetBytes, byte value);

  /**
   * Puts the byte array at the current position.
   * Increments the position by <i>Byte.BYTES * (lengthBytes - srcOffsetBytes)</i>.
   * @param srcArray The source array.
   * @param srcOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  public abstract void putByteArray(byte[] srcArray, int srcOffsetBytes, int lengthBytes);

  /**
   * Puts the char value at the current position.
   * Increments the position by <i>Character.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putChar(char value);

  /**
   * Puts the char value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putChar(long offsetBytes, char value);

  /**
   * Puts the char array at the current position.
   * Increments the position by <i>Character.BYTES * (lengthChars - srcOffsetChars)</i>.
   * @param srcArray The source array.
   * @param srcOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  public abstract void putCharArray(char[] srcArray, int srcOffsetChars, int lengthChars);

  /**
   * Puts the double value at the current position.
   * Increments the position by <i>Double.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putDouble(double value);

  /**
   * Puts the double value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putDouble(long offsetBytes, double value);

  /**
   * Puts the double array at the current position.
   * Increments the position by <i>Double.BYTES * (lengthDoubles - srcOffsetDoubles)</i>.
   * @param srcArray The source array.
   * @param srcOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  public abstract void putDoubleArray(double[] srcArray, int srcOffsetDoubles, int lengthDoubles);

  /**
   * Puts the float value at the current position.
   * Increments the position by <i>Float.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putFloat(float value);

  /**
   * Puts the float value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putFloat(long offsetBytes, float value);

  /**
   * Puts the float array at the current position.
   * Increments the position by <i>Float.BYTES * (lengthFloats - srcOffsetFloats)</i>.
   * @param srcArray The source array.
   * @param srcOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  public abstract void putFloatArray(float[] srcArray, int srcOffsetFloats, int lengthFloats);

  /**
   * Puts the int value at the current position.
   * Increments the position by <i>Integer.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putInt(int value);

  /**
   * Puts the int value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putInt(long offsetBytes, int value);

  /**
   * Puts the int array at the current position.
   * Increments the position by <i>Integer.BYTES * (lengthInts - srcOffsetInts)</i>.
   * @param srcArray The source array.
   * @param srcOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  public abstract void putIntArray(int[] srcArray, int srcOffsetInts, int lengthInts);

  /**
   * Puts the long value at the current position.
   * Increments the position by <i>Long.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putLong(long value);

  /**
   * Puts the long value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putLong(long offsetBytes, long value);

  /**
   * Puts the long array at the current position.
   * Increments the position by <i>Long.BYTES * (lengthLongs - srcOffsetLongs)</i>.
   * @param srcArray The source array.
   * @param srcOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  public abstract void putLongArray(long[] srcArray, int srcOffsetLongs, int lengthLongs);

  /**
   * Puts the short value at the current position.
   * Increments the position by <i>Short.BYTES</i>.
   * @param value the value to put
   */
  public abstract void putShort(short value);

  /**
   * Puts the short value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putShort(long offsetBytes, short value);

  /**
   * Puts the short array at the current position.
   * Increments the position by <i>Short.BYTES * (lengthShorts - srcOffsetShorts)</i>.
   * @param srcArray The source array.
   * @param srcOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  public abstract void putShortArray(short[] srcArray, int srcOffsetShorts, int lengthShorts);

  //OTHER WRITE METHODS XXX
  /**
   * Returns the primitive backing array, otherwise null.
   * @return the primitive backing array, otherwise null.
   */
  public abstract Object getArray();

  /**
   * Clears all bytes of this Buffer from position to end to zero. The position will be set to end.
   */
  public abstract void clear();

  /**
   * Fills this Buffer from position to end with the given byte value.
   * The position will be set to <i>end</i>.
   * @param value the given byte value
   */
  public abstract void fill(byte value);

  //OTHER XXX
  /**
   * Returns the offset of the start of this WritableBuffer from the backing resource,
   * but not including any Java object header.
   *
   * @return the offset of the start of this WritableBuffer from the backing resource.
   */
  public abstract long getRegionOffset();

}
