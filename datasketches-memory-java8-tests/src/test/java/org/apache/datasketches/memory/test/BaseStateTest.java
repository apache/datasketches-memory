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

package org.apache.datasketches.memory.test;

import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.apache.datasketches.memory.internal.BaseStateImpl;
import org.apache.datasketches.memory.internal.Prim;
import org.apache.datasketches.memory.internal.StepBoolean;
import org.apache.datasketches.memory.internal.Util;
import org.testng.annotations.Test;

public class BaseStateTest {

  @Test
  public void checkPrimOffset() {
    int off = (int)Prim.BYTE.off();
    assertTrue(off > 0);
  }

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

  //StepBoolean checks
  @Test
  public void checkStepBoolean() {
    checkStepBoolean(true);
    checkStepBoolean(false);
  }

  private static void checkStepBoolean(boolean initialState) {
    StepBoolean step = new StepBoolean(initialState);
    assertTrue(step.get() == initialState); //confirm initialState
    step.change();
    assertTrue(step.hasChanged());      //1st change was successful
    assertTrue(step.get() != initialState); //confirm it is different from initialState
    step.change();
    assertTrue(step.get() != initialState); //Still different from initialState
    assertTrue(step.hasChanged());  //confirm it was changed from initialState value
  }

  @Test
  public void checkPrim() {
    assertEquals(Prim.DOUBLE.scale(), ARRAY_DOUBLE_INDEX_SCALE);
  }

  @Test
  public void checkGetNativeBaseOffset_Heap() throws Exception {
    WritableMemory wmem = WritableMemory.allocate(8); //heap
    final long offset = ReflectUtil.getNativeBaseOffset(wmem);
    assertEquals(offset, 0L);
  }

  @Test
  public void checkIsByteOrderCompatible() {
    WritableMemory wmem = WritableMemory.allocate(8);
    assertTrue(wmem.isByteOrderCompatible(ByteOrder.nativeOrder()));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkByteOrderNull() {
    Util.isNativeByteOrder(null);
    fail();
  }

  @Test
  public void checkIsNativeByteOrder() {
    assertTrue(BaseStateImpl.isNativeByteOrder(ByteOrder.nativeOrder()));
    try {
      BaseStateImpl.isNativeByteOrder(null);
      fail();
    } catch (final IllegalArgumentException e) {}
  }

  @Test
  public void checkXxHash64() {
    WritableMemory mem = WritableMemory.allocate(8);
    long out = mem.xxHash64(mem.getLong(0), 1L);
    assertTrue(out != 0);
  }

  @Test
  public void checkTypeDecode() {
    for (int i = 0; i < 128; i++) {
      BaseStateImpl.typeDecode(i);
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
