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

package org.apache.datasketches.memory;

import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class MemoryBoundaryCheckTest {

  private final WritableBuffer writableBuffer = WritableMemory.allocate(8).asWritableBuffer();

  @Test
  public void testGetBoolean() {
    writableBuffer.getBoolean(7);
    try {
      writableBuffer.getBoolean(8);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutBoolean() {
    writableBuffer.putBoolean(7, true);
    try {
      writableBuffer.putBoolean(8, true);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetByte() {
    writableBuffer.getByte(7);
    try {
      writableBuffer.getByte(8);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutByte() {
    writableBuffer.putByte(7, (byte) 1);
    try {
      writableBuffer.putByte(8, (byte) 1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetChar() {
    writableBuffer.getChar(6);
    try {
      writableBuffer.getChar(7);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutChar() {
    writableBuffer.putChar(6, 'a');
    try {
      writableBuffer.putChar(7, 'a');
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetShort() {
    writableBuffer.getShort(6);
    try {
      writableBuffer.getShort(7);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutShort() {
    writableBuffer.putShort(6, (short) 1);
    try {
      writableBuffer.putShort(7, (short) 1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetInt() {
    writableBuffer.getInt(4);
    try {
      writableBuffer.getInt(5);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutInt() {
    writableBuffer.putInt(4, 1);
    try {
      writableBuffer.putInt(5, 1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetFloat() {
    writableBuffer.getFloat(4);
    try {
      writableBuffer.getFloat(5);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutFloat() {
    writableBuffer.putFloat(4, 1f);
    try {
      writableBuffer.putFloat(5, 1f);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetLong() {
    writableBuffer.getLong(0);
    try {
      writableBuffer.getLong(1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutLong() {
    writableBuffer.putLong(0, 1L);
    try {
      writableBuffer.putLong(1, 1L);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testGetDouble() {
    writableBuffer.getDouble(0);
    try {
      writableBuffer.getDouble(1);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }

  @Test
  public void testPutDouble() {
    writableBuffer.putDouble(0, 1d);
    try {
      writableBuffer.putDouble(1, 1d);
      throw new RuntimeException("Expected AssertionError");
    } catch (final AssertionError expected) {
      // ignore
    }
  }
}
