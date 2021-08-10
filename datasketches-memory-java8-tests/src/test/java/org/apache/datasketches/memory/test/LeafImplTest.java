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

import static org.apache.datasketches.memory.internal.Util.nativeByteOrder;
import static org.apache.datasketches.memory.internal.Util.nonNativeByteOrder;
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
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMapHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("javadoc")
public class LeafImplTest {
  private static final ByteOrder NO = nativeByteOrder;
  private static final ByteOrder NNO = nonNativeByteOrder;
  private static final MemoryRequestServer dummyMemReqSvr = new DummyMemoryRequestServer();

  private static ByteOrder otherOrder(ByteOrder order) { return (order == NO) ? NNO : NO; }

  static class DummyMemoryRequestServer implements MemoryRequestServer {
    @Override
    public WritableMemory request(long capacityBytes) { return null; }
    @Override
    public void requestClose(WritableMemory memToClose, WritableMemory newMemory) { }
  }

  @Test
  public void checkDirectLeafs() throws Exception {
    long off = 0;
    long cap = 128;
    // Off Heap, Native order, No ByteBuffer, has MemReqSvr
    try (WritableHandle wdh = WritableMemory.allocateDirect(cap, NO, dummyMemReqSvr)) {
      WritableMemory memNO = wdh.getWritable();
      memNO.putShort(0, (short) 1);
      assertNull(ReflectUtil.getUnsafeObject(memNO));
      assertTrue(memNO.isDirect());
      checkCombinations(memNO, off, cap, memNO.isDirect(), NO, false, true);
    }
    // Off Heap, Non Native order, No ByteBuffer, has MemReqSvr
    try (WritableHandle wdh = WritableMemory.allocateDirect(cap, NNO, dummyMemReqSvr)) {
      WritableMemory memNNO = wdh.getWritable();
      memNNO.putShort(0, (short) 1);
      assertNull(ReflectUtil.getUnsafeObject(memNNO));
      assertTrue(memNNO.isDirect());
      checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNO, false, true);
    }
  }

  @Test
  public void checkByteBufferLeafs() {
    long off = 0;
    long cap = 128;
    //BB on heap, native order, has ByteBuffer, has MemReqSvr
    ByteBuffer bb = ByteBuffer.allocate((int)cap);
    bb.order(NO);
    bb.putShort(0, (short) 1);
    WritableMemory mem = WritableMemory.writableWrap(bb, NO, dummyMemReqSvr);
    assertEquals(bb.isDirect(), mem.isDirect());
    assertNotNull(ReflectUtil.getUnsafeObject(mem));
    checkCombinations(mem, off, cap, mem.isDirect(), mem.getTypeByteOrder(), true, true);

    //BB off heap, native order, has ByteBuffer, has MemReqSvr
    ByteBuffer dbb = ByteBuffer.allocateDirect((int)cap);
    dbb.order(NO);
    dbb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(dbb, NO, dummyMemReqSvr);
    assertEquals(dbb.isDirect(), mem.isDirect());
    assertNull(ReflectUtil.getUnsafeObject(mem));
    checkCombinations(mem, off, cap,  mem.isDirect(), mem.getTypeByteOrder(), true, true);

    //BB on heap, non native order, has ByteBuffer, has MemReqSvr
    bb = ByteBuffer.allocate((int)cap);
    bb.order(NNO);
    bb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(bb, NNO, dummyMemReqSvr);
    assertEquals(bb.isDirect(), mem.isDirect());
    assertNotNull(ReflectUtil.getUnsafeObject(mem));
    checkCombinations(mem, off, cap, mem.isDirect(), mem.getTypeByteOrder(), true, true);

    //BB off heap, non native order, has ByteBuffer, has MemReqSvr
    dbb = ByteBuffer.allocateDirect((int)cap);
    dbb.order(NNO);
    dbb.putShort(0, (short) 1);
    mem = WritableMemory.writableWrap(dbb, NNO, dummyMemReqSvr);
    assertEquals(dbb.isDirect(), mem.isDirect());
    assertNull(ReflectUtil.getUnsafeObject(mem));
    checkCombinations(mem, off, cap,  mem.isDirect(), mem.getTypeByteOrder(), true, true);
  }

  @Test
  public void checkMapLeafs() throws Exception {
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
    try (WritableMapHandle wmh = WritableMemory.writableMap(file, off, cap, NO)) {
      WritableMemory memNO = wmh.getWritable();
      memNO.putShort(0, (short) 1);
      assertNull(ReflectUtil.getUnsafeObject(memNO));
      assertTrue(memNO.isDirect());
      checkCombinations(memNO, off, cap, memNO.isDirect(), NO, false, false);
    }
    // Off heap, Non Native order, No ByteBuffer, no MemReqSvr
    try (WritableMapHandle wmh = WritableMemory.writableMap(file, off, cap, NNO)) {
      WritableMemory memNNO = wmh.getWritable();
      memNNO.putShort(0, (short) 1);
      assertNull(ReflectUtil.getUnsafeObject(memNNO));
      assertTrue(memNNO.isDirect());
      checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNO, false, false);
    }
  }

  @Test
  public void checkHeapLeafs() {
    long off = 0;
    long cap = 128;
    // On Heap, Native order, No ByteBuffer, No MemReqSvr
    WritableMemory memNO = WritableMemory.allocate((int)cap); //assumes NO
    memNO.putShort(0, (short) 1);
    assertNotNull(ReflectUtil.getUnsafeObject(memNO));
    assertFalse(memNO.isDirect());
    checkCombinations(memNO, off, cap, memNO.isDirect(), NO, false, false);
    // On Heap, Non-native order, No ByteBuffer, No MemReqSvr
    WritableMemory memNNO = WritableMemory.allocate((int)cap, NNO);
    memNNO.putShort(0, (short) 1);
    assertNotNull(ReflectUtil.getUnsafeObject(memNNO));
    assertFalse(memNNO.isDirect());
    checkCombinations(memNNO, off, cap, memNNO.isDirect(), NNO, false, false);
  }

  private static void checkCombinations(WritableMemory mem, long off, long cap,
      boolean direct, ByteOrder bo, boolean hasByteBuffer, boolean hasMemReqSvr) {
    ByteOrder oo = otherOrder(bo);

    assertEquals(mem.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(mem.writableRegion(off, cap, oo).getShort(0), 256);

    assertEquals(mem.asWritableBuffer(bo).getShort(0), 1);
    assertEquals(mem.asWritableBuffer(oo).getShort(0), 256);

    ByteBuffer bb = mem.getByteBuffer();
    assertTrue( hasByteBuffer ? bb != null : bb == null);

    assertTrue(mem.getTypeByteOrder() == bo);

    if (hasMemReqSvr) { assertTrue(mem.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    Object obj = ReflectUtil.getUnsafeObject(mem);
    if (direct) {
      assertTrue(mem.isDirect());
      assertNull(obj);
    } else {
      assertFalse(mem.isDirect());
      assertNotNull(obj);
    }

    assertTrue(mem.isValid() == true);

    WritableBuffer buf = mem.asWritableBuffer();

    assertEquals(buf.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(buf.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(buf.writableDuplicate(bo).getShort(0), 1);
    assertEquals(buf.writableDuplicate(oo).getShort(0), 256);

    bb = buf.getByteBuffer();
    assertTrue(hasByteBuffer ? bb != null : bb == null);

    assertTrue(buf.getTypeByteOrder() == bo);

    if (hasMemReqSvr) { assertTrue(buf.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    obj = ReflectUtil.getUnsafeObject(buf);
    if (direct) {
      assertTrue(buf.isDirect());
      assertNull(obj);
    } else {
      assertFalse(buf.isDirect());
      assertNotNull(obj);
    }

    assertTrue(buf.isValid() == true);

    WritableMemory nnMem = mem.writableRegion(off, cap, oo);

    assertEquals(nnMem.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(nnMem.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(nnMem.asWritableBuffer(bo).getShort(0), 1);
    assertEquals(nnMem.asWritableBuffer(oo).getShort(0), 256);

    bb = nnMem.getByteBuffer();
    assertTrue( hasByteBuffer ? bb != null : bb == null);

    assertTrue(nnMem.getTypeByteOrder() == oo);

    if (hasMemReqSvr) { assertTrue(nnMem.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    obj = ReflectUtil.getUnsafeObject(nnMem);
    if (direct) {
      assertTrue(nnMem.isDirect());
      assertNull(obj);
    } else {
      assertFalse(nnMem.isDirect());
      assertNotNull(obj);
    }

    assertTrue(nnMem.isValid() == true);

    WritableBuffer nnBuf = mem.asWritableBuffer(oo);

    assertEquals(nnBuf.writableRegion(off, cap, bo).getShort(0), 1);
    assertEquals(nnBuf.writableRegion(off, cap, oo).getShort(0), 256);
    assertEquals(nnBuf.writableDuplicate(bo).getShort(0), 1);
    assertEquals(nnBuf.writableDuplicate(oo).getShort(0), 256);

    bb = nnBuf.getByteBuffer();
    assertTrue( hasByteBuffer ? bb != null : bb == null);

    assertTrue(nnBuf.getTypeByteOrder() == oo);

    if (hasMemReqSvr) { assertTrue(nnBuf.getMemoryRequestServer() instanceof DummyMemoryRequestServer); }

    obj = ReflectUtil.getUnsafeObject(nnBuf);
    if (direct) {
      assertTrue(nnBuf.isDirect());
      assertNull(obj);
    } else {
      assertFalse(nnBuf.isDirect());
      assertNotNull(obj);
    }

    assertTrue(nnBuf.isValid() == true);
  }

}
