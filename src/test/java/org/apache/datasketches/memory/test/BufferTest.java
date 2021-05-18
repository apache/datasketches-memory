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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.internal.BufferImpl;
import org.apache.datasketches.memory.internal.MemoryImpl;
import org.apache.datasketches.memory.internal.WritableBufferImpl;
import org.apache.datasketches.memory.internal.WritableMemoryImpl;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

@SuppressWarnings("javadoc")
public class BufferTest {

  @Test
  public void checkDirectRoundTrip() {
    int n = 1024; //longs
    try (WritableHandle wh = WritableMemoryImpl.allocateDirect(n * 8)) {
      WritableMemoryImpl wmem = wh.get();
      WritableBufferImpl wbuf = wmem.asWritableBuffer();
      for (int i = 0; i < n; i++) {
        wbuf.putLong(i);
      }
      wbuf.resetPosition();
      for (int i = 0; i < n; i++) {
        long v = wbuf.getLong();
        assertEquals(v, i);
      }
    }
  }

  @Test
  public void checkAutoHeapRoundTrip() {
    int n = 1024; //longs
    WritableBufferImpl wbuf = WritableMemoryImpl.allocate(n * 8).asWritableBuffer();
    for (int i = 0; i < n; i++) {
      wbuf.putLong(i);
    }
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = wbuf.getLong();
      assertEquals(v, i);
    }
  }

  @Test
  public void checkArrayWrap() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    WritableBufferImpl wbuf = WritableMemoryImpl.writableWrap(arr).asWritableBuffer();
    for (int i = 0; i < n; i++) {
      wbuf.putLong(i);
    }
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = wbuf.getLong();
      assertEquals(v, i);
    }
    BufferImpl buf = MemoryImpl.wrap(arr).asBuffer();
    buf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = buf.getLong();
      assertEquals(v, i);
    }
    // Check Zero length array wraps
    MemoryImpl mem = MemoryImpl.wrap(new byte[0]);
    BufferImpl buffZeroLengthArrayWrap = mem.asBuffer();
    assertEquals(buffZeroLengthArrayWrap.getCapacity(), 0);
    // check 0 length array wraps
    List<BufferImpl> buffersToCheck = Lists.newArrayList();
    buffersToCheck.add(WritableMemoryImpl.allocate(0).asBuffer());
    buffersToCheck.add(WritableBufferImpl.writableWrap(ByteBuffer.allocate(0)));
    buffersToCheck.add(BufferImpl.wrap(ByteBuffer.allocate(0)));
    buffersToCheck.add(MemoryImpl.wrap(new boolean[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new byte[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new char[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new short[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new int[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new long[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new float[0]).asBuffer());
    buffersToCheck.add(MemoryImpl.wrap(new double[0]).asBuffer());
    //Check the buffer lengths
    for (BufferImpl buffer : buffersToCheck) {
      assertEquals(buffer.getCapacity(), 0);
    }
  }

  @Test
  public void simpleBBTest() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    ByteBuffer bb = ByteBuffer.wrap(arr);
    bb.order(ByteOrder.nativeOrder());

    WritableBufferImpl wbuf = WritableBufferImpl.writableWrap(bb);
    for (int i = 0; i < n; i++) { //write to wbuf
      wbuf.putLong(i);
    }
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) { //read from wbuf
      long v = wbuf.getLong();
      assertEquals(v, i);
    }
    for (int i = 0; i < n; i++) { //read from BB
      long v = bb.getLong();
      assertEquals(v, i);
    }
  }

  @Test
  public void checkByteBufHeap() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    ByteBuffer bb = ByteBuffer.wrap(arr);
    bb.order(ByteOrder.nativeOrder());

    WritableBufferImpl wbuf = WritableBufferImpl.writableWrap(bb);
    for (int i = 0; i < n; i++) { //write to wbuf
      wbuf.putLong(i);
    }
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) { //read from wbuf
      long v = wbuf.getLong();
      assertEquals(v, i);
    }
    for (int i = 0; i < n; i++) { //read from BB
      long v = bb.getLong(i * 8);
      assertEquals(v, i);
    }
    BufferImpl buf1 = MemoryImpl.wrap(arr).asBuffer();
    for (int i = 0; i < n; i++) { //read from wrapped arr
      long v = buf1.getLong();
      assertEquals(v, i);
    }
    //convert to wbuf to RO
    BufferImpl buf = wbuf;
    buf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = buf.getLong();
      assertEquals(v, i);
    }
  }

  @Test
  public void checkByteBufDirect() {
    int n = 1024; //longs
    ByteBuffer bb = ByteBuffer.allocateDirect(n * 8);
    bb.order(ByteOrder.nativeOrder());

    WritableBufferImpl wbuf = WritableBufferImpl.writableWrap(bb);
    for (int i = 0; i < n; i++) { //write to wmem
      wbuf.putLong(i);
    }
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) { //read from wmem
      long v = wbuf.getLong();
      assertEquals(v, i);
    }
    for (int i = 0; i < n; i++) { //read from BB
      long v = bb.getLong(i * 8);
      assertEquals(v, i);
    }
    BufferImpl buf1 = BufferImpl.wrap(bb);
    for (int i = 0; i < n; i++) { //read from wrapped bb RO
      long v = buf1.getLong();
      assertEquals(v, i);
    }
    //convert to RO
    BufferImpl buf = wbuf;
    buf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = buf.getLong();
      assertEquals(v, i);
    }
  }

  @Test
  public void checkByteBufBigEndianOrder() {
    int n = 1024; //longs
    ByteBuffer bb = ByteBuffer.allocate(n * 8);
    bb.order(ByteOrder.BIG_ENDIAN);
    BufferImpl buf = BufferImpl.wrap(bb);
    assertEquals(buf.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkReadOnlyHeapByteBuffer() {
    ByteBuffer bb = ByteBuffer.allocate(128);
    bb.order(ByteOrder.nativeOrder());
    for (int i = 0; i < 128; i++) { bb.put(i, (byte)i); }

    bb.position(64);
    ByteBuffer slice = bb.slice().asReadOnlyBuffer();
    slice.order(ByteOrder.nativeOrder());

    BufferImpl buf = BufferImpl.wrap(slice);
    for (int i = 0; i < 64; i++) {
      assertEquals(buf.getByte(), 64 + i);
    }
    buf.toHexString("slice", 0, slice.capacity());
    //println(s);
  }

  @Test
  public void checkPutGetArraysHeap() {
    int n = 1024; //longs
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }

    WritableBufferImpl wbuf = WritableMemoryImpl.allocate(n * 8).asWritableBuffer();
    wbuf.putLongArray(arr, 0, n);
    long[] arr2 = new long[n];
    wbuf.resetPosition();
    wbuf.getLongArray(arr2, 0, n);
    for (int i = 0; i < n; i++) {
      assertEquals(arr2[i], i);
    }
  }

  @Test
  public void checkRORegions() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }

    BufferImpl buf = MemoryImpl.wrap(arr).asBuffer();
    buf.setPosition(n2 * 8);
    BufferImpl reg = buf.region();
    for (int i = 0; i < n2; i++) {
      long v = reg.getLong();
      assertEquals(v, i + n2);
      //println("" + v);
    }
  }

  @Test
  public void checkWRegions() {
    int n = 16;
    int n2 = n / 2;
    long[] arr = new long[n];
    for (int i = 0; i < n; i++) { arr[i] = i; }
    WritableBufferImpl wbuf = WritableMemoryImpl.writableWrap(arr).asWritableBuffer();
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getLong(), i); //write all
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    wbuf.setPosition(n2 * 8);
    WritableBufferImpl reg = wbuf.writableRegion();
    for (int i = 0; i < n2; i++) { reg.putLong(i); } //rewrite top half
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getLong(), i % 8);
      //println("" + wmem.getLong(i * 8));
    }
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkParentUseAfterFree() {
    int bytes = 64 * 8;
    @SuppressWarnings("resource") //intentionally not using try-with-resources here
    WritableHandle wh = WritableMemoryImpl.allocateDirect(bytes);
    WritableMemoryImpl wmem = wh.get();
    WritableBufferImpl wbuf = wmem.asWritableBuffer();
    wh.close();
    //with -ea assert: MemoryImpl not valid.
    //with -da sometimes segfaults, sometimes passes!
    wbuf.getLong();
  }

  @SuppressWarnings("resource")
  @Test(expectedExceptions = AssertionError.class)
  public void checkRegionUseAfterFree() {
    int bytes = 64;
    WritableHandle wh = WritableMemoryImpl.allocateDirect(bytes);
    MemoryImpl wmem = wh.get();

    BufferImpl reg = wmem.asBuffer().region();
    wh.close();
    //with -ea assert: MemoryImpl not valid.
    //with -da sometimes segfaults, sometimes passes!
    reg.getByte();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkBaseBufferInvariants() {
    WritableBufferImpl wbuf = WritableMemoryImpl.allocate(64).asWritableBuffer();
    wbuf.setStartPositionEnd(1, 0, 2); //out of order
  }


  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s String to print
   */
  static void println(final String s) {
    //System.out.println(s);
  }

}
