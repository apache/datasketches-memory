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

import org.apache.datasketches.memory.MemoryBoundsException;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class MemoryBoundaryCheckTest {

  private final WritableBuffer writableBuffer = WritableMemory.allocate(8).asWritableBuffer();

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetBoolean() {
    writableBuffer.getBoolean(8);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutBoolean() {
    writableBuffer.putBoolean(8, true);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetByte() {
    writableBuffer.getByte(8);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutByte() {
    writableBuffer.putByte(8, (byte) 1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetChar() {
    writableBuffer.getChar(7);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutChar() {
    writableBuffer.putChar(7, 'a');
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetShort() {
    writableBuffer.getShort(7);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutShort() {
    writableBuffer.putShort(7, (short) 1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetInt() {
    writableBuffer.getInt(5);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutInt() {
    writableBuffer.putInt(5, 1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetFloat() {
    writableBuffer.getFloat(5);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutFloat() {
    writableBuffer.putFloat(5, 1f);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetLong() {
    writableBuffer.getLong(1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutLong() {
    writableBuffer.putLong(1, 1L);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetDouble() {
    writableBuffer.getDouble(1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutDouble() {
    writableBuffer.putDouble(1, 1d);
  }
}
