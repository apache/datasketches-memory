/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public abstract class Buffer {


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

  //OTHER PRIMITIVE READ METHODS: copy, isYYYY(), areYYYY() //XXX

  /**
   * Compares the bytes of this Buffer to <i>that</i> Buffer starting at their current positions.
   * Returns <i>(this &lt; that) ? -1 : (this &gt; that) ? 1 : 0;</i>.
   * If all bytes are equal up to the shorter of the two lengths, the shorter length is considered
   * to be less than the other.
   * @param thisLengthBytes the length of the region to compare from <i>this Buffer</i>
   * @param that the other Buffer to compare with
   * @param thatLengthBytes the length of the region to compare from <i>that Buffer</i>
   * @return <i>(this &lt; that) ? -1 : (this &gt; that) ? 1 : 0;</i>
   */
  public abstract int compareTo(long thisLengthBytes, Buffer that, long thatLengthBytes);

  /**
   * Copies bytes from a source range of this Buffer to a destination range of the given
   * WritableBuffer relative to their respective positions using the same low-level system copy
   * function as found in {@link java.lang.System#arraycopy(Object, int, Object, int, int)}.
   * @param destination the destination Buffer, which may not be Read-Only.
   * @param lengthBytes the number of bytes to copy
   */
  public abstract void copyTo(WritableBuffer destination, long lengthBytes);

  /**
   * Returns true if all bits defined by the bitMask are clear
   * @param bitMask bits set to one will be checked
   * @return true if all bits defined by the bitMask are clear
   */
  public abstract boolean isAllBitsClear(byte bitMask);

  /**
   * Returns true if all bits defined by the bitMask are set
   * @param bitMask bits set to one will be checked
   * @return true if all bits defined by the bitMask are set
   */
  public abstract boolean isAllBitsSet(byte bitMask);

  /**
   * Returns true if any bits defined by the bitMask are clear
   * @param bitMask bits set to one will be checked
   * @return true if any bits defined by the bitMask are clear
   */
  public abstract boolean isAnyBitsClear(byte bitMask);

  /**
   * Returns true if any bits defined by the bitMask are set
   * @param bitMask bits set to one will be checked
   * @return true if any bits defined by the bitMask are set
   */
  public abstract boolean isAnyBitsSet(byte bitMask);

  //OTHER READ METHODS //XXX

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
