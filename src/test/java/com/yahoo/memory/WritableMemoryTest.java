/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.CompareAndCopy.UNSAFE_COPY_THRESHOLD;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

import org.testng.annotations.Test;

public class WritableMemoryTest {

  @Test
  public void wrapBigEndian() {
    ByteBuffer bb = ByteBuffer.allocate(64); //big endian
    WritableMemory wmem = WritableMemory.wrap(bb);
    assertTrue(wmem.isSwapBytes());
    assertEquals(wmem.getResourceOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void wrapBigEndian2() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableBuffer wbuf = WritableBuffer.wrap(bb);
    assertTrue(wbuf.isSwapBytes());
    assertEquals(wbuf.getResourceOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkGetArray() {
    byte[] byteArr = new byte[64];
    WritableMemory wmem = WritableMemory.wrap(byteArr);
    assertTrue(wmem.getArray() == byteArr);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    assertTrue(wbuf.getArray() == byteArr);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkSelfArrayCopy() {
    byte[] srcAndDst = new byte[128];
    WritableMemory wmem = WritableMemory.wrap(srcAndDst);
    wmem.getByteArray(0, srcAndDst, 64, 64);  //non-overlapping
  }

  @Test
  public void checkEquals() {
    int len = 7;
    WritableMemory wmem1 = WritableMemory.allocate(len);
    //assertTrue(wmem1.equals(wmem1)); //intentionally ignoring this check

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
    wmem2.putByte(0,  (byte) 0);
    wmem2.putByte(len - 2, (byte) 0);
    assertFalse(wmem1.equalTo(0, wmem2, 0, len - 1));
  }

  @Test
  public void checkEquals2() {
    int len = 23;
    WritableMemory wmem1 = WritableMemory.allocate(len);
    assertFalse(wmem1.equals(null));
    //assertTrue(wmem1.equals(wmem1)); //intentionally ignoring this check

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
    final int thresh = UNSAFE_COPY_THRESHOLD;
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
    WritableMemory wmem = WritableMemory.wrap(new byte[0], ByteOrder.BIG_ENDIAN);
    assertFalse(wmem.isSwapBytes());
    println("" + wmem.isSwapBytes());
    wmem = WritableMemory.wrap(new byte[8], ByteOrder.BIG_ENDIAN);
    assertTrue(wmem.isSwapBytes());
    println("" + wmem.isSwapBytes());
  }

  @Test
  @SuppressWarnings("unused")
  public void checkOwnerClientCase() {
    WritableMemory owner = WritableMemory.allocate(64);
    Memory client1 = owner; //Client1 cannot write (no API)
    owner.putInt(0, 1); //But owner can write
    ((WritableMemory)client1).putInt(0, 2); //Client1 can write, but with explicit effort.
    Memory client2 = owner.region(0, owner.getCapacity()); //client2 cannot write (no API)
    owner.putInt(0,  3); //But Owner should be able to write
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
