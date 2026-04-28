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

import static org.apache.datasketches.memory.internal.Util.NON_NATIVE_BYTE_ORDER;
import static org.apache.datasketches.memory.internal.Util.otherByteOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class LeafImplTest {
  private static final ByteOrder NBO = ByteOrder.nativeOrder();
  private static final ByteOrder NNBO = NON_NATIVE_BYTE_ORDER;
  private static final MemoryRequestServer dummyMemReqSvr = new DummyMemoryRequestServer();

  static class DummyMemoryRequestServer implements MemoryRequestServer {

    @Override
    public WritableMemory request(WritableMemory currentWMem, long capacityBytes) { return null; }

    @Override
    public void requestClose(WritableMemory memToClose, WritableMemory newMemory) { }
  }

  @Test
  public void checkDirectLeaves() throws Exception {
    long off = 0;
    long cap = 128;
    // Off Heap, Native order, No ByteBuffer, has MemReqSvr
    try (WritableMemory memNO = WritableMemory.allocateDirect(cap, NBO, dummyMemReqSvr)) {
      memNO.putShort(0, (short) 1);
      assertNull(((ResourceImpl)memNO).getUnsafeObject());
      assertTrue(memNO.isDirect());
      checkCombinations(memNO, off, cap, memNO.isDirect(), NBO, false, true);
    }
    // Off Heap, Non Native order, No ByteBuffer, has MemReqSvr
    try (WritableMemory memNNO = WritableMemory.allocateDirect(cap, NNBO, dummyMemReqSvr)) {
      memNNO.putShort(0, (short) 1);
      assertNull(((ResourceImpl)memNNO).getUnsafeObject());
      assertTrue(memNNO.isDirect());
      checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNBO, false, true);
    }
  }

  @Test
  public void checkByteBufferLeaves() {
    long off = 0;
    long cap = 128;
    //BB on heap, native order, has ByteBuffer, has MemReqSvr
    ByteBuffer bb = ByteBuffer.allocate((int)cap);
    bb.order(NBO);
    bb.putShort(0, (short) 1);
    WritableMemory mem = WritableMemory.writableWrap(bb, NBO, dummyMemReqSvr);
    assertEquals(bb.isDirect(), mem.isDirect());
    assertNotNull(((ResourceImpl)mem).getUnsafeObject());
    checkCombinations(mem, off, cap, mem.isDirect(), mem.getTypeByteOrder(), true, true);

    //BB off heap, native order, has ByteBuffer, has MemReqSvr
    ByteBuffer dbb = ByteBuffer.allocateDirect((int)cap);
    dbb.order(NBO);
    dbb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(dbb, NBO, dummyMemReqSvr);
    assertEquals(dbb.isDirect(), mem.isDirect());
    assertNull(((ResourceImpl)mem).getUnsafeObject());
    checkCombinations(mem, off, cap,  mem.isDirect(), mem.getTypeByteOrder(), true, true);

    //BB on heap, non native order, has ByteBuffer, has MemReqSvr
    bb = ByteBuffer.allocate((int)cap);
    bb.order(NNBO);
    bb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(bb, NNBO, dummyMemReqSvr);
    assertEquals(bb.isDirect(), mem.isDirect());
    assertNotNull(((ResourceImpl)mem).getUnsafeObject());
    checkCombinations(mem, off, cap, mem.isDirect(), mem.getTypeByteOrder(), true, true);

    //BB off heap, non native order, has ByteBuffer, has MemReqSvr
    dbb = ByteBuffer.allocateDirect((int)cap);
    dbb.order(NNBO);
    dbb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(dbb, NNBO, dummyMemReqSvr);
    assertEquals(dbb.isDirect(), mem.isDirect());
    assertNull(((ResourceImpl)mem).getUnsafeObject());
    checkCombinations(mem, off, cap,  mem.isDirect(), mem.getTypeByteOrder(), true, true);
  }

  @Test
  public void checkMapLeaves() throws Exception {
    long off = 0;
    long cap = 128;
    File file = new File("TestFile2.bin");
    if (file.exists()) {
      try {
        java.nio.file.Files.delete(file.toPath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    assertTrue(file.createNewFile());
    assertTrue(file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue(file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.
    // Off Heap, Native order, No ByteBuffer, No MemReqSvr
    try (WritableMemory memNO = WritableMemory.writableMap(file, off, cap, NBO)) {
      memNO.putShort(0, (short) 1);
      assertNull(((ResourceImpl)memNO).getUnsafeObject());
      assertTrue(memNO.isDirect());
      checkCombinations(memNO, off, cap, memNO.isDirect(), NBO, false, false);
    }
    // Off heap, Non Native order, No ByteBuffer, no MemReqSvr
    try (WritableMemory memNNO = WritableMemory.writableMap(file, off, cap, NNBO)) {
      memNNO.putShort(0, (short) 1);
      assertNull(((ResourceImpl)memNNO).getUnsafeObject());
      assertTrue(memNNO.isDirect());
      checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNBO, false, false);
    }
  }

  @Test
  public void checkHeapLeaves() {
    long off = 0;
    long cap = 128;
    // On Heap, Native order, No ByteBuffer, No MemReqSvr
    WritableMemory memNO = WritableMemory.allocate((int)cap); //assumes NBO
    memNO.putShort(0, (short) 1);
    assertNotNull(((ResourceImpl)memNO).getUnsafeObject());
    assertFalse(memNO.isDirect());
    checkCombinations(memNO, off, cap, memNO.isDirect(), NBO, false, false);
    // On Heap, Non-native order, No ByteBuffer, No MemReqSvr
    WritableMemory memNNO = WritableMemory.allocate((int)cap, NNBO);
    memNNO.putShort(0, (short) 1);
    assertNotNull(((ResourceImpl)memNNO).getUnsafeObject());
    assertFalse(memNNO.isDirect());
    checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNBO, false, false);
  }

  private static void checkCombinations(WritableMemory mem, long off, long cap,
      boolean direct, ByteOrder bo, boolean hasByteBuffer, boolean hasMemReqSvr) {
    ByteOrder oo = otherByteOrder(bo);

    assertEquals(mem.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(mem.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(mem.asWritableBuffer(bo).getShort(0), 1);
    assertEquals(mem.asWritableBuffer(oo).getShort(0), 256);
    assertEquals(mem.getRelativeOffset(), 0);

    ByteBuffer bb = ((ResourceImpl)mem).getByteBuffer();
    assertTrue( hasByteBuffer ? bb != null : bb == null);

    assertTrue(mem.getTypeByteOrder() == bo);

    if (hasMemReqSvr) { assertTrue(mem.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }
    else { assertNull(mem.getMemoryRequestServer()); }

    Object obj = ((ResourceImpl)mem).getUnsafeObject();
    if (direct) {
      assertTrue(mem.isDirect());
      assertNull(obj);
      assertTrue(((ResourceImpl)mem).getCumulativeOffset(0) != 0);
    } else {
      assertFalse(mem.isDirect());
      assertNotNull(obj);
      assertTrue(((ResourceImpl)mem).getCumulativeOffset(0) != 0);
    }

    assertTrue(mem.isAlive() == true);

    WritableBuffer buf = mem.asWritableBuffer();

    assertEquals(buf.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(buf.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(buf.writableDuplicate(bo).getShort(0), 1);
    assertEquals(buf.writableDuplicate(oo).getShort(0), 256);
    assertEquals(buf.getRelativeOffset(), 0);

    bb = ((ResourceImpl)buf).getByteBuffer();
    assertTrue(hasByteBuffer ? bb != null : bb == null);

    assertTrue(buf.getTypeByteOrder() == bo);

    if (hasMemReqSvr) { assertTrue(buf.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }
    else { assertNull(buf.getMemoryRequestServer()); }

    obj = ((ResourceImpl)buf).getUnsafeObject();
    if (direct) {
      assertTrue(buf.isDirect());
      assertNull(obj);
      assertTrue(((ResourceImpl)buf).getCumulativeOffset(0) != 0);
    } else {
      assertFalse(buf.isDirect());
      assertNotNull(obj);
      assertTrue(((ResourceImpl)buf).getCumulativeOffset(0) != 0);
    }

    assertTrue(buf.isAlive() == true);

    WritableMemory nnMem = mem.writableRegion(off, cap, oo);

    assertEquals(nnMem.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(nnMem.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(nnMem.asWritableBuffer(bo).getShort(0), 1);
    assertEquals(nnMem.asWritableBuffer(oo).getShort(0), 256);

    bb = ((ResourceImpl)nnMem).getByteBuffer();
    assertTrue( hasByteBuffer ? bb != null : bb == null);

    assertTrue(nnMem.getTypeByteOrder() == oo);

    if (hasMemReqSvr) { assertTrue(nnMem.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    obj = ((ResourceImpl)nnMem).getUnsafeObject();
    if (direct) {
      assertTrue(nnMem.isDirect());
      assertNull(obj);
      assertTrue(((ResourceImpl)nnMem).getCumulativeOffset(0) != 0);
    } else {
      assertFalse(nnMem.isDirect());
      assertNotNull(obj);
      assertTrue(((ResourceImpl)nnMem).getCumulativeOffset(0) != 0);
    }

    assertTrue(nnMem.isAlive() == true);

    WritableBuffer nnBuf = mem.asWritableBuffer(oo);

    assertEquals(nnBuf.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(nnBuf.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(nnBuf.writableDuplicate(bo).getShort(0), 1);
    assertEquals(nnBuf.writableDuplicate(oo).getShort(0), 256);

    bb = ((ResourceImpl)nnBuf).getByteBuffer();
    assertTrue( hasByteBuffer ? bb != null : bb == null);

    assertTrue(nnBuf.getTypeByteOrder() == oo);

    if (hasMemReqSvr) { assertTrue(nnBuf.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    obj = ((ResourceImpl)nnBuf).getUnsafeObject();
    if (direct) {
      assertTrue(nnBuf.isDirect());
      assertNull(obj);
      assertTrue(((ResourceImpl)nnBuf).getCumulativeOffset(0) != 0);
    } else {
      assertFalse(nnBuf.isDirect());
      assertNotNull(obj);
      assertTrue(((ResourceImpl)nnBuf).getCumulativeOffset(0) != 0);
    }

    assertTrue(nnBuf.isAlive() == true);
  }

}
