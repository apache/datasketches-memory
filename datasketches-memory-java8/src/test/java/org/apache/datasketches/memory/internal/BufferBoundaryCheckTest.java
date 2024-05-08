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
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class BufferBoundaryCheckTest {

  private final WritableMemory writableMemory = WritableMemory.allocate(8);

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetBoolean() {
    writableMemory.getBoolean(8);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutBoolean() {
    writableMemory.putBoolean(8, true);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetByte() {
    writableMemory.getByte(8);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutByte() {
    writableMemory.putByte(8, (byte) 1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetChar() {
    writableMemory.getChar(7);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutChar() {
    writableMemory.putChar(7, 'a');
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetShort() {
    writableMemory.getShort(7);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutShort() {
    writableMemory.putShort(7, (short) 1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetInt() {
    writableMemory.getInt(5);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutInt() {
    writableMemory.putInt(5, 1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetFloat() {
    writableMemory.getFloat(5);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutFloat() {
    writableMemory.putFloat(5, 1f);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetLong() {
    writableMemory.getLong(1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutLong() {
    writableMemory.putLong(1, 1L);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testGetDouble() {
    writableMemory.getDouble(1);
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void testPutDouble() {
    writableMemory.putDouble(1, 1d);
  }

}
