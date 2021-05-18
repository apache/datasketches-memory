/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import static org.apache.datasketches.memory.internal.Util.negativeCheck;
import static org.apache.datasketches.memory.internal.Util.nullCheck;
import static org.apache.datasketches.memory.internal.Util.zeroCheck;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableDirectHandle;
import org.apache.datasketches.memory.WritableMapHandle;
import org.apache.datasketches.memory.WritableMemory;


/**
 * Provides read and write primitive and primitive array access to any of the four resources
 * mentioned at the package level.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public abstract class WritableMemoryImpl extends MemoryImpl implements WritableMemory {

  //Pass-through ctor
  WritableMemoryImpl(final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  //BYTE BUFFER
  public static WritableMemoryImpl writableWrap(final ByteBuffer byteBuf) {
    return BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, false, byteBuf.order());
  }

  public static WritableMemoryImpl writableWrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    return BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, false, byteOrder);
  }

  //MAP
  public static WritableMapHandle writableMap(final File file) {
    return WritableMemoryImpl.writableMap(file, 0, file.length(), Util.nativeByteOrder);
  }

  public static WritableMapHandle writableMap(final File file, final long fileOffsetBytes,
      final long capacityBytes, final ByteOrder byteOrder) {
    zeroCheck(capacityBytes, "Capacity");
    nullCheck(file, "file is null");
    negativeCheck(fileOffsetBytes, "File offset is negative");
    return BaseWritableMemoryImpl
        .wrapMap(file, fileOffsetBytes, capacityBytes, false, byteOrder);
  }

  //ALLOCATE DIRECT
  public static WritableDirectHandle allocateDirect(final long capacityBytes) {
    return allocateDirect(capacityBytes, null);
  }

  public static WritableDirectHandle allocateDirect(final long capacityBytes,
      final MemoryRequestServer memReqSvr) {
    return BaseWritableMemoryImpl.wrapDirect(capacityBytes, Util.nativeByteOrder, memReqSvr);
  }

  //REGIONS
  @Override
  public abstract WritableMemoryImpl writableRegion(long offsetBytes, long capacityBytes);

  @Override
  public abstract WritableMemoryImpl writableRegion(long offsetBytes, long capacityBytes,
      ByteOrder byteOrder);

  //AS BUFFER
  @Override
  public abstract WritableBufferImpl asWritableBuffer();

  @Override
  public abstract WritableBufferImpl asWritableBuffer(ByteOrder byteOrder);

  //ALLOCATE HEAP VIA AUTOMATIC BYTE ARRAY
  public static WritableMemoryImpl allocate(final int capacityBytes) {
    final byte[] arr = new byte[capacityBytes];
    return writableWrap(arr, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl allocate(final int capacityBytes, final ByteOrder byteOrder) {
    final byte[] arr = new byte[capacityBytes];
    return writableWrap(arr, byteOrder);
  }

  //ACCESS PRIMITIVE HEAP ARRAYS for write
  public static WritableMemoryImpl writableWrap(final boolean[] arr) {
    final long lengthBytes = arr.length << Prim.BOOLEAN.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final byte[] arr) {
    return WritableMemoryImpl.writableWrap(arr, 0, arr.length, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final byte[] arr, final ByteOrder byteOrder) {
    return WritableMemoryImpl.writableWrap(arr, 0, arr.length, byteOrder);
  }

  public static WritableMemoryImpl writableWrap(final byte[] arr, final int offsetBytes, final int lengthBytes,
      final ByteOrder byteOrder) {
    UnsafeUtil.checkBounds(offsetBytes, lengthBytes, arr.length);
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, byteOrder);
  }

  public static WritableMemoryImpl writableWrap(final char[] arr) {
    final long lengthBytes = arr.length << Prim.CHAR.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final short[] arr) {
    final long lengthBytes = arr.length << Prim.SHORT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final int[] arr) {
    final long lengthBytes = arr.length << Prim.INT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final long[] arr) {
    final long lengthBytes = arr.length << Prim.LONG.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final float[] arr) {
    final long lengthBytes = arr.length << Prim.FLOAT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }

  public static WritableMemoryImpl writableWrap(final double[] arr) {
    final long lengthBytes = arr.length << Prim.DOUBLE.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, Util.nativeByteOrder);
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putX() and putXArray()
  @Override
  public abstract void putBoolean(long offsetBytes, boolean value);

  @Override
  public abstract void putBooleanArray(long offsetBytes, boolean[] srcArray, int srcOffsetBooleans,
          int lengthBooleans);

  @Override
  public abstract void putByte(long offsetBytes, byte value);

  @Override
  public abstract void putByteArray(long offsetBytes, byte[] srcArray, int srcOffsetBytes,
          int lengthBytes);

  @Override
  public abstract void putChar(long offsetBytes, char value);

  @Override
  public abstract void putCharArray(long offsetBytes, char[] srcArray, int srcOffsetChars,
          int lengthChars);

  @Override
  public abstract long putCharsToUtf8(long offsetBytes, CharSequence src);

  @Override
  public abstract void putDouble(long offsetBytes, double value);

  @Override
  public abstract void putDoubleArray(long offsetBytes, double[] srcArray,
          final int srcOffsetDoubles, final int lengthDoubles);

  @Override
  public abstract void putFloat(long offsetBytes, float value);

  @Override
  public abstract void putFloatArray(long offsetBytes, float[] srcArray,
          final int srcOffsetFloats, final int lengthFloats);

  @Override
  public abstract void putInt(long offsetBytes, int value);

  @Override
  public abstract void putIntArray(long offsetBytes, int[] srcArray,
          final int srcOffsetInts, final int lengthInts);

  @Override
  public abstract void putLong(long offsetBytes, long value);

  @Override
  public abstract void putLongArray(long offsetBytes, long[] srcArray,
          final int srcOffsetLongs, final int lengthLongs);

  @Override
  public abstract void putShort(long offsetBytes, short value);

  @Override
  public abstract void putShortArray(long offsetBytes, short[] srcArray,
          final int srcOffsetShorts, final int lengthShorts);

  //Atomic Methods
  @Override
  public abstract long getAndAddLong(long offsetBytes, long delta);

  @Override
  public abstract boolean compareAndSwapLong(long offsetBytes, long expect, long update);

  @Override
  public abstract long getAndSetLong(long offsetBytes, long newValue);

  //OTHER WRITE METHODS
  @Override
  public abstract Object getArray();

  @Override
  public abstract void clear();

  @Override
  public abstract void clear(long offsetBytes, long lengthBytes);

  @Override
  public abstract void clearBits(long offsetBytes, byte bitMask);

  @Override
  public abstract void fill(byte value);

  @Override
  public abstract void fill(long offsetBytes, long lengthBytes, byte value);

  @Override
  public abstract void setBits(long offsetBytes, byte bitMask);

  
  //OTHER WRITABLE API METHODS
  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
  }
  
}
