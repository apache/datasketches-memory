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
import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

/**
 * @author Lee Rhodes
 */
public class SpecificLeafTest {
  private static final MemoryRequestServer memReqSvr = BaseState.defaultMemReqSvr;

  @Test
  public void checkByteBufferLeafs() {
    int bytes = 128;
    ByteBuffer bb = ByteBuffer.allocate(bytes);
    bb.order(ByteOrder.nativeOrder());

    Memory mem = Memory.wrap(bb).region(0, bytes, ByteOrder.nativeOrder());
    assertTrue(mem.hasByteBuffer());
    assertTrue(mem.isReadOnly());
    assertTrue(mem.isMemory());
    assertFalse(mem.isDirect());
    assertFalse(mem.isMapped());
    checkCrossLeafTypeIds(mem);
    Buffer buf = mem.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
    assertEquals(buf.getTypeByteOrder(), BaseState.NATIVE_BYTE_ORDER);

    bb.order(BaseState.NON_NATIVE_BYTE_ORDER);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
    Buffer buf3 = buf2.duplicate();

    assertTrue(mem.isRegion());
    assertTrue(mem2.isRegion());
    assertTrue(buf.isRegion());
    assertTrue(buf2.isRegion());
    assertTrue(buf3.isDuplicate());
  }

  @Test
  public void checkDirectLeafs() throws Exception {
    int bytes = 128;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory wmem = WritableMemory.allocateDirect(bytes, 1, scope, ByteOrder.nativeOrder(), memReqSvr);
      assertFalse(((BaseStateImpl)wmem).isReadOnly());
      assertTrue(wmem.isDirect());
      assertFalse(wmem.isHeap());
      assertFalse(wmem.isReadOnly());
      checkCrossLeafTypeIds(wmem);
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);

      Memory mem = wmem.region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf = mem.asBuffer().region(0, bytes, ByteOrder.nativeOrder());


      Memory mem2 = nnwmem.region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
      Buffer buf3 = buf2.duplicate();

      assertTrue(mem.isRegion());
      assertTrue(mem2.isRegion());
      assertTrue(buf.isRegion());
      assertTrue(buf2.isRegion());
      assertTrue(buf3.isDuplicate());
      assertTrue(mem2.isMemory());
    }
  }

  @Test
  public void checkMapLeafs() throws IOException {
    File file = new File("TestFile2.bin");
    if (file.exists()) {
      java.nio.file.Files.delete(file.toPath());
    }
    assertTrue(file.createNewFile());
    assertTrue(file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue(file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.

    final long bytes = 128;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      WritableMemory mem = WritableMemory.writableMap(file, 0L, bytes, scope, ByteOrder.nativeOrder());
      assertTrue(mem.isMapped());
      assertFalse(mem.isReadOnly());
      checkCrossLeafTypeIds(mem);
      Memory nnreg = mem.region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);

      Memory reg = mem.region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf = reg.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf4 = buf.duplicate();

      Memory reg2 = nnreg.region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
      Buffer buf2 = reg2.asBuffer().region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
      Buffer buf3 = buf2.duplicate();

      assertTrue(reg.isRegion());
      assertTrue(reg2.isRegion());
      assertEquals(reg2.getTypeByteOrder(), BaseState.NON_NATIVE_BYTE_ORDER);
      assertTrue(buf.isRegion());
      assertFalse(buf.isMemory());
      assertTrue(buf2.isRegion());
      assertTrue(buf3.isDuplicate());
      assertTrue(buf4.isDuplicate());
    }
  }

  @Test
  public void checkHeapLeafs() {
    int bytes = 128;
    Memory mem = Memory.wrap(new byte[bytes]);
    assertTrue(mem.isHeap());
    assertTrue(((BaseStateImpl)mem).isReadOnly());
    checkCrossLeafTypeIds(mem);
    Memory nnreg = mem.region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);

    Memory reg = mem.region(0, bytes, ByteOrder.nativeOrder());
    Buffer buf = reg.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
    Buffer buf4 = buf.duplicate();

    Memory reg2 = nnreg.region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
    Buffer buf2 = reg2.asBuffer().region(0, bytes, BaseState.NON_NATIVE_BYTE_ORDER);
    Buffer buf3 = buf2.duplicate();

    assertFalse(mem.isRegion());
    assertTrue(reg2.isRegion());
    assertTrue(buf.isRegion());
    assertTrue(buf2.isRegion());
    assertTrue(buf3.isDuplicate());
    assertTrue(buf4.isDuplicate());
  }

  private static void checkCrossLeafTypeIds(Memory mem) {
    Memory reg1 = mem.region(0, mem.getCapacity());
    assertTrue(reg1.isRegion());

    Buffer buf1 = reg1.asBuffer();
    assertTrue(buf1.isRegion());
    assertTrue(buf1.isBuffer());
    assertTrue(buf1.isReadOnly());

    Buffer buf2 = buf1.duplicate();
    assertTrue(buf2.isRegion());
    assertTrue(buf2.isBuffer());
    assertTrue(buf2.isDuplicate());
    assertTrue(buf2.isReadOnly());

    Memory mem2 = buf1.asMemory(); //
    assertTrue(mem2.isRegion());
    assertFalse(mem2.isBuffer());
    assertFalse(mem2.isDuplicate());
    assertTrue(mem2.isReadOnly());

    Buffer buf3 = buf1.duplicate(BaseState.NON_NATIVE_BYTE_ORDER);
    assertTrue(buf3.isRegion());
    assertTrue(buf3.isBuffer());
    assertTrue(buf3.isDuplicate());
    assertEquals(buf3.getTypeByteOrder(), BaseState.NON_NATIVE_BYTE_ORDER);
    assertTrue(buf3.isReadOnly());

    Memory mem3 = buf3.asMemory();
    assertTrue(mem3.isRegion());
    assertFalse(mem3.isBuffer());
    assertTrue(mem3.isDuplicate());
    assertEquals(mem3.getTypeByteOrder(), BaseState.NON_NATIVE_BYTE_ORDER);
    assertTrue(mem3.isReadOnly());
  }

}
