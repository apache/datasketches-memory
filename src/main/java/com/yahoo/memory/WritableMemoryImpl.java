/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.BOOLEAN_SHIFT;
import static com.yahoo.memory.UnsafeUtil.BYTE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.UNSAFE_COPY_THRESHOLD;
import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.checkOverlap;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of WritableMemory
 *
 * UTF-8 encoding/decoding is based on
 * https://github.com/google/protobuf/blob/3e944aec9ebdf5043780fba751d604c0a55511f2/
 * java/core/src/main/java/com/google/protobuf/Utf8.java
 *
 * Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class WritableMemoryImpl extends WritableMemory {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long unsafeObjHeader; //Heap ByteBuffer includes the slice() offset here.
  final long capacity;
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  //Static variable for cases where byteBuf/array sizes are zero
  final static WritableMemoryImpl ZERO_SIZE_MEMORY;

  static {
    ZERO_SIZE_MEMORY = new WritableMemoryImpl(
        new ResourceState(new byte[0], Prim.BYTE, 0)
    );
  }

  WritableMemoryImpl(final ResourceState state) {
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    unsafeObjHeader = state.getUnsafeObjectHeader();
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
  }

  //REGIONS/DUPLICATES XXX
  @Override
  public Memory duplicate() {
    return region(0, capacity);
  }

  @Override
  public WritableMemory writableDuplicate() {
    return writableRegion(0, capacity);
  }

  @Override
  public Memory region(final long offsetBytes, final long capacityBytes) {
    checkValid();
    return writableRegion(offsetBytes, capacityBytes);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes) {
    checkValid();
    assert (offsetBytes + capacityBytes) <= capacity
        : "newOff + newCap: " + (offsetBytes + capacityBytes) + ", origCap: " + capacity;
    final ResourceState newState = state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    return new WritableMemoryImpl(newState);
  }

  //BUFFER XXX
  @Override
  public Buffer asBuffer() {
    return asWritableBuffer();
  }

  @Override
  public WritableBuffer asWritableBuffer() {
    final ResourceState newState = state.copy();
    final WritableBufferImpl impl = new WritableBufferImpl(newState); //with new BaseBuffer
    final ByteBuffer byteBuf = newState.getByteBuffer();
    if (byteBuf != null) {
      impl.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    } //else defaults
    return impl;
  }

  ///PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final long offsetBytes, final boolean[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + (dstOffset << BOOLEAN_SHIFT),
        copyBytes);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final long offsetBytes, final byte[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << BYTE_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + (dstOffset << BYTE_SHIFT),
        copyBytes);
  }

  @Override
  public char getChar(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE, capacity);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << CHAR_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_CHAR_BASE_OFFSET + (dstOffset << CHAR_SHIFT),
        copyBytes);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << DOUBLE_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_DOUBLE_BASE_OFFSET + (dstOffset << DOUBLE_SHIFT),
        copyBytes);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE, capacity);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << FLOAT_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_FLOAT_BASE_OFFSET + (dstOffset << FLOAT_SHIFT),
        copyBytes);
  }

  @Override
  public int getInt(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << INT_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_INT_BASE_OFFSET + (dstOffset << INT_SHIFT),
        copyBytes);
  }

  @Override
  public long getLong(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << LONG_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_LONG_BASE_OFFSET + (dstOffset << LONG_SHIFT),
        copyBytes);
  }

  @Override
  public short getShort(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE, capacity);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << SHORT_SHIFT;
    assertBounds(offsetBytes, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_SHORT_BASE_OFFSET + (dstOffset << SHORT_SHIFT),
        copyBytes);
  }

  @Override
  public void getUtf8(long offsetBytes, Appendable dst, int utf8Length)
      throws IOException, Utf8CodingException {
    checkBounds(offsetBytes, utf8Length, capacity);

    long address = getCumulativeOffset(offsetBytes);
    final long addressLimit = address + utf8Length;

    // Optimize for 100% ASCII (Hotspot loves small simple top-level loops like this).
    // This simple loop stops when we encounter a byte >= 0x80 (i.e. non-ASCII).
    while (address < addressLimit) {
      byte b = unsafe.getByte(unsafeObj, address);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      address++;
      dst.append((char) b);
    }

    while (address < addressLimit) {
      byte byte1 = unsafe.getByte(unsafeObj, address++);
      if (DecodeUtil.isOneByte(byte1)) {
        dst.append((char) byte1);
        // It's common for there to be multiple ASCII characters in a run mixed in, so add an
        // extra optimized loop to take care of these runs.
        while (address < addressLimit) {
          byte b = unsafe.getByte(unsafeObj, address);
          if (!DecodeUtil.isOneByte(b)) {
            break;
          }
          address++;
          dst.append((char) b);
        }
      } else if (DecodeUtil.isTwoBytes(byte1)) {
        if (address >= addressLimit) {
          throw Utf8CodingException.input();
        }
        DecodeUtil.handleTwoBytes(
            byte1, /* byte2 */ unsafe.getByte(unsafeObj, address++), dst);
      } else if (DecodeUtil.isThreeBytes(byte1)) {
        if (address >= addressLimit - 1) {
          throw Utf8CodingException.input();
        }
        DecodeUtil.handleThreeBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            /* byte3 */ unsafe.getByte(unsafeObj, address++),
            dst);
      } else {
        if (address >= addressLimit - 2) {
          throw Utf8CodingException.input();
        }
        DecodeUtil.handleFourBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            /* byte3 */ unsafe.getByte(unsafeObj, address++),
            /* byte4 */ unsafe.getByte(unsafeObj, address++),
            dst);
      }
    }
  }

  /**
   * Utility methods for decoding bytes into {@link String}. Callers are responsible for extracting
   * bytes (possibly using Unsafe methods), and checking remaining bytes. All other UTF-8 validity
   * checks and codepoint conversion happen in this class.
   */
  private static class DecodeUtil {

    /**
     * Returns whether this is a single-byte codepoint (i.e., ASCII) with the form '0XXXXXXX'.
     */
    private static boolean isOneByte(byte b) {
      return b >= 0;
    }

    /**
     * Returns whether this is a two-byte codepoint with the form '10XXXXXX'.
     */
    private static boolean isTwoBytes(byte b) {
      return b < (byte) 0xE0;
    }

    /**
     * Returns whether this is a three-byte codepoint with the form '110XXXXX'.
     */
    private static boolean isThreeBytes(byte b) {
      return b < (byte) 0xF0;
    }

    private static void handleTwoBytes(byte byte1, byte byte2, Appendable dst)
        throws IOException, Utf8CodingException {
      // Simultaneously checks for illegal trailing-byte in leading position (<= '11000000') and
      // overlong 2-byte, '11000001'.
      if (byte1 < (byte) 0xC2
          || isNotTrailingByte(byte2)) {
        throw Utf8CodingException.input();
      }
      dst.append((char) (((byte1 & 0x1F) << 6) | trailingByteValue(byte2)));
    }

    private static void handleThreeBytes(byte byte1, byte byte2, byte byte3, Appendable dst)
        throws IOException, Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // overlong? 5 most significant bits must not all be zero
          || (byte1 == (byte) 0xE0 && byte2 < (byte) 0xA0)
          // check for illegal surrogate codepoints
          || (byte1 == (byte) 0xED && byte2 >= (byte) 0xA0)
          || isNotTrailingByte(byte3)) {
        throw Utf8CodingException.input();
      }
      dst.append((char)
          (((byte1 & 0x0F) << 12) | (trailingByteValue(byte2) << 6) | trailingByteValue(byte3)));
    }

    private static void handleFourBytes(
        byte byte1, byte byte2, byte byte3, byte byte4, Appendable dst)
        throws IOException, Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // Check that 1 <= plane <= 16.  Tricky optimized form of:
          //   valid 4-byte leading byte?
          // if (byte1 > (byte) 0xF4 ||
          //   overlong? 4 most significant bits must not all be zero
          //     byte1 == (byte) 0xF0 && byte2 < (byte) 0x90 ||
          //   codepoint larger than the highest code point (U+10FFFF)?
          //     byte1 == (byte) 0xF4 && byte2 > (byte) 0x8F)
          || (((byte1 << 28) + (byte2 - (byte) 0x90)) >> 30) != 0
          || isNotTrailingByte(byte3)
          || isNotTrailingByte(byte4)) {
        throw Utf8CodingException.input();
      }
      int codepoint = ((byte1 & 0x07) << 18)
                      | (trailingByteValue(byte2) << 12)
                      | (trailingByteValue(byte3) << 6)
                      | trailingByteValue(byte4);
      dst.append(DecodeUtil.highSurrogate(codepoint));
      dst.append(DecodeUtil.lowSurrogate(codepoint));
    }

    /**
     * Returns whether the byte is not a valid continuation of the form '10XXXXXX'.
     */
    private static boolean isNotTrailingByte(byte b) {
      return b > (byte) 0xBF;
    }

    /**
     * Returns the actual value of the trailing byte (removes the prefix '10') for composition.
     */
    private static int trailingByteValue(byte b) {
      return b & 0x3F;
    }

    private static char highSurrogate(int codePoint) {
      return (char) ((Character.MIN_HIGH_SURROGATE
                      - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10))
                     + (codePoint >>> 10));
    }

    private static char lowSurrogate(int codePoint) {
      return (char) (Character.MIN_LOW_SURROGATE + (codePoint & 0x3ff));
    }
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Memory that,
      final long thatOffsetBytes, final long thatLengthBytes) {
    checkValid();
    ((WritableMemoryImpl)that).checkValid();
    assertBounds(thisOffsetBytes, thisLengthBytes, capacity);
    assertBounds(thatOffsetBytes, thatLengthBytes, that.getCapacity());
    final long thisAdd = getCumulativeOffset(thisOffsetBytes);
    final long thatAdd = that.getCumulativeOffset(thatOffsetBytes);
    final Object thisObj = (isDirect()) ? null : unsafeObj;
    final Object thatObj = (that.isDirect()) ? null : ((WritableMemory)that).getArray();
    final long lenBytes = Math.min(thisLengthBytes, thatLengthBytes);
    for (long i = 0; i < lenBytes; i++) {
      final int thisByte = unsafe.getByte(thisObj, thisAdd + i);
      final int thatByte = unsafe.getByte(thatObj, thatAdd + i);
      if (thisByte < thatByte) { return -1; }
      if (thisByte > thatByte) { return  1; }
    }
    if (thisLengthBytes < thatLengthBytes) { return -1; }
    if (thisLengthBytes > thatLengthBytes) { return  1; }
    return 0;
  }

  @Override
  public void copyTo(final long srcOffsetBytes, final WritableMemory destination,
      final long dstOffsetBytes, final long lengthBytes) {
    checkValid();
    assertBounds(srcOffsetBytes, lengthBytes, capacity);
    assertBounds(dstOffsetBytes, lengthBytes, destination.getCapacity());
    assert ((this == destination)
        ? checkOverlap(srcOffsetBytes, dstOffsetBytes, lengthBytes)
        : true) : "Region Overlap" ;

    long srcAdd = getCumulativeOffset(srcOffsetBytes);
    long dstAdd = destination.getCumulativeOffset(dstOffsetBytes);
    final Object srcParent = (isDirect()) ? null : unsafeObj;
    final Object dstParent = (destination.isDirect()) ? null : destination.getArray();
    long lenBytes = lengthBytes;

    while (lenBytes > 0) {
      final long chunkBytes = (lenBytes > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : lenBytes;
      unsafe.copyMemory(srcParent, srcAdd, dstParent, dstAdd, lenBytes);
      lenBytes -= chunkBytes;
      srcAdd += chunkBytes;
      dstAdd += chunkBytes;
    }
  }

  //OTHER READ METHODS XXX
  @Override
  public long getCapacity() {
    checkValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset(final long offsetBytes) {
    checkValid();
    return cumBaseOffset + offsetBytes;
  }

  private long getOffsetBytes(final long cumulativeOffset) {
    return cumulativeOffset - cumBaseOffset;
  }

  @Override
  public long getRegionOffset(final long offsetBytes) {
    checkValid();
    return state.getRegionOffset() + offsetBytes;
  }

  @Override
  public ByteOrder getResourceOrder() {
    checkValid();
    return state.order();
  }

  @Override
  public boolean hasArray() {
    checkValid();
    return unsafeObj != null;
  }

  @Override
  public boolean hasByteBuffer() {
    checkValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    checkValid();
    return state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    checkValid();
    return state.isResourceReadOnly();
  }

  @Override
  public boolean isSameResource(final Memory that) {
    if (that == null) { return false; }
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public boolean isValid() {
    return state.isValid();
  }

  @Override
  public boolean swapBytes() {
    return state.isSwapBytes();
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
    checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Params         : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, state);
  }

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public void putBoolean(final long offsetBytes, final boolean value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final long offsetBytes, final boolean[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_BOOLEAN_BASE_OFFSET + (srcOffset << BOOLEAN_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final long offsetBytes, final byte[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << BYTE_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_BYTE_BASE_OFFSET + (srcOffset << BYTE_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE, capacity);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << CHAR_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_CHAR_BASE_OFFSET + (srcOffset << CHAR_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << DOUBLE_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_DOUBLE_BASE_OFFSET + (srcOffset << DOUBLE_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE, capacity);
    unsafe.putFloat(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << FLOAT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_FLOAT_BASE_OFFSET + (srcOffset << FLOAT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << INT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_INT_BASE_OFFSET + (srcOffset << INT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << LONG_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_LONG_BASE_OFFSET + (srcOffset << LONG_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE, capacity);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffset,
      final int length) {
    checkValid();
    final long copyBytes = length << SHORT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_SHORT_BASE_OFFSET + (srcOffset << SHORT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public long putUtf8(long offsetBytes, CharSequence src)
  {
    int utf16Length = src.length();
    long j = getCumulativeOffset(offsetBytes);
    int i = 0;
    long limit = getCumulativeOffset(capacity);
    for (char c; i < utf16Length && i + j < limit && (c = src.charAt(i)) < 0x80; i++) {
      unsafe.putByte(unsafeObj, j + i, (byte) c);
    }
    if (i == utf16Length) {
      return j + utf16Length;
    }
    j += i;
    for (char c; i < utf16Length; i++) {
      c = src.charAt(i);
      if (c < 0x80 && j < limit) {
        unsafe.putByte(unsafeObj, j++, (byte) c);
      } else if (c < 0x800 && j <= limit - 2) { // 11 bits, two UTF-8 bytes
        unsafe.putByte(unsafeObj, j++, (byte) ((0xF << 6) | (c >>> 6)));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & c)));
      } else if ((c < Character.MIN_SURROGATE || Character.MAX_SURROGATE < c) && j <= limit - 3) {
        // Maximum single-char code point is 0xFFFF, 16 bits, three UTF-8 bytes
        unsafe.putByte(unsafeObj, j++, (byte) ((0xF << 5) | (c >>> 12)));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & (c >>> 6))));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & c)));
      } else if (j <= limit - 4) {
        // Minimum code point represented by a surrogate pair is 0x10000, 17 bits,
        // four UTF-8 bytes
        final char low;
        if (i + 1 == src.length()
            || !Character.isSurrogatePair(c, (low = src.charAt(++i)))) {
          throw new UnpairedSurrogateException((i - 1), utf16Length);
        }
        int codePoint = Character.toCodePoint(c, low);
        unsafe.putByte(unsafeObj, j++, (byte) ((0xF << 4) | (codePoint >>> 18)));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & (codePoint >>> 12))));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & (codePoint >>> 6))));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & codePoint)));
      } else {
        // If we are surrogates and we're not a surrogate pair, always throw an
        // UnpairedSurrogateException instead of an ArrayOutOfBoundsException.
        if ((Character.MIN_SURROGATE <= c && c <= Character.MAX_SURROGATE)
            && (i + 1 == src.length()
                || !Character.isSurrogatePair(c, src.charAt(i + 1)))) {
          throw new UnpairedSurrogateException(i, utf16Length);
        }
        throw new IllegalArgumentException(
                "Failed writing " + c + " at offset " + getOffsetBytes(j));
      }
    }
    return getOffsetBytes(j);
  }

  static class UnpairedSurrogateException extends IllegalArgumentException {
    UnpairedSurrogateException(int index, int length) {
      super("Unpaired surrogate at index " + index + " of " + length);
    }
  }

  //Atomic Write Methods XXX
  @Override
  public long getAndAddLong(final long offsetBytes, final long delta) { //JDK 8+
    checkValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    final long add = cumBaseOffset + offsetBytes;
    return UnsafeUtil.compatibilityMethods.getAndAddLong(unsafeObj, add, delta) + delta;
  }

  @Override
  public long getAndSetLong(final long offsetBytes, final long newValue) { //JDK 8+
    checkValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    final long add = cumBaseOffset + offsetBytes;
    return UnsafeUtil.compatibilityMethods.getAndSetLong(unsafeObj, add, newValue);
  }

  @Override
  public boolean compareAndSwapLong(final long offsetBytes, final long expect, final long update) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    return unsafe.compareAndSwapLong(unsafeObj, cumBaseOffset + offsetBytes, expect, update);
  }

  //OTHER WRITE METHODS XXX
  @Override
  public Object getArray() {
    checkValid();
    return unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    checkValid();
    return state.getByteBuffer();
  }

  @Override
  public void clear() {
    fill(0, capacity, (byte) 0);
  }

  @Override
  public void clear(final long offsetBytes, final long lengthBytes) {
    fill(offsetBytes, lengthBytes, (byte) 0);
  }

  @Override
  public void clearBits(final long offsetBytes, final byte bitMask) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    final long cumBaseOff = cumBaseOffset + offsetBytes;
    int value = unsafe.getByte(unsafeObj, cumBaseOff) & 0XFF;
    value &= ~bitMask;
    unsafe.putByte(unsafeObj, cumBaseOff, (byte)value);
  }

  @Override
  public void fill(final byte value) {
    fill(0, capacity, value);
  }

  @Override
  public void fill(final long offsetBytes, final long lengthBytes, final byte value) {
    checkValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
    unsafe.setMemory(unsafeObj, cumBaseOffset + offsetBytes, lengthBytes, value);
  }

  @Override
  public void setBits(final long offsetBytes, final byte bitMask) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    final long myOffset = cumBaseOffset + offsetBytes;
    final byte value = unsafe.getByte(unsafeObj, myOffset);
    unsafe.putByte(unsafeObj, myOffset, (byte)(value | bitMask));
  }

  //OTHER XXX
  @Override
  public MemoryRequestServer getMemoryRequestServer() { //only applicable to writable
    checkValid();
    return state.getMemoryRequestServer();
  }

  @Override
  public void setMemoryRequest(final MemoryRequestServer memReqSvr) {
    state.setMemoryRequestServer(memReqSvr);
  }

  @Override
  public WritableDirectHandle getHandle() {
    return state.getHandle();
  }

  @Override
  public void setHandle(final WritableDirectHandle handle) {
    state.setHandle(handle);
  }

  //RESTRICTED READ AND WRITE XXX
  private final void checkValid() { //applies to both readable and writable
    assert state.isValid() : "Memory not valid.";
  }

  @Override
  ResourceState getResourceState() {
    return state;
  }

}
