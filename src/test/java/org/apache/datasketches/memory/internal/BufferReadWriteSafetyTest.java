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

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class BufferReadWriteSafetyTest {

  // Test various operations with read-only Buffer

  private final WritableBuffer buf = (WritableBuffer) Buffer.wrap(ByteBuffer.allocate(8));


  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutByte() {
    buf.setPosition(0);
    buf.putByte(0, (byte) 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutBytePositional() {
    buf.setPosition(0);
    buf.putByte((byte) 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutShort() {
    buf.setPosition(0);
    buf.putShort(0, (short) 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutShortPositional() {
    buf.setPosition(0);
    buf.putShort((short) 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutChar() {
    buf.setPosition(0);
    buf.putChar(0, (char) 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutCharPositional() {
    buf.setPosition(0);
    buf.putChar((char) 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutInt() {
    buf.setPosition(0);
    buf.putInt(0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutIntPositional() {
    buf.setPosition(0);
    buf.putInt(1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutLong() {
    buf.setPosition(0);
    buf.putLong(0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutLongPositional() {
    buf.setPosition(0);
    buf.putLong(1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutFloat() {
    buf.setPosition(0);
    buf.putFloat(0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutFloatPositional() {
    buf.setPosition(0);
    buf.putFloat(1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutDouble() {
    buf.setPosition(0);
    buf.putDouble(0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutDoublePositional() {
    buf.setPosition(0);
    buf.putDouble(1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutByteArray() {
    buf.setPosition(0);
    buf.putByteArray(new byte[] {1}, 0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutShortArray() {
    buf.setPosition(0);
    buf.putShortArray(new short[] {1}, 0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutCharArray() {
    buf.setPosition(0);
    buf.putCharArray(new char[] {1}, 0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutIntArray() {
    buf.setPosition(0);
    buf.putIntArray(new int[] {1}, 0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutLongArray() {
    buf.setPosition(0);
    buf.putLongArray(new long[] {1}, 0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testPutFloatArray() {
    buf.setPosition(0);
    buf.putFloatArray(new float[] {1}, 0, 1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testDoubleByteArray() {
    buf.setPosition(0);
    buf.putDoubleArray(new double[] {1}, 0, 1);
  }

  // Now, test that various ways to obtain a read-only buffer produce a read-only buffer indeed

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testWritableMemoryAsBuffer() {
    WritableBuffer buf1 = (WritableBuffer) WritableMemory.allocate(8).asBuffer();
    buf1.putInt(1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testWritableBufferRegion() {
    WritableBuffer buf1 = (WritableBuffer) WritableMemory.allocate(8).asWritableBuffer().region();
    buf1.putInt(1);
  }

  @Test(expectedExceptions = { IllegalArgumentException.class, UnsupportedOperationException.class })
  public void testWritableBufferDuplicate() {
    WritableBuffer buf1 = (WritableBuffer) WritableMemory.allocate(8).asWritableBuffer().duplicate();
    buf1.putInt(1);
  }
}
