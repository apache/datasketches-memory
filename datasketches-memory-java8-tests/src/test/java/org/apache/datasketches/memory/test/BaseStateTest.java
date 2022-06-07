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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class BaseStateTest {

  @Test
  public void checkIsSameResource() {
    WritableMemory wmem = WritableMemory.allocate(16);
    Memory mem = wmem;
    assertFalse(wmem.isSameResource(null));
    assertTrue(wmem.isSameResource(mem));

    WritableBuffer wbuf = wmem.asWritableBuffer();
    Buffer buf = wbuf;
    assertFalse(wbuf.isSameResource(null));
    assertTrue(wbuf.isSameResource(buf));
  }

  @Test
  public void checkNotEqualTo() {
    byte[] arr = new byte[8];
    Memory mem = Memory.wrap(arr);
    assertFalse(mem.equalTo(0, arr, 0, 8));
  }

  @Test
  public void checkIsByteOrderCompatible() {
    WritableMemory wmem = WritableMemory.allocate(8);
    assertTrue(wmem.isByteOrderCompatible(ByteOrder.nativeOrder()));
  }

  @Test
  public void checkXxHash64() {
    WritableMemory mem = WritableMemory.allocate(8);
    long out = mem.xxHash64(mem.getLong(0), 1L);
    assertTrue(out != 0);
  }

  @Test
  public void checkTypeDecode() {
    for (int i = 0; i < 256; i++) {
      String str = BaseStateImpl.typeDecode(i);
      println(i + "\t" + str);
    }
  }

  @Test
  public void checkToHexString() {
    WritableMemory mem = WritableMemory.writableWrap(new byte[16]);
    println(mem.toHexString("baseMem", 0, 16, true));
    for (int i = 0; i < 16; i++) { mem.putByte(i, (byte)i); }
    Buffer buf = mem.asBuffer();
    println(buf.toHexString("buffer", 0, 16, true));
  }

  @Test
  public void checkToMemorySegment() {
    WritableMemory mem = WritableMemory.allocate(8);
    mem.toMemorySegment();
    mem.asByteBufferView(ByteOrder.nativeOrder());
  }

  /********************/
  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

}
