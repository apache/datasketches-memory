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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

public class Buffer2Test {

  @Test
  public void testWrapByteBuf() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(0);

    Buffer buffer = Buffer.wrap(bb.asReadOnlyBuffer().order(ByteOrder.nativeOrder()));
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }
    assertEquals(true, buffer.hasByteBuffer());
  }

  @Test
  public void testWrapDirectBB() {
    ByteBuffer bb = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(0);

    Buffer buffer = Buffer.wrap(bb);
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }

    assertEquals(true, buffer.hasByteBuffer());
  }

  @Test
  public void testWrapByteArray() {
    byte[] byteArray = new byte[64];

    for (byte i = 0; i < 64; i++) {
      byteArray[i] = i;
    }

    Buffer buffer = Memory.wrap(byteArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(byteArray[i++], buffer.getByte());
    }

    buffer.setPosition(0);
    byte[] copyByteArray = new byte[64];
    buffer.getByteArray(copyByteArray, 0, 64);
    assertEquals(byteArray, copyByteArray);

    assertEquals(false, buffer.hasByteBuffer());
  }

  @Test
  public void testWrapCharArray() {
    char[] charArray = new char[64];

    for (char i = 0; i < 64; i++) {
      charArray[i] = i;
    }

    Buffer buffer = Memory.wrap(charArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(charArray[i++], buffer.getChar());
    }

    buffer.setPosition(0);
    char[] copyCharArray = new char[64];
    buffer.getCharArray(copyCharArray, 0, 64);
    assertEquals(charArray, copyCharArray);
  }

  @Test
  public void testWrapShortArray() {
    short[] shortArray = new short[64];

    for (short i = 0; i < 64; i++) {
      shortArray[i] = i;
    }

    Buffer buffer = Memory.wrap(shortArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(shortArray[i++], buffer.getShort());
    }

    buffer.setPosition(0);
    short[] copyShortArray = new short[64];
    buffer.getShortArray(copyShortArray, 0, 64);
    assertEquals(shortArray, copyShortArray);
  }

  @Test
  public void testWrapIntArray() {
    int[] intArray = new int[64];

    for (int i = 0; i < 64; i++) {
      intArray[i] = i;
    }

    Buffer buffer = Memory.wrap(intArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(intArray[i++], buffer.getInt());
    }

    buffer.setPosition(0);
    int[] copyIntArray = new int[64];
    buffer.getIntArray(copyIntArray, 0, 64);
    assertEquals(intArray, copyIntArray);
  }

  @Test
  public void testWrapLongArray() {
    long[] longArray = new long[64];

    for (int i = 0; i < 64; i++) {
      longArray[i] = i;
    }

    Buffer buffer = Memory.wrap(longArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(longArray[i++], buffer.getLong());
    }

    buffer.setPosition(0);
    long[] copyLongArray = new long[64];
    buffer.getLongArray(copyLongArray, 0, 64);
    assertEquals(longArray, copyLongArray);
  }

  @Test
  public void testWrapFloatArray() {
    float[] floatArray = new float[64];

    for (int i = 0; i < 64; i++) {
      floatArray[i] = i;
    }

    Buffer buffer = Memory.wrap(floatArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(floatArray[i++], buffer.getFloat());
    }

    buffer.setPosition(0);
    float[] copyFloatArray = new float[64];
    buffer.getFloatArray(copyFloatArray, 0, 64);
    assertEquals(floatArray, copyFloatArray);
  }

  @Test
  public void testWrapDoubleArray() {
    double[] doubleArray = new double[64];

    for (int i = 0; i < 64; i++) {
      doubleArray[i] = i;
    }

    Buffer buffer = Memory.wrap(doubleArray).asBuffer();
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(doubleArray[i++], buffer.getDouble());
    }

    buffer.setPosition(0);
    double[] copyDoubleArray = new double[64];
    buffer.getDoubleArray(copyDoubleArray, 0, 64);
    assertEquals(doubleArray, copyDoubleArray);
  }

  @Test
  public void testByteBufferPositionPreservation() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }
  }

  @Test
  public void testGetAndHasRemaining() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    assertEquals(bb.hasRemaining(), buffer.hasRemaining());
    assertEquals(bb.remaining(), buffer.getRemaining());
  }

  @Test
  public void testGetSetIncResetPosition() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    assertEquals(bb.position(), buffer.getPosition());
    assertEquals(30, buffer.setPosition(30).getPosition());
    assertEquals(40, buffer.incrementPosition(10).getPosition());
    assertEquals(0, buffer.resetPosition().getPosition());
  }

  @Test
  public void testByteBufferSlice() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb.slice().order(ByteOrder.nativeOrder()));
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }

    assertEquals(bb.position(), buffer.getPosition() + 10);
    assertEquals(30, buffer.setPosition(30).getPosition());
    assertEquals(40, buffer.incrementPosition(10).getPosition());
    assertEquals(0, buffer.resetPosition().getPosition());
  }

  @Test
  public void testDuplicateAndRegion() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb.slice().order(ByteOrder.nativeOrder())); //slice = 54
    buffer.setPosition(30);//remaining = 24
    Buffer dupBuffer = buffer.duplicate(); //all 54
    Buffer regionBuffer = buffer.region(); //24

    assertEquals(dupBuffer.getStart(), buffer.getStart());
    assertEquals(regionBuffer.getStart(), buffer.getStart());
    assertEquals(dupBuffer.getEnd(), buffer.getEnd());
    assertEquals(regionBuffer.getEnd(), buffer.getRemaining());
    assertEquals(dupBuffer.getPosition(), buffer.getPosition());
    assertEquals(regionBuffer.getPosition(), 0);
    assertEquals(dupBuffer.getCapacity(), buffer.getCapacity());
    assertEquals(regionBuffer.getCapacity(), buffer.getCapacity() - 30);
  }

  @Test
  public void checkRORegions() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    Memory mem = Memory.wrap(arr);
    Buffer buf = mem.asBuffer();
    Buffer reg = buf.region(n2 * 8, n2 * 8, buf.getTypeByteOrder()); //top half
    for (int i = 0; i < n2; i++) {
      long v = reg.getLong(i * 8);
      long e = i + n2;
      assertEquals(v, e);
    }
  }

  @Test
  public void testAsMemory() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    Memory memory = buffer.asMemory();

    assertEquals(buffer.getCapacity(), memory.getCapacity());

    while(buffer.hasRemaining()){
      assertEquals(memory.getByte(buffer.getPosition()), buffer.getByte());
    }
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testROByteBuffer() {
    byte[] arr = new byte[64];
    ByteBuffer roBB = ByteBuffer.wrap(arr).asReadOnlyBuffer();
    Buffer buf = Buffer.wrap(roBB);
    WritableBuffer wbuf = (WritableBuffer) buf;
    wbuf.putByte(0, (byte) 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testROByteBuffer2() {
    byte[] arr = new byte[64];
    ByteBuffer roBB = ByteBuffer.wrap(arr).asReadOnlyBuffer();
    Buffer buf = Buffer.wrap(roBB);
    WritableBuffer wbuf = (WritableBuffer) buf;
    wbuf.putByteArray(arr, 0, 64);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testIllegalFill() {
    byte[] arr = new byte[64];
    ByteBuffer roBB = ByteBuffer.wrap(arr).asReadOnlyBuffer();
    Buffer buf = Buffer.wrap(roBB);
    WritableBuffer wbuf = (WritableBuffer) buf;
    wbuf.fill((byte)0);
  }

  @Test
  public void checkWritableWrap() {
    ByteBuffer bb = ByteBuffer.allocate(16);
    WritableBuffer buf = WritableBuffer.writableWrap(bb, ByteOrder.nativeOrder(), BaseState.defaultMemReqSvr);
    assertNotNull(buf);
  }

  @Test
  public void testWritableDuplicate() {
    WritableMemory wmem = WritableMemory.writableWrap(new byte[1]);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    WritableBuffer wbuf2 = wbuf.writableDuplicate();
    assertEquals(wbuf2.getCapacity(), 1);
    Buffer buf = wmem.asBuffer();
    assertEquals(buf.getCapacity(), 1);
  }

  @Test
  public void checkIndependence() {
    int cap = 64;
    ResourceScope scope = ResourceScope.newImplicitScope();
    WritableMemory wmem = WritableMemory.allocateDirect(cap, 8, scope, ByteOrder.nativeOrder(), null);
    WritableBuffer wbuf1 = wmem.asWritableBuffer();
    WritableBuffer wbuf2 = wmem.asWritableBuffer();
    assertFalse(wbuf1 == wbuf2);
    assertTrue(wbuf1.nativeOverlap(wbuf2) == cap);

    WritableMemory reg1 = wmem.writableRegion(0, cap);
    WritableMemory reg2 = wmem.writableRegion(0, cap);
    assertFalse(reg1 == reg2);
    assertTrue(reg1.nativeOverlap(reg2) == cap);


    WritableBuffer wbuf3 = wbuf1.writableRegion();
    WritableBuffer wbuf4 = wbuf1.writableRegion();
    assertFalse(wbuf3 == wbuf4);
    assertTrue(wbuf3.nativeOverlap(wbuf4) == cap);
  }

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
