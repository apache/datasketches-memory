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

import static org.testng.Assert.fail;

import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class BufferBoundaryCheckTest {

  private final WritableMemory writableMemory = WritableMemory.allocate(8);

  @Test
  public void testGetByte() {
    writableMemory.getByte(7);
    try {
      writableMemory.getByte(8);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutByte() {
    writableMemory.putByte(7, (byte) 1);
    try {
      writableMemory.putByte(8, (byte) 1);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testGetChar() {
    writableMemory.getChar(6);
    try {
      writableMemory.getChar(7);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutChar() {
    writableMemory.putChar(6, 'a');
    try {
      writableMemory.putChar(7, 'a');
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testGetShort() {
    writableMemory.getShort(6);
    try {
      writableMemory.getShort(7);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutShort() {
    writableMemory.putShort(6, (short) 1);
    try {
      writableMemory.putShort(7, (short) 1);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testGetInt() {
    writableMemory.getInt(4);
    try {
      writableMemory.getInt(5);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutInt() {
    writableMemory.putInt(4, 1);
    try {
      writableMemory.putInt(5, 1);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testGetFloat() {
    writableMemory.getFloat(4);
    try {
      writableMemory.getFloat(5);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutFloat() {
    writableMemory.putFloat(4, 1f);
    try {
      writableMemory.putFloat(5, 1f);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testGetLong() {
    writableMemory.getLong(0);
    try {
      writableMemory.getLong(1);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutLong() {
    writableMemory.putLong(0, 1L);
    try {
      writableMemory.putLong(1, 1L);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testGetDouble() {
    writableMemory.getDouble(0);
    try {
      writableMemory.getDouble(1);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

  @Test
  public void testPutDouble() {
    writableMemory.putDouble(0, 1d);
    try {
      writableMemory.putDouble(1, 1d);
      fail("Expected IndexOutOfBoundsException");
    } catch (final IndexOutOfBoundsException expected) {
      // ignore
    }
  }

}
