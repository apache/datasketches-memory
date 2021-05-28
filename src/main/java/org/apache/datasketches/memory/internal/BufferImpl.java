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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.datasketches.memory.Buffer;

/**
 * Provides read-only, positional primitive and primitive array methods to any of the four resources
 * mentioned in the package level documentation.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 *
 * @see org.apache.datasketches.memory.internal
 */
public abstract class BufferImpl extends BaseBufferImpl implements Buffer {

  //Pass-through ctor
  BufferImpl(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  //BYTE BUFFER
  public static BufferImpl wrap(final ByteBuffer byteBuf) {
    return wrap(byteBuf, byteBuf.order());
  }

  public static BufferImpl wrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    final BaseWritableMemoryImpl wmem =
        BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, true, byteOrder);
    final WritableBufferImpl wbuf = wmem.asWritableBuffer(true, byteOrder);
    wbuf.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    return wbuf;
  }

  //MAP
  //Use MemoryImpl for mapping files and the asBuffer()

  //DUPLICATES
  @Override
  public abstract BufferImpl duplicate();

  @Override
  public abstract BufferImpl duplicate(ByteOrder byteOrder);

  //REGIONS
  @Override
  public abstract BufferImpl region();

  @Override
  public abstract BufferImpl region(long offsetBytes, long capacityBytes,
      ByteOrder byteOrder);

  //MEMORY
  @Override
  public abstract MemoryImpl asMemory();

  //ACCESS PRIMITIVE HEAP ARRAYS for readOnly
  // use MemoryImpl or WritableMemoryImpl and then asBuffer().
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE getX() and getXArray()
  @Override
  public abstract boolean getBoolean();

  @Override
  public abstract boolean getBoolean(long offsetBytes);

  @Override
  public abstract void getBooleanArray(boolean[] dstArray, int dstOffsetBooleans,
      int lengthBooleans);

  @Override
  public abstract byte getByte();

  @Override
  public abstract byte getByte(long offsetBytes);

  @Override
  public abstract void getByteArray(byte[] dstArray, int dstOffsetBytes, int lengthBytes);

  @Override
  public abstract char getChar();

  @Override
  public abstract char getChar(long offsetBytes);

  @Override
  public abstract void getCharArray(char[] dstArray, int dstOffsetChars, int lengthChars);

  @Override
  public abstract double getDouble();

  @Override
  public abstract double getDouble(long offsetBytes);

  @Override
  public abstract void getDoubleArray(double[] dstArray, int dstOffsetDoubles, int lengthDoubles);

  @Override
  public abstract float getFloat();

  @Override
  public abstract float getFloat(long offsetBytes);

  @Override
  public abstract void getFloatArray(float[] dstArray, int dstOffsetFloats, int lengthFloats);

  @Override
  public abstract int getInt();

  @Override
  public abstract int getInt(long offsetBytes);

  @Override
  public abstract void getIntArray(int[] dstArray, int dstOffsetInts, int lengthInts);

  @Override
  public abstract long getLong();

  @Override
  public abstract long getLong(long offsetBytes);

  @Override
  public abstract void getLongArray(long[] dstArray, int dstOffsetLongs, int lengthLongs);

  @Override
  public abstract short getShort();

  @Override
  public abstract short getShort(long offsetBytes);

  @Override
  public abstract void getShortArray(short[] dstArray, int dstOffsetShorts, int lengthShorts);

  //SPECIAL PRIMITIVE READ METHODS: compareTo
  @Override
  public abstract int compareTo(long thisOffsetBytes, long thisLengthBytes, Buffer that,
          long thatOffsetBytes, long thatLengthBytes);

}
