/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class BufferTest {


  @Test
  public void checkDirectRoundTrip() {
    int n = 1024; //longs
    try (WritableDirectHandle wh = WritableMemory.allocateDirect(n * 8)) {
      WritableMemory wmem = wh.get();
      WritableBuffer wbuf = wmem.asWritableBuffer();
      for (int i = 0; i < n; i++) wbuf.putLong(i);
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
    WritableBuffer wbuf = WritableBuffer.allocate(n * 8);
    for (int i = 0; i < n; i++) wbuf.putLong(i);
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
    WritableBuffer wbuf = WritableBuffer.wrap(arr);
    for (int i = 0; i < n; i++) wbuf.putLong(i);
    wbuf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = wbuf.getLong();
      assertEquals(v, i);
    }
    Buffer buf = Buffer.wrap(arr);
    buf.resetPosition();
    for (int i = 0; i < n; i++) {
      long v = buf.getLong();
      assertEquals(v, i);
    }
    // Check Zero length array wraps
    Buffer buffZeroLengthArrayWrap = Buffer.wrap(new byte[0]);
    assertEquals(buffZeroLengthArrayWrap.getCapacity(), 0);
    // check 0 length array wraps
    List<Buffer> buffersToCheck = Lists.newArrayList();
    buffersToCheck.add(WritableBuffer.allocate(0));
    buffersToCheck.add(WritableBuffer.wrap(ByteBuffer.allocate(0)));
    buffersToCheck.add(WritableBuffer.wrap(new boolean[0]));
    buffersToCheck.add(WritableBuffer.wrap(new byte[0]));
    buffersToCheck.add(WritableBuffer.wrap(new char[0]));
    buffersToCheck.add(WritableBuffer.wrap(new short[0]));
    buffersToCheck.add(WritableBuffer.wrap(new int[0]));
    buffersToCheck.add(WritableBuffer.wrap(new long[0]));
    buffersToCheck.add(WritableBuffer.wrap(new float[0]));
    buffersToCheck.add(WritableBuffer.wrap(new double[0]));
    buffersToCheck.add(Buffer.wrap(ByteBuffer.allocate(0)));
    buffersToCheck.add(Buffer.wrap(new boolean[0]));
    buffersToCheck.add(Buffer.wrap(new byte[0]));
    buffersToCheck.add(Buffer.wrap(new char[0]));
    buffersToCheck.add(Buffer.wrap(new short[0]));
    buffersToCheck.add(Buffer.wrap(new int[0]));
    buffersToCheck.add(Buffer.wrap(new long[0]));
    buffersToCheck.add(Buffer.wrap(new float[0]));
    buffersToCheck.add(Buffer.wrap(new double[0]));
    //Check the buffer lengths
    for (Buffer buffer : buffersToCheck) {
      assertEquals(buffer.getCapacity(), 0);
    }
  }

  @Test
  public void checkByteBufHeap() {
    int n = 1024; //longs
    byte[] arr = new byte[n * 8];
    ByteBuffer bb = ByteBuffer.wrap(arr);
    bb.order(ByteOrder.nativeOrder());

    WritableBuffer wbuf = WritableBuffer.wrap(bb);
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
    Buffer buf1 = Buffer.wrap(arr);
    for (int i = 0; i < n; i++) { //read from wrapped arr
      long v = buf1.getLong();
      assertEquals(v, i);
    }
    //convert to wbuf to RO
    Buffer buf = wbuf;
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

    WritableBuffer wbuf = WritableBuffer.wrap(bb);
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
    Buffer buf1 = Buffer.wrap(bb);
    for (int i = 0; i < n; i++) { //read from wrapped bb RO
      long v = buf1.getLong();
      assertEquals(v, i);
    }
    //convert to RO
    Buffer buf = wbuf;
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
    Buffer buf = Buffer.wrap(bb);
    assertTrue(buf.swapBytes());
    assertEquals(buf.getResourceOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkReadOnlyHeapByteBuffer() {
    ByteBuffer bb = ByteBuffer.allocate(128);
    bb.order(ByteOrder.nativeOrder());
    for (int i = 0; i < 128; i++) { bb.put(i, (byte)i); }

    bb.position(64);
    ByteBuffer slice = bb.slice().asReadOnlyBuffer();
    slice.order(ByteOrder.nativeOrder());

    Buffer buf = Buffer.wrap(slice);
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

    WritableBuffer wbuf = WritableBuffer.allocate(n * 8);
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

    Buffer buf = Buffer.wrap(arr);
    buf.setPosition(n2 * 8);
    Buffer reg = buf.region();
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
    WritableBuffer wbuf = WritableBuffer.wrap(arr);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getLong(), i); //write all
      //println("" + wmem.getLong(i * 8));
    }
    //println("");
    wbuf.setPosition(n2 * 8);
    WritableBuffer reg = wbuf.writableRegion();
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
    WritableDirectHandle wh = WritableMemory.allocateDirect(bytes);
    WritableMemory wmem = wh.get();
    WritableBuffer wbuf = wmem.asWritableBuffer();
    wh.close();
    //with -ea assert: Memory not valid.
    //with -da sometimes segfaults, sometimes passes!
    wbuf.getLong();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkRegionUseAfterFree() {
    int bytes = 64;
    @SuppressWarnings("resource") //intentionally not using try-with-resources here
    WritableDirectHandle wh = WritableMemory.allocateDirect(bytes);
    Memory wmem = wh.get();

    Buffer reg = wmem.asBuffer().region();
    wh.close();
    //with -ea assert: Memory not valid.
    //with -da sometimes segfaults, sometimes passes!
    reg.getByte();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void checkBaseBufferInvariants() {
    BaseBuffer bb = new BaseBuffer(new ResourceState());
    bb.setStartPositionEnd(1, 0, 2);
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
