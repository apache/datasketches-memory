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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.internal.Util;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class WritableMemoryTest {

  @Test
  public void wrapBigEndian() {
    ByteBuffer bb = ByteBuffer.allocate(64); //big endian
    WritableMemory wmem = WritableMemory.writableWrap(bb);
    assertEquals(wmem.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void wrapBigEndianAsLittle() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    bb.putChar(0, (char)1); //as NNO
    WritableMemory wmem = WritableMemory.writableWrap(bb, ByteOrder.LITTLE_ENDIAN, null);
    assertEquals(wmem.getChar(0), 256);
  }

  @Test
  public void allocateWithByteOrder() {
    WritableMemory wmem = WritableMemory.allocate(64, ByteOrder.BIG_ENDIAN);
    assertEquals(wmem.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
    wmem = WritableMemory.allocate(64, ByteOrder.LITTLE_ENDIAN);
    assertEquals(wmem.getTypeByteOrder(), ByteOrder.LITTLE_ENDIAN);
  }

  @Test
  public void checkGetArray() {
    byte[] byteArr = new byte[64];
    WritableMemory wmem = WritableMemory.writableWrap(byteArr);
    assertTrue(wmem.getArray() == byteArr);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    assertTrue(wbuf.getArray() == byteArr);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkSelfArrayCopy() {
    byte[] srcAndDst = new byte[128];
    WritableMemory wmem = WritableMemory.writableWrap(srcAndDst);
    wmem.getByteArray(0, srcAndDst, 64, 64);  //non-overlapping
  }

  @Test
  public void checkEquals() {
    int len = 7;
    WritableMemory wmem1 = WritableMemory.allocate(len);
    //@SuppressWarnings({"EqualsWithItself", "SelfEquals"}) //unsupported
    //SelfEquals for Plexus, EqualsWithItself for IntelliJ
    //boolean eq1 = wmem1.equals(wmem1); //strict profile complains
    //assertTrue(eq1);

    WritableMemory wmem2 = WritableMemory.allocate(len + 1);
    assertFalse(wmem1.equals(wmem2));

    WritableMemory reg1 = wmem1.writableRegion(0, wmem1.getCapacity());
    assertTrue(wmem1.equals(reg1));

    wmem2 = WritableMemory.allocate(len);
    for (int i = 0; i < len; i++) {
      wmem1.putByte(i, (byte) i);
      wmem2.putByte(i, (byte) i);
    }
    assertTrue(wmem1.equals(wmem2));
    assertTrue(wmem1.equalTo(0, wmem1, 0, len));

    reg1 = wmem1.writableRegion(0, wmem1.getCapacity());
    assertTrue(wmem1.equalTo(0, reg1, 0, len));

    len = 24;
    wmem1 = WritableMemory.allocate(len);
    wmem2 = WritableMemory.allocate(len);
    for (int i = 0; i < len; i++) {
      wmem1.putByte(i, (byte) i);
      wmem2.putByte(i, (byte) i);
    }
    assertTrue(wmem1.equalTo(0, wmem2, 0, len - 1));
    assertTrue(wmem1.equalTo(0, wmem2, 0, len));
    wmem2.putByte(0, (byte) 10);
    assertFalse(wmem1.equalTo(0, wmem2, 0, len));
    wmem2.putByte(0, (byte) 0);
    wmem2.putByte(len - 2, (byte) 0);
    assertFalse(wmem1.equalTo(0, wmem2, 0, len - 1));
  }

  @Test
  public void checkEquals2() {
    int len = 23;
    WritableMemory wmem1 = WritableMemory.allocate(len);
    assertFalse(wmem1.equals(null));
    //@SuppressWarnings({"EqualsWithItself", "SelfEquals"}) //unsupported
    //SelfEquals for Plexus, EqualsWithItself for IntelliJ
    //boolean eq1 = wmem1.equals(wmem1); //strict profile complains
    //assertTrue(eq1);

    WritableMemory wmem2 = WritableMemory.allocate(len + 1);
    assertFalse(wmem1.equals(wmem2));

    for (int i = 0; i < len; i++) {
      wmem1.putByte(i, (byte) i);
      wmem2.putByte(i, (byte) i);
    }
    assertTrue(wmem1.equalTo(0, wmem2, 0, len));
    assertTrue(wmem1.equalTo(1, wmem2, 1, len - 1));
  }

  @Test
  public void checkLargeEquals() {
    // Size bigger than UNSAFE_COPY_MEMORY_THRESHOLD; size with "reminder" = 7, to test several
    // traits of the implementation
    final int thresh = Util.UNSAFE_COPY_THRESHOLD_BYTES;
    byte[] bytes1 = new byte[(thresh * 2) + 7];
    ThreadLocalRandom.current().nextBytes(bytes1);
    byte[] bytes2 = bytes1.clone();
    Memory mem1 = Memory.wrap(bytes1);
    Memory mem2 = Memory.wrap(bytes2);
    assertTrue(mem1.equals(mem2));

    bytes2[thresh + 10] = (byte) (bytes1[thresh + 10] + 1);
    assertFalse(mem1.equals(mem2));

    bytes2[thresh + 10] = bytes1[thresh + 10];
    bytes2[(thresh * 2) + 3] = (byte) (bytes1[(thresh * 2) + 3] + 1);
    assertFalse(mem1.equals(mem2));
  }

  @Test
  public void checkWrapWithBO() {
    WritableMemory wmem = WritableMemory.writableWrap(new byte[0], ByteOrder.BIG_ENDIAN);
    boolean nativeBO = wmem.getTypeByteOrder() == Util.nativeByteOrder;
    assertTrue(nativeBO); //remains true for ZeroSizeMemory
    println("" + nativeBO);
    wmem = WritableMemory.writableWrap(new byte[8], ByteOrder.BIG_ENDIAN);
    nativeBO = wmem.getTypeByteOrder() == Util.nativeByteOrder;
    assertFalse(nativeBO);
    println("" + nativeBO);
  }

  @Test
  @SuppressWarnings("unused")
  public void checkOwnerClientCase() {
    WritableMemory owner = WritableMemory.allocate(64);
    Memory client1 = owner; //Client1 cannot write (no API)
    owner.putInt(0, 1); //But owner can write
    ((WritableMemory)client1).putInt(0, 2); //Client1 can write, but with explicit effort.
    Memory client2 = owner.region(0, owner.getCapacity()); //client2 cannot write (no API)
    owner.putInt(0, 3); //But Owner should be able to write
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(final String s) {
    //System.out.println(s);
  }

}
