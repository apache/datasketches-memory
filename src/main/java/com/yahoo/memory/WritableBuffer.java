/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public abstract class WritableBuffer extends Buffer {

  //PRIMITIVE putXXX() and putXXXArray() //XXX

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

  //Atomic Methods //XXX

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
   * @param lengthBytes the length in bytes
   */
  public abstract void clear(long lengthBytes);

  /**
   * Clears the bits defined by the bitMask
   * @param bitMask the bits set to one will be cleared
   */
  public abstract void clearBits(byte bitMask);

  /**
   * Fills all bytes of this Memory region to the given byte value.
   * @param value the given byte value
   */
  public abstract void fill(byte value);

  /**
   * Fills a portion of this Memory region to the given byte value.
   * @param lengthBytes the length in bytes
   * @param value the given byte value
   */
  public abstract void fill(long lengthBytes, byte value);

  /**
   * Sets the bits defined by the bitMask
   * @param bitMask the bits set to one will be set
   */
  public abstract void setBits(byte bitMask);

  //OTHER //XXX

  /**
   * Returns a MemoryRequest or null
   * @return a MemoryRequest or null
   */
  public abstract MemoryRequest getMemoryRequest();


}
