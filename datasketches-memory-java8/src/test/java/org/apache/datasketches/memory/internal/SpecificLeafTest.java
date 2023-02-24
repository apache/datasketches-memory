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
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class SpecificLeafTest {

  @Test
  public void checkByteBufferLeaves() {
    int bytes = 128;
    ByteBuffer bb = ByteBuffer.allocate(bytes);
    bb.order(ByteOrder.nativeOrder());

    Memory mem = Memory.wrap(bb).region(0, bytes, ByteOrder.nativeOrder());
    ResourceImpl bsi = (ResourceImpl)mem;
    int typeId = bsi.getTypeId();
    assertTrue(bsi.isByteBufferResource());
    assertTrue(bsi.isNativeOrder(typeId));
    assertTrue(mem.isReadOnly());
    checkCrossLeafTypeIds(mem);
    Buffer buf = mem.asBuffer().region(0, bytes, ByteOrder.nativeOrder());

    bb.order(Util.NON_NATIVE_BYTE_ORDER);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf3 = buf2.duplicate();

    assertTrue(((ResourceImpl)mem).isRegionView());
    assertTrue(((ResourceImpl)mem2).isRegionView());
    assertTrue(((ResourceImpl)buf).isRegionView());
    assertTrue(((ResourceImpl)buf2).isRegionView());
    assertTrue(((ResourceImpl)buf3).isDuplicateBufferView());
  }

  @Test
  public void checkDirectLeaves()  {
    int bytes = 128;
    try (WritableMemory wmem = WritableMemory.allocateDirect(bytes)) {
      assertTrue(((ResourceImpl)wmem).isDirectResource());
      assertFalse(wmem.isReadOnly());
      checkCrossLeafTypeIds(wmem);
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, Util.NON_NATIVE_BYTE_ORDER);

      Memory mem = wmem.region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf = mem.asBuffer().region(0, bytes, ByteOrder.nativeOrder());


      Memory mem2 = nnwmem.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf3 = buf2.duplicate();

      assertTrue(((ResourceImpl)mem).isRegionView());
      assertTrue(((ResourceImpl)mem2).isRegionView());
      assertTrue(((ResourceImpl)buf).isRegionView());
      assertTrue(((ResourceImpl)buf2).isRegionView());
      assertTrue(((ResourceImpl)buf3).isDuplicateBufferView());
    }
  }

  @Test
  public void checkHeapLeaves() {
    int bytes = 128;
    Memory mem = Memory.wrap(new byte[bytes]);
    ResourceImpl bsi = (ResourceImpl)mem;
    assertTrue(bsi.isHeapResource());
    assertTrue(bsi.isReadOnly());
    checkCrossLeafTypeIds(mem);
    Memory nnreg = mem.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);

    Memory reg = mem.region(0, bytes, ByteOrder.nativeOrder());
    Buffer buf = reg.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
    Buffer buf4 = buf.duplicate();

    Memory reg2 = nnreg.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
    Buffer buf3 = buf2.duplicate();

    assertFalse(((ResourceImpl)mem).isRegionView());
    assertTrue(((ResourceImpl)reg2).isRegionView());
    assertTrue(((ResourceImpl)buf).isRegionView());
    assertTrue(((ResourceImpl)buf2).isRegionView());
    assertTrue(((ResourceImpl)buf3).isDuplicateBufferView());
    assertTrue(((ResourceImpl)buf4).isDuplicateBufferView());
  }

  @Test
  public void checkMapLeaves() throws IOException {
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

    try (WritableMemory mem = WritableMemory.writableMap(file, 0L, bytes, ByteOrder.nativeOrder())) {
      assertTrue(((ResourceImpl)mem).isMemoryMappedResource());
      assertFalse(mem.isReadOnly());
      checkCrossLeafTypeIds(mem);
      Memory nnreg = mem.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);

      Memory reg = mem.region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf = reg.asBuffer().region(0, bytes, ByteOrder.nativeOrder());
      Buffer buf4 = buf.duplicate();

      Memory reg2 = nnreg.region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.NON_NATIVE_BYTE_ORDER);
      Buffer buf3 = buf2.duplicate();

      assertTrue(((ResourceImpl)reg).isRegionView());
      assertTrue(((ResourceImpl)reg2).isRegionView());
      assertTrue(((ResourceImpl)buf).isRegionView());
      assertTrue(((ResourceImpl)buf2).isRegionView());
      assertTrue(((ResourceImpl)buf3).isDuplicateBufferView());
      assertTrue(((ResourceImpl)buf4).isDuplicateBufferView());
    }
  }

  private static void checkCrossLeafTypeIds(Memory mem) {
    Memory reg1 = mem.region(0, mem.getCapacity());
    assertTrue(((ResourceImpl)reg1).isRegionView());
    Buffer buf1 = reg1.asBuffer();
    assertTrue(((ResourceImpl)buf1).isRegionView());
    assertTrue(((ResourceImpl)buf1).isBufferApi(((ResourceImpl)buf1).getTypeId()));
    assertTrue(buf1.isReadOnly());

    Buffer buf2 = buf1.duplicate();
    assertTrue(((ResourceImpl)buf2).isRegionView());
    assertTrue(((ResourceImpl)buf2).isBufferApi(((ResourceImpl)buf2).getTypeId()));
    assertTrue(((ResourceImpl)buf2).isDuplicateBufferView());
    assertTrue(buf2.isReadOnly());

    Memory mem2 = buf1.asMemory(); //
    assertTrue(((ResourceImpl)mem2).isRegionView());
    assertFalse(((ResourceImpl)mem2).isBufferApi(((ResourceImpl)mem2).getTypeId()));
    assertFalse(((ResourceImpl)mem2).isDuplicateBufferView());
    assertTrue(mem2.isReadOnly());

    Buffer buf3 = buf1.duplicate(Util.NON_NATIVE_BYTE_ORDER);
    assertTrue(((ResourceImpl)buf3).isRegionView());
    assertTrue(((ResourceImpl)buf3).isBufferApi(((ResourceImpl)buf3).getTypeId()));
    assertTrue(((ResourceImpl)buf3).isDuplicateBufferView());
    assertTrue(((ResourceImpl)buf3).isNonNativeOrder());
    assertTrue(buf3.isReadOnly());

    Memory mem3 = buf3.asMemory();
    assertTrue(((ResourceImpl)mem3).isRegionView());
    assertFalse(((ResourceImpl)mem3).isBufferApi(((ResourceImpl)mem3).getTypeId()));
    assertTrue(((ResourceImpl)mem3).isDuplicateBufferView());
    assertTrue(((ResourceImpl)mem3).isNonNativeOrder());
    assertTrue(mem3.isReadOnly());
  }

}
