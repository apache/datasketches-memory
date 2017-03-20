/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public class WritableBufferImpl extends WritableBuffer implements Positional {

  //PRIMITIVE getXXX() and getXXXArray() //XXX

  @Override
  public boolean getBoolean() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void getBooleanArray(final boolean[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public byte getByte() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getByteArray(final byte[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public char getChar() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public double getDouble() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public float getFloat() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getInt() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getLong() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public short getShort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  //OTHER PRIMITIVE READ METHODS: copy, final isYYYY(), final areYYYY() //XXX

  @Override
  public int compareTo(final long thisLengthBytes, final Buffer that, final long thatLengthBytes) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void copyTo(final WritableBuffer destination, final long lengthBytes) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAllBitsClear(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAllBitsSet(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAnyBitsClear(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAnyBitsSet(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  //OTHER READ METHODS //XXX

  @Override
  public long getCapacity() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getCumulativeOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean hasArray() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean hasByteBuffer() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isDirect() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isResourceReadOnly() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isValid() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
    // TODO Auto-generated method stub
    return null;
  }

  //PRIMITIVE putXXX() and putXXXArray() //XXX

  @Override
  public void putBoolean(final boolean value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putBooleanArray(final boolean[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putByte(final byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putByteArray(final byte[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putChar(final char value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putDouble(final double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putFloat(final float value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putInt(final int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putLong(final long value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putShort(final short value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getAndAddLong(final long delta) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean compareAndSwapLong(final long expect, final long update) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public long getAndSetLong(final long newValue) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Object getArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub

  }

  @Override
  public void clear(final long lengthBytes) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearBits(final byte bitMask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fill(final byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fill(final long lengthBytes, final byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setBits(final byte bitMask) {
    // TODO Auto-generated method stub

  }

  @Override
  public MemoryRequest getMemoryRequest() {
    // TODO Auto-generated method stub
    return null;
  }

  //POSITIONAL

  @Override
  public void flip() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean hasRemaining() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public long limit() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void limit(final long lim) {
    // TODO Auto-generated method stub

  }

  @Override
  public void mark() {
    // TODO Auto-generated method stub

  }

  @Override
  public long position() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void position(final long pos) {
    // TODO Auto-generated method stub

  }

  @Override
  public long remaining() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void rewind() {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearPositions() {
    // TODO Auto-generated method stub

  }
}
