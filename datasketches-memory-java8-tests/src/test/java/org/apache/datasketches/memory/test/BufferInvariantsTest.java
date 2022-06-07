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
import static org.testng.Assert.fail;

import java.nio.ByteBuffer;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

/**
 * @author Lee Rhodes
 */
public class BufferInvariantsTest {
  private static final MemoryRequestServer memReqSvr = BaseState.defaultMemReqSvr;

  @Test
  public void testRegion() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(10);
    byteBuffer.limit(7);
    Buffer buff = Buffer.wrap(byteBuffer); //assuming buff has cap of 8
    assertEquals(buff.getCapacity(), 10); //wrong should be 8
    buff.getByte(); //pos moves to 1
    Buffer copyBuff = buff.region(); //pos: 0, start: 0, end: 6: cap: 7
    assertEquals(copyBuff.getEnd(), 6);
    assertEquals(copyBuff.getCapacity(), 6);
    assertEquals(copyBuff.getStart(), 0);
    assertEquals(copyBuff.getPosition(), 0);

    buff.setStartPositionEnd(1, 1, 5);
    buff.getByte();
    Buffer copyBuff2 = buff.region();
    assertEquals(copyBuff2.getEnd(), 3);
    assertEquals(copyBuff2.getCapacity(), 3);
    assertEquals(copyBuff2.getStart(), 0);
    assertEquals(copyBuff2.getPosition(), 0);
  }

  @Test
  public void testBB() {
    int n = 25;
    ByteBuffer bb = ByteBuffer.allocate(n);
    for (byte i = 0; i < n; i++) { bb.put(i, i); }
    assertEquals(bb.position(), 0);
    assertEquals(bb.limit(), n);
    assertEquals(bb.get(0), 0);
//    print("Orig : ");
//    printbb(bb);

    bb.limit(20);
    bb.position(5);
    assertEquals(bb.remaining(), 15);
//    print("Set  : ");
//    printbb(bb);

    ByteBuffer dup = bb.duplicate();
    assertEquals(dup.position(), 5);
    assertEquals(dup.limit(), 20);
    assertEquals(dup.capacity(), 25);
//    print("Dup  : ");
//    printbb(dup);

    ByteBuffer sl = bb.slice();
    assertEquals(sl.position(), 0);
    assertEquals(sl.limit(), 15);
    assertEquals(sl.capacity(), 15);
//    print("Slice: ");
//    printbb(sl);
  }

  @Test
  public void testBuf() {
    int n = 25;
    WritableBuffer buf = WritableMemory.allocate(n).asWritableBuffer();
    for (byte i = 0; i < n; i++) { buf.putByte(i); }
    buf.setPosition(0);
    assertEquals(buf.getPosition(), 0);
    assertEquals(buf.getEnd(), 25);
    assertEquals(buf.getCapacity(), 25);
//    print("Orig  : ");
//    printbuf(buf);

    buf.setStartPositionEnd(0, 5, 20);
    assertEquals(buf.getRemaining(), 15);
    assertEquals(buf.getCapacity(), 25);
    assertEquals(buf.getByte(), 5);
    buf.setPosition(5);
//    print("Set   : ");
//    printbuf(buf);

    Buffer dup = buf.duplicate();
    assertEquals(dup.getRemaining(), 15);
    assertEquals(dup.getCapacity(), 25);
    assertEquals(dup.getByte(), 5);
    dup.setPosition(5);
//    print("Dup   : ");
//    printbuf(dup);


    Buffer reg = buf.region();
    assertEquals(reg.getPosition(), 0);
    assertEquals(reg.getEnd(), 15);
    assertEquals(reg.getRemaining(), 15);
    assertEquals(reg.getCapacity(), 15);
    assertEquals(reg.getByte(), 5);
    reg.setPosition(0);
//    print("Region: ");
//    printbuf(reg);
  }

  @Test
  public void testBufWrap() {
    int n = 25;
    ByteBuffer bb = ByteBuffer.allocate(n);
    for (byte i = 0; i < n; i++) { bb.put(i, i); }

    bb.position(5);
    bb.limit(20);

    Buffer buf = Buffer.wrap(bb);
    assertEquals(buf.getPosition(), 5);
    assertEquals(buf.getEnd(), 20);
    assertEquals(buf.getRemaining(), 15);
    assertEquals(buf.getCapacity(), 25);
    assertEquals(buf.getByte(), 5);
    buf.setPosition(5);
//    print("Buf.wrap: ");
//    printbuf(buf);

    Buffer reg = buf.region();
    assertEquals(reg.getPosition(), 0);
    assertEquals(reg.getEnd(), 15);
    assertEquals(reg.getRemaining(), 15);
    assertEquals(reg.getCapacity(), 15);
    assertEquals(reg.getByte(), 5);
    reg.setPosition(0);
//    print("Buf.region: ");
//    printbuf(reg);
  }

  @Test
  public void checkLimitsDirect() throws Exception {
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory wmem = WritableMemory.allocateDirect(100, scope, memReqSvr);
      Buffer buf = wmem.asBuffer();
      buf.setStartPositionEnd(40, 45, 50);
      buf.setStartPositionEnd(0, 0, 100);
      try {
        buf.setStartPositionEnd(0, 0, 101);
        fail();
      } catch (AssertionError e) {
        //
      }
    }
  }

  @Test
  public void testRegionDirect() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(10);
    byteBuffer.limit(7);
    Buffer buff = Buffer.wrap(byteBuffer); //assuming buff has cap of 8
    assertEquals(buff.getCapacity(), 10); //wrong should be 8
    buff.getByte(); //pos moves to 1
    Buffer copyBuff = buff.region(); //pos: 0, start: 0, end: 6: cap: 7
    assertEquals(copyBuff.getEnd(), 6);
    assertEquals(copyBuff.getCapacity(), 6);
    assertEquals(copyBuff.getStart(), 0);
    assertEquals(copyBuff.getPosition(), 0);

    buff.setStartPositionEnd(1, 1, 5);
    buff.getByte();
    Buffer copyBuff2 = buff.region();
    assertEquals(copyBuff2.getEnd(), 3);
    assertEquals(copyBuff2.getCapacity(), 3);
    assertEquals(copyBuff2.getStart(), 0);
    assertEquals(copyBuff2.getPosition(), 0);
  }

  @Test
  public void testBBDirect() {
    int n = 25;
    ByteBuffer bb = ByteBuffer.allocateDirect(n);
    for (byte i = 0; i < n; i++) { bb.put(i, i); }
    assertEquals(bb.position(), 0);
    assertEquals(bb.limit(), n);
    assertEquals(bb.get(0), 0);
//    print("Orig : ");
//    printbb(bb);

    bb.limit(20);
    bb.position(5);
    assertEquals(bb.remaining(), 15);
//    print("Set  : ");
//    printbb(bb);

    ByteBuffer dup = bb.duplicate();
    assertEquals(dup.position(), 5);
    assertEquals(dup.limit(), 20);
    assertEquals(dup.capacity(), 25);
//    print("Dup  : ");
//    printbb(dup);

    ByteBuffer sl = bb.slice();
    assertEquals(sl.position(), 0);
    assertEquals(sl.limit(), 15);
    assertEquals(sl.capacity(), 15);
//    print("Slice: ");
//    printbb(sl);
  }

  @Test
  public void testBufDirect() throws Exception {
    int n = 25;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
    WritableMemory wmem = WritableMemory.allocateDirect(n, scope, memReqSvr);
    WritableBuffer buf = wmem.asWritableBuffer();
    for (byte i = 0; i < n; i++) { buf.putByte(i); }
    buf.setPosition(0);
    assertEquals(buf.getPosition(), 0);
    assertEquals(buf.getEnd(), 25);
    assertEquals(buf.getCapacity(), 25);
//    print("Orig  : ");
//    printbuf(buf);

    buf.setStartPositionEnd(0, 5, 20);
    assertEquals(buf.getRemaining(), 15);
    assertEquals(buf.getCapacity(), 25);
    assertEquals(buf.getByte(), 5);
    buf.setPosition(5);
//    print("Set   : ");
//    printbuf(buf);

    Buffer dup = buf.duplicate();
    assertEquals(dup.getRemaining(), 15);
    assertEquals(dup.getCapacity(), 25);
    assertEquals(dup.getByte(), 5);
    dup.setPosition(5);
//    print("Dup   : ");
//    printbuf(dup);


    Buffer reg = buf.region();
    assertEquals(reg.getPosition(), 0);
    assertEquals(reg.getEnd(), 15);
    assertEquals(reg.getRemaining(), 15);
    assertEquals(reg.getCapacity(), 15);
    assertEquals(reg.getByte(), 5);
    reg.setPosition(0);
//    print("Region: ");
//    printbuf(reg);
    }
  }

  @Test
  public void testBufWrapDirect() {
    int n = 25;
    ByteBuffer bb = ByteBuffer.allocateDirect(n);
    for (byte i = 0; i < n; i++) { bb.put(i, i); }

    bb.position(5);
    bb.limit(20);

    Buffer buf = Buffer.wrap(bb);
    assertEquals(buf.getPosition(), 5);
    assertEquals(buf.getEnd(), 20);
    assertEquals(buf.getRemaining(), 15);
    assertEquals(buf.getCapacity(), 25);
    assertEquals(buf.getByte(), 5);
    buf.setPosition(5);
//    print("Buf.wrap: ");
//    printbuf(buf);

    Buffer reg = buf.region();
    assertEquals(reg.getPosition(), 0);
    assertEquals(reg.getEnd(), 15);
    assertEquals(reg.getRemaining(), 15);
    assertEquals(reg.getCapacity(), 15);
    assertEquals(reg.getByte(), 5);
    reg.setPosition(0);
//    print("Buf.region: ");
//    printbuf(reg);
  }


  static void printbb(ByteBuffer bb) {
    println("pos: " + bb.position() + ", lim: " + bb.limit() + ", cap: " + bb.capacity());
    int rem = bb.remaining();
    int pos = bb.position();
    int i;
    for (i = 0; i < (rem-1); i++) {
      print(bb.get(i+ pos) + ", ");
    }
    println(bb.get(i + pos) + "\n");
  }

  static void printbuf(Buffer buf) {
    println("pos: " + buf.getPosition() + ", end: " + buf.getEnd() + ", cap: " + buf.getCapacity());
    long rem = buf.getRemaining();
    long pos = buf.getPosition();
    int i;
    for (i = 0; i < (rem-1); i++) {
      print(buf.getByte(i+ pos) + ", ");
    }
    println(buf.getByte(i + pos) + "\n");
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

  /**
   * @param s value to print
   */
  static void print(String s) {
    //System.out.print(s); //disable here
  }
}
