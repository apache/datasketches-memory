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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.DefaultMemoryFactory;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

public class ResourceImplTest {

  @Test
  public void checkIsSameResource() {
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(16);
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
    Memory mem = DefaultMemoryFactory.DEFAULT.wrap(arr);
    assertFalse(mem.equalTo(0, arr, 0, 8));
  }

  @Test
  public void checkGetNativeBaseOffset_Heap() throws Exception {
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(8); //heap
    final long offset = ((ResourceImpl)wmem).getNativeBaseOffset();
    assertEquals(offset, 0L);
  }

  @Test
  public void checkIsByteOrderCompatible() {
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(8);
    assertTrue(wmem.isByteOrderCompatible(ByteOrder.nativeOrder()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkByteOrderNull() {
    Util.isNativeByteOrder(null);
    fail();
  }

  @Test
  public void checkIsNativeByteOrder() {
    assertTrue(ResourceImpl.isNativeByteOrder(ByteOrder.nativeOrder()));
    try {
      ResourceImpl.isNativeByteOrder(null);
      fail();
    } catch (final IllegalArgumentException e) {}
  }

  @Test
  public void checkXxHash64() {
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(8);
    long out = mem.xxHash64(mem.getLong(0), 1L);
    assertTrue(out != 0);
  }

  @Test
  public void checkTypeDecode() {
    for (int i = 0; i < 128; i++) {
      ResourceImpl.typeDecode(i);
    }
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
