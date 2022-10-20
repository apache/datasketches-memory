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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMapHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class SpecificLeafTest {

  @Test
  public void checkByteBufferLeafs() {
    int bytes = 128;
    ByteBuffer bb = ByteBuffer.allocate(bytes);
    bb.order(ByteOrder.nativeOrder());

    Memory mem = Memory.wrap(bb).region(0, bytes, ByteOrder.nativeOrder());
    assertTrue(((BaseStateImpl)mem).isBBType());
    assertTrue(mem.isReadOnly());
    checkCrossLeafTypeIds(mem);
    Buffer buf = mem.asBuffer().region(0, bytes, ByteOrder.nativeOrder());

    bb.order(Util.NON_NATIVE_BYTE_ORDER);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf3 = buf2.duplicate();

    assertTrue(((BaseStateImpl)mem).isRegionType());
    assertTrue(((BaseStateImpl)mem2).isRegionType());
    assertTrue(((BaseStateImpl)buf).isRegionType());
    assertTrue(((BaseStateImpl)buf2).isRegionType());
    assertTrue(((BaseStateImpl)buf3).isDuplicateType());
  }

  @Test
  public void checkDirectLeafs() throws Exception {
    int bytes = 128;
    try (WritableHandle h = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wmem = h.getWritable(); //native mem
      assertTrue(((BaseStateImpl)wmem).isDirectType());
      assertFalse(wmem.isReadOnly());
      checkCrossLeafTypeIds(wmem);
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, Util.NON_NATIVE_BYTE_ORDER);

      Memory mem = wmem.region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf = mem.asBuffer().region(0, bytes, ByteOrder.nativeOrder());


      Memory mem2 = nnwmem.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf3 = buf2.duplicate();

      assertTrue(((BaseStateImpl)mem).isRegionType());
      assertTrue(((BaseStateImpl)mem2).isRegionType());
      assertTrue(((BaseStateImpl)buf).isRegionType());
      assertTrue(((BaseStateImpl)buf2).isRegionType());
      assertTrue(((BaseStateImpl)buf3).isDuplicateType());
    }
  }

  @Test
  public void checkMapLeafs() throws Exception {
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

    final long bytes = 128;

    try (WritableMapHandle h = WritableMemory.writableMap(file, 0L, bytes, ByteOrder.nativeOrder())) {
      WritableMemory mem = h.getWritable(); //native mem
      assertTrue(((BaseStateImpl)mem).isMapType());
      assertFalse(mem.isReadOnly());
      checkCrossLeafTypeIds(mem);
      Memory nnreg = mem.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);

      Memory reg = mem.region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf = reg.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf4 = buf.duplicate();

      Memory reg2 = nnreg.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf3 = buf2.duplicate();

      assertTrue(((BaseStateImpl)reg).isRegionType());
      assertTrue(((BaseStateImpl)reg2).isRegionType());
      assertTrue(((BaseStateImpl)buf).isRegionType());
      assertTrue(((BaseStateImpl)buf2).isRegionType());
      assertTrue(((BaseStateImpl)buf3).isDuplicateType());
      assertTrue(((BaseStateImpl)buf4).isDuplicateType());
    }
  }

  @Test
  public void checkHeapLeafs() {
    int bytes = 128;
    Memory mem = Memory.wrap(new byte[bytes]);
    assertTrue(((BaseStateImpl)mem).isHeapType());
    assertTrue(((BaseStateImpl)mem).isReadOnlyType());
    checkCrossLeafTypeIds(mem);
    Memory nnreg = mem.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);

    Memory reg = mem.region(0, bytes, ByteOrder.nativeOrder());
    Buffer buf = reg.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
    Buffer buf4 = buf.duplicate();

    Memory reg2 = nnreg.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf3 = buf2.duplicate();

    assertFalse(((BaseStateImpl)mem).isRegionType());
    assertTrue(((BaseStateImpl)reg2).isRegionType());
    assertTrue(((BaseStateImpl)buf).isRegionType());
    assertTrue(((BaseStateImpl)buf2).isRegionType());
    assertTrue(((BaseStateImpl)buf3).isDuplicateType());
    assertTrue(((BaseStateImpl)buf4).isDuplicateType());
  }

  private static void checkCrossLeafTypeIds(Memory mem) {
    Memory reg1 = mem.region(0, mem.getCapacity());
    assertTrue(((BaseStateImpl)reg1).isRegionType());

    Buffer buf1 = reg1.asBuffer();
    assertTrue(((BaseStateImpl)buf1).isRegionType());
    assertTrue(((BaseStateImpl)buf1).isBufferType());
    assertTrue(buf1.isReadOnly());

    Buffer buf2 = buf1.duplicate();
    assertTrue(((BaseStateImpl)buf2).isRegionType());
    assertTrue(((BaseStateImpl)buf2).isBufferType());
    assertTrue(((BaseStateImpl)buf2).isDuplicateType());
    assertTrue(buf2.isReadOnly());

    Memory mem2 = buf1.asMemory(); //
    assertTrue(((BaseStateImpl)mem2).isRegionType());
    assertFalse(((BaseStateImpl)mem2).isBufferType());
    assertFalse(((BaseStateImpl)mem2).isDuplicateType());
    assertTrue(mem2.isReadOnly());

    Buffer buf3 = buf1.duplicate(Util.NON_NATIVE_BYTE_ORDER);
    assertTrue(((BaseStateImpl)buf3).isRegionType());
    assertTrue(((BaseStateImpl)buf3).isBufferType());
    assertTrue(((BaseStateImpl)buf3).isDuplicateType());
    assertTrue(((BaseStateImpl)buf3).isNonNativeType());
    assertTrue(buf3.isReadOnly());

    Memory mem3 = buf3.asMemory();
    assertTrue(((BaseStateImpl)mem3).isRegionType());
    assertFalse(((BaseStateImpl)mem3).isBufferType());
    assertTrue(((BaseStateImpl)mem3).isDuplicateType());
    assertTrue(((BaseStateImpl)mem3).isNonNativeType());
    assertTrue(mem3.isReadOnly());
  }

}
