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
  public void getBooleanArray(boolean[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public byte getByte() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getByteArray(byte[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public char getChar() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getCharArray(char[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public double getDouble() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getDoubleArray(double[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public float getFloat() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getFloatArray(float[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getInt() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getIntArray(int[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getLong() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getLongArray(long[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public short getShort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getShortArray(short[] dstArray, int dstOffset, int length) {
    // TODO Auto-generated method stub

  }

  //OTHER PRIMITIVE READ METHODS: copy, isYYYY(), areYYYY() //XXX

  @Override
  public int compareTo(long thisLengthBytes, Buffer that, long thatLengthBytes) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void copyTo(WritableBuffer destination, long lengthBytes) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAllBitsClear(byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAllBitsSet(byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAnyBitsClear(byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAnyBitsSet(byte bitMask) {
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
  public String toHexString(String header, long offsetBytes, int lengthBytes) {
    // TODO Auto-generated method stub
    return null;
  }

  //PRIMITIVE putXXX() and putXXXArray() //XXX

  @Override
  public void putBoolean(boolean value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putBooleanArray(boolean[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putByte(byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putByteArray(byte[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putChar(char value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putCharArray(char[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putDouble(double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putDoubleArray(double[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putFloat(float value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putFloatArray(float[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putInt(int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putIntArray(int[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putLong(long value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putLongArray(long[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putShort(short value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putShortArray(short[] srcArray, int srcOffset, int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getAndAddLong(long delta) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean compareAndSwapLong(long expect, long update) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public long getAndSetLong(long newValue) {
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
  public void clear(long lengthBytes) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearBits(byte bitMask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fill(byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fill(long lengthBytes, byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setBits(byte bitMask) {
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
  public void limit(long lim) {
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
  public void position(long pos) {
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
