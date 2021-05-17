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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;

/**
 * Provides read-only primitive and primitive array methods to any of the four resources
 * mentioned in the package level documentation.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 *
 * @see org.apache.datasketches.memory.internal
 */
public abstract class MemoryImpl extends BaseStateImpl implements Memory {

  //Pass-through ctor
  MemoryImpl(final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  //BYTE BUFFER
  public static MemoryImpl wrap(final ByteBuffer byteBuf) {
    return BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, true, byteBuf.order());
  }

  public static MemoryImpl wrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    return BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, true, byteOrder);
  }

  //MAP
  public static MapHandle map(final File file) {
    return map(file, 0, file.length(), ByteOrder.nativeOrder());
  }

  public static MapHandle map(final File file, final long fileOffsetBytes, final long capacityBytes,
      final ByteOrder byteOrder) {
    zeroCheck(capacityBytes, "Capacity");
    nullCheck(file, "file is null");
    negativeCheck(fileOffsetBytes, "File offset is negative");
    return (MapHandle) BaseWritableMemoryImpl.wrapMap(file, fileOffsetBytes, capacityBytes, true, byteOrder);
  }

  //REGIONS
  @Override
  public abstract MemoryImpl region(long offsetBytes, long capacityBytes);

  @Override
  public abstract MemoryImpl region(long offsetBytes, long capacityBytes, ByteOrder byteOrder);

  //AS BUFFER
  @Override
  public abstract Buffer asBuffer();

  @Override
  public abstract Buffer asBuffer(ByteOrder byteOrder);

  //UNSAFE BYTE BUFFER VIEW
  @Override
  public abstract ByteBuffer unsafeByteBufferView(long offsetBytes, int capacityBytes);

  //ACCESS PRIMITIVE HEAP ARRAYS for readOnly
  public static Memory wrap(final boolean[] arr) {
    final long lengthBytes = arr.length << Prim.BOOLEAN.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true,
        Util.nativeByteOrder);
  }

  public static Memory wrap(final byte[] arr) {
    return MemoryImpl.wrap(arr, 0, arr.length, Util.nativeByteOrder);
  }

  public static Memory wrap(final byte[] arr, final ByteOrder byteOrder) {
    return MemoryImpl.wrap(arr, 0, arr.length, byteOrder);
  }

  public static Memory wrap(final byte[] arr, final int offsetBytes, final int lengthBytes,
      final ByteOrder byteOrder) {
    UnsafeUtil.checkBounds(offsetBytes, lengthBytes, arr.length);
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, byteOrder);
  }

  public static Memory wrap(final char[] arr) {
    final long lengthBytes = arr.length << Prim.CHAR.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, Util.nativeByteOrder);
  }

  public static Memory wrap(final short[] arr) {
    final long lengthBytes = arr.length << Prim.SHORT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, Util.nativeByteOrder);
  }

  public static Memory wrap(final int[] arr) {
    final long lengthBytes = arr.length << Prim.INT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, Util.nativeByteOrder);
  }

  public static Memory wrap(final long[] arr) {
    final long lengthBytes = arr.length << Prim.LONG.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, Util.nativeByteOrder);
  }

  public static Memory wrap(final float[] arr) {
    final long lengthBytes = arr.length << Prim.FLOAT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, Util.nativeByteOrder);
  }

  public static Memory wrap(final double[] arr) {
    final long lengthBytes = arr.length << Prim.DOUBLE.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, true, Util.nativeByteOrder);
  }

  //PRIMITIVE getX() and getXArray()

  @Override
  public abstract boolean getBoolean(long offsetBytes);

  @Override
  public abstract void getBooleanArray(long offsetBytes, boolean[] dstArray, int dstOffsetBooleans,
      int lengthBooleans);

  @Override
  public abstract byte getByte(long offsetBytes);

  @Override
  public abstract void getByteArray(long offsetBytes, byte[] dstArray, int dstOffsetBytes,
      int lengthBytes);

  @Override
  public abstract char getChar(long offsetBytes);

  @Override
  public abstract void getCharArray(long offsetBytes, char[] dstArray, int dstOffsetChars,
      int lengthChars);

  @Override
  public abstract int getCharsFromUtf8(long offsetBytes, int utf8LengthBytes, Appendable dst)
      throws IOException, Utf8CodingException;

  @Override
  public abstract int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final StringBuilder dst) throws Utf8CodingException;

  @Override
  public abstract double getDouble(long offsetBytes);

  @Override
  public abstract void getDoubleArray(long offsetBytes, double[] dstArray, int dstOffsetDoubles,
      int lengthDoubles);

  @Override
  public abstract float getFloat(long offsetBytes);

  @Override
  public abstract void getFloatArray(long offsetBytes, float[] dstArray, int dstOffsetFloats,
      int lengthFloats);

  @Override
  public abstract int getInt(long offsetBytes);

  @Override
  public abstract void getIntArray(long offsetBytes, int[] dstArray, int dstOffsetInts,
      int lengthInts);

  @Override
  public abstract long getLong(long offsetBytes);

  @Override
  public abstract void getLongArray(long offsetBytes, long[] dstArray, int dstOffsetLongs,
      int lengthLongs);

  @Override
  public abstract short getShort(long offsetBytes);

  @Override
  public abstract void getShortArray(long offsetBytes, short[] dstArray, int dstOffsetShorts,
      int lengthShorts);

  //SPECIAL PRIMITIVE READ METHODS: compareTo, copyTo, writeTo
  @Override
  public abstract int compareTo(long thisOffsetBytes, long thisLengthBytes, Memory that,
      long thatOffsetBytes, long thatLengthBytes);

  @Override
  public abstract void copyTo(long srcOffsetBytes, WritableMemory destination, long dstOffsetBytes,
      long lengthBytes);


  @Override
  public abstract void writeTo(long offsetBytes, long lengthBytes, WritableByteChannel out)
      throws IOException;

}
