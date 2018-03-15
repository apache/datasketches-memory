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
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public abstract class WritableBuffer extends Buffer {

  WritableBuffer(final long capacity) {
    super(capacity);
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
    return wrapBB(byteBuf, false);
  }

  static WritableBuffer wrapBB(final ByteBuffer byteBuf, final boolean localReadOnly) {
    final WritableMemory wmem = WritableMemory.wrapBB(byteBuf, localReadOnly);
    final WritableBuffer wbuf = wmem.asWritableBuffer();
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
   * Increments the position by <i>Boolean.BYTES</i>.
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
   * Increments the position by <i>Boolean.BYTES * (lengthBooleans - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthBooleans number of array units to transfer
   */
  public abstract void putBooleanArray(boolean[] srcArray, int srcOffset, int lengthBooleans);

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
   * Increments the position by <i>Byte.BYTES * (lengthBytes - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthBytes number of array units to transfer
   */
  public abstract void putByteArray(byte[] srcArray, int srcOffset, int lengthBytes);

  /**
   * Puts the char value at the current position.
   * Increments the position by <i>Char.BYTES</i>.
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
   * Increments the position by <i>Char.BYTES * (lengthChars - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthChars number of array units to transfer
   */
  public abstract void putCharArray(char[] srcArray, int srcOffset, int lengthChars);

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
   * Increments the position by <i>Double.BYTES * (lengthDoubles - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  public abstract void putDoubleArray(double[] srcArray, int srcOffset, int lengthDoubles);

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
   * Increments the position by <i>Float.BYTES * (lengthFloats - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthFloats number of array units to transfer
   */
  public abstract void putFloatArray(float[] srcArray, int srcOffset, int lengthFloats);

  /**
   * Puts the int value at the current position.
   * Increments the position by <i>Int.BYTES</i>.
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
   * Increments the position by <i>Int.BYTES * (lengthInts - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthInts number of array units to transfer
   */
  public abstract void putIntArray(int[] srcArray, int srcOffset, int lengthInts);

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
   * Increments the position by <i>Long.BYTES * (lengthLongs - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthLongs number of array units to transfer
   */
  public abstract void putLongArray(long[] srcArray, int srcOffset, int lengthLongs);

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
   * Increments the position by <i>Short.BYTES * (lengthShorts - srcOffset)</i>.
   * @param srcArray The source array.
   * @param srcOffset offset in array units
   * @param lengthShorts number of array units to transfer
   */
  public abstract void putShortArray(short[] srcArray, int srcOffset, int lengthShorts);

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

  //OTHER XXX
  /**
   * Returns the offset of the start of this WritableBuffer from the backing resource,
   * but not including any Java object header.
   *
   * @return the offset of the start of this WritableBuffer from the backing resource.
   */
  public abstract long getRegionOffset();

}
