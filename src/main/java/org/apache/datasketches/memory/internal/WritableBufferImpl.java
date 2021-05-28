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

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;

/**
 * Provides read and write, positional primitive and primitive array access to any of the four
 * resources mentioned at the package level.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public abstract class WritableBufferImpl extends BufferImpl implements WritableBuffer {

  //Pass-through ctor
  WritableBufferImpl(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  //BYTE BUFFER
  public static WritableBufferImpl writableWrap(final ByteBuffer byteBuf) {
    return writableWrap(byteBuf, byteBuf.order());
  }

  public static WritableBufferImpl writableWrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    final BaseWritableMemoryImpl wmem =
        BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, false, byteOrder);
    final WritableBufferImpl wbuf = wmem.asWritableBuffer(false, byteOrder);
    wbuf.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    return wbuf;
  }

  //MAP
  //Use WritableMemoryImpl for mapping files and then asWritableBuffer()

  //ALLOCATE DIRECT
  //Use WritableMemoryImpl to allocate direct memory and then asWritableBuffer().

  //DUPLICATES
  @Override
  public abstract WritableBufferImpl writableDuplicate();

  @Override
  public abstract WritableBufferImpl writableDuplicate(ByteOrder byteOrder);

  //REGIONS
  @Override
  public abstract WritableBufferImpl writableRegion();

  @Override
  public abstract WritableBufferImpl writableRegion(long offsetBytes, long capacityBytes,
      ByteOrder byteOrder);

  //AS MEMORY
  @Override
  public abstract WritableMemoryImpl asWritableMemory();

  //ACCESS PRIMITIVE HEAP ARRAYS for write
  //use WritableMemoryImpl and then asWritableBuffer().
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putX() and putXArray()
  @Override
  public abstract void putBoolean(boolean value);

  @Override
  public abstract void putBoolean(long offsetBytes, boolean value);

  @Override
  public abstract void putBooleanArray(boolean[] srcArray, int srcOffsetBooleans,
      int lengthBooleans);

  @Override
  public abstract void putByte(byte value);

  @Override
  public abstract void putByte(long offsetBytes, byte value);

  @Override
  public abstract void putByteArray(byte[] srcArray, int srcOffsetBytes, int lengthBytes);

  @Override
  public abstract void putChar(char value);

  @Override
  public abstract void putChar(long offsetBytes, char value);

  @Override
  public abstract void putCharArray(char[] srcArray, int srcOffsetChars, int lengthChars);

  @Override
  public abstract void putDouble(double value);

  @Override
  public abstract void putDouble(long offsetBytes, double value);

  @Override
  public abstract void putDoubleArray(double[] srcArray, int srcOffsetDoubles, int lengthDoubles);

  @Override
  public abstract void putFloat(float value);

  @Override
  public abstract void putFloat(long offsetBytes, float value);

  @Override
  public abstract void putFloatArray(float[] srcArray, int srcOffsetFloats, int lengthFloats);

  @Override
  public abstract void putInt(int value);

  @Override
  public abstract void putInt(long offsetBytes, int value);

  @Override
  public abstract void putIntArray(int[] srcArray, int srcOffsetInts, int lengthInts);

  @Override
  public abstract void putLong(long value);

  @Override
  public abstract void putLong(long offsetBytes, long value);

  @Override
  public abstract void putLongArray(long[] srcArray, int srcOffsetLongs, int lengthLongs);

  @Override
  public abstract void putShort(short value);

  @Override
  public abstract void putShort(long offsetBytes, short value);

  @Override
  public abstract void putShortArray(short[] srcArray, int srcOffsetShorts, int lengthShorts);

  //OTHER WRITE METHODS
  @Override
  public abstract Object getArray();

  @Override
  public abstract void clear();

  @Override
  public abstract void fill(byte value);

  //OTHER WRITABLE API METHODS
  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
  }

}
