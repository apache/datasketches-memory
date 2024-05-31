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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

/**
 * @author Lee Rhodes
 */
public class LeafImplTest {
  private static final ByteOrder NBO = ByteOrder.nativeOrder();
  private static final ByteOrder NNBO = BaseState.NON_NATIVE_BYTE_ORDER;
  private static final MemoryRequestServer dummyMemReqSvr = new DummyMemoryRequestServer();

  static class DummyMemoryRequestServer implements MemoryRequestServer {
    @Override
    public WritableMemory request(WritableMemory currentWMem, long capacityBytes) { return null; }
    @Override
    public void requestClose(WritableMemory memToClose, WritableMemory newMemory) { }
  }

  public static ByteOrder otherByteOrder(final ByteOrder order) {
    return (order == ByteOrder.nativeOrder()) ? NNBO : ByteOrder.nativeOrder();
  }

  @Test
  public void checkDirectLeafs() throws Exception {
    long off = 0;
    long cap = 128;
    // Off Heap, Native order, No ByteBuffer, has MemReqSvr
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory memNO = WritableMemory.allocateDirect(cap, 8, scope, NBO, dummyMemReqSvr);
      memNO.putShort(0, (short) 1);
      assertTrue(memNO.isDirect());
      checkCombinations(memNO, off, cap, memNO.isDirect(), NBO, false, true);
    }
    // Off Heap, Non Native order, No ByteBuffer, has MemReqSvr
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory memNNO = WritableMemory.allocateDirect(cap, 8, scope, NNBO, dummyMemReqSvr);
      memNNO.putShort(0, (short) 1);
      assertTrue(memNNO.isDirect());
      checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNBO, false, true);
    }
  }

  @Test
  public void checkByteBufferLeafs() {
    long off = 0;
    long cap = 128;
    //BB on heap, native order, has ByteBuffer, has MemReqSvr
    ByteBuffer bb = ByteBuffer.allocate((int)cap);
    bb.order(NBO);
    bb.putShort(0, (short) 1);
    WritableMemory mem = WritableMemory.writableWrap(bb, NBO, dummyMemReqSvr);
    assertEquals(bb.isDirect(), mem.isDirect());

    checkCombinations(mem, off, cap, mem.isDirect(), mem.getTypeByteOrder(), true, false);

    //BB off heap, native order, has ByteBuffer, has MemReqSvr
    ByteBuffer dbb = ByteBuffer.allocateDirect((int)cap);
    dbb.order(NBO);
    dbb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(dbb, NBO, dummyMemReqSvr);
    assertEquals(dbb.isDirect(), mem.isDirect());

    checkCombinations(mem, off, cap,  mem.isDirect(), mem.getTypeByteOrder(), true, false);

    //BB on heap, non native order, has ByteBuffer, has MemReqSvr
    bb = ByteBuffer.allocate((int)cap);
    bb.order(NNBO);
    bb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(bb, NNBO, dummyMemReqSvr);
    assertEquals(bb.isDirect(), mem.isDirect());

    checkCombinations(mem, off, cap, mem.isDirect(), mem.getTypeByteOrder(), true, false);

    //BB off heap, non native order, has ByteBuffer, has MemReqSvr
    dbb = ByteBuffer.allocateDirect((int)cap);
    dbb.order(NNBO);
    dbb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(dbb, NNBO, dummyMemReqSvr);
    assertEquals(dbb.isDirect(), mem.isDirect());

    checkCombinations(mem, off, cap,  mem.isDirect(), mem.getTypeByteOrder(), true, false);
  }

  @Test
  public void checkMapLeafs() throws IOException {
    long off = 0;
    long cap = 128;
    File file = new File("TestFile2.bin");
    if (file.exists()) {
      java.nio.file.Files.delete(file.toPath());
    }
    assertTrue(file.createNewFile());
    assertTrue(file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue(file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.
    // Off Heap, Native order, No ByteBuffer, No MemReqSvr
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory memNO = WritableMemory.writableMap(file, off, cap, scope, NBO);
      memNO.putShort(0, (short) 1);
      assertTrue(memNO.isDirect());
      checkCombinations(memNO, off, cap, memNO.isDirect(), NBO, false, false);
    }
    // Off heap, Non Native order, No ByteBuffer, no MemReqSvr
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory memNNO = WritableMemory.writableMap(file, off, cap, scope, NNBO);
      memNNO.putShort(0, (short) 1);
      assertTrue(memNNO.isDirect());
      checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNBO, false, false);
    }
  }

  @Test
  public void checkHeapLeafs() {
    long off = 0;
    long cap = 128;
    // On Heap, Native order, No ByteBuffer, MemReqSvr
    WritableMemory memNO = WritableMemory.allocate((int)cap, NBO, dummyMemReqSvr); //assumes NBO
    memNO.putShort(0, (short) 1);
    assertFalse(memNO.isDirect());
    checkCombinations(memNO, off, cap, memNO.isDirect(), NBO, false, true);

    // On Heap, Non-native order, No ByteBuffer, MemReqSvr
    WritableMemory memNNO = WritableMemory.allocate((int)cap, NNBO, dummyMemReqSvr);
    memNNO.putShort(0, (short) 1);
    assertFalse(memNNO.isDirect());
    checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNBO, false, true);
  }

  private static void checkCombinations(WritableMemory mem, long off, long cap,
      boolean direct, ByteOrder bo, boolean fromByteBuffer, boolean hasMemReqSvr) {
    ByteOrder oo = otherByteOrder(bo);

    //### Start with given mem
    assertEquals(mem.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(mem.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(mem.asWritableBuffer(bo).getShort(0), 1);
    assertEquals(mem.asWritableBuffer(oo).getShort(0), 256);
    assertTrue(mem.getTypeByteOrder() == bo);

    if (fromByteBuffer) { assertTrue(mem.hasByteBuffer()); }
    else { assertFalse(mem.hasByteBuffer()); }

    if (hasMemReqSvr) {
      assertTrue(mem.hasMemoryRequestServer());
      assertTrue(mem.getMemoryRequestServer() instanceof DummyMemoryRequestServer);
    }

    if (direct) {
      assertTrue(mem.isDirect());
    } else {
      assertFalse(mem.isDirect());
    }
    assertTrue(mem.isAlive() == true);

    //### Convert to writable buffer
    WritableBuffer buf = mem.asWritableBuffer();

    assertEquals(buf.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(buf.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(buf.writableDuplicate(bo).getShort(0), 1);
    assertEquals(buf.writableDuplicate(oo).getShort(0), 256);
    assertTrue(buf.getTypeByteOrder() == bo);

    if (fromByteBuffer) { assertTrue(buf.hasByteBuffer()); }

    if (hasMemReqSvr) {
      assertTrue(buf.hasMemoryRequestServer());
      assertTrue(buf.getMemoryRequestServer() instanceof DummyMemoryRequestServer);
    }

    if (direct) {
      assertTrue(buf.isDirect());
    } else {
      assertFalse(buf.isDirect());
    }
    assertTrue(buf.isAlive() == true);

    //### Convert to non native writable Region
    WritableMemory nnMem = mem.writableRegion(off, cap, oo);

    assertEquals(nnMem.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(nnMem.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(nnMem.asWritableBuffer(bo).getShort(0), 1);
    assertEquals(nnMem.asWritableBuffer(oo).getShort(0), 256);

    assertTrue(nnMem.getTypeByteOrder() == oo);

    if (fromByteBuffer) { assertTrue(nnMem.hasByteBuffer()); }

    if (hasMemReqSvr) {
      assertTrue(nnMem.hasMemoryRequestServer());
      assertTrue(nnMem.getMemoryRequestServer() instanceof DummyMemoryRequestServer);
    }

    if (direct) {
      assertTrue(nnMem.isDirect());
    } else {
      assertFalse(nnMem.isDirect());
    }
    assertTrue(nnMem.isAlive() == true);

    //### Convert to non native buffer
    WritableBuffer nnBuf = mem.asWritableBuffer(oo);

    assertEquals(nnBuf.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(nnBuf.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(nnBuf.writableDuplicate(bo).getShort(0), 1);
    assertEquals(nnBuf.writableDuplicate(oo).getShort(0), 256);

    assertTrue(nnBuf.getTypeByteOrder() == oo);

    if (fromByteBuffer) { assertTrue(nnBuf.hasByteBuffer()); }

    if (hasMemReqSvr) { assertTrue(nnBuf.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    if (direct) {
      assertTrue(nnBuf.isDirect());
    } else {
      assertFalse(nnBuf.isDirect());
    }
    assertTrue(nnBuf.isAlive() == true);
  }

}
