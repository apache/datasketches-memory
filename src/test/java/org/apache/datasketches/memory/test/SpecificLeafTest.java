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

import static org.apache.datasketches.memory.BaseState.DUPLICATE;
import static org.apache.datasketches.memory.BaseState.REGION;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableDirectHandle;
import org.apache.datasketches.memory.WritableMapHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("javadoc")
public class SpecificLeafTest {

  @Test
  public void checkByteBufferLeafs() {
    int bytes = 128;
    ByteBuffer bb = ByteBuffer.allocate(bytes);
    bb.order(BaseState.nativeByteOrder);

    Memory mem = Memory.wrap(bb).region(0, bytes, BaseState.nativeByteOrder);
    assertTrue(mem.isBBType());
    assertTrue(mem.isReadOnly());
    checkCrossLeafTypeIds(mem);
    Buffer buf = mem.asBuffer().region(0, bytes, BaseState.nativeByteOrder);

    bb.order(BaseState.nonNativeByteOrder);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, BaseState.nonNativeByteOrder);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, BaseState.nonNativeByteOrder);
    Buffer buf3 = buf2.duplicate();

    assertTrue((mem.getTypeId() & REGION) > 0);
    assertTrue((mem2.getTypeId() & REGION) > 0);
    assertTrue((buf.getTypeId() & REGION) > 0);
    assertTrue((buf2.getTypeId() & REGION) > 0);
    assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
  }

  @Test
  public void checkDirectLeafs() {
    int bytes = 128;
    try (WritableDirectHandle h = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wmem = h.get(); //native mem
      assertTrue(wmem.isDirectType());
      assertFalse(wmem.isReadOnly());
      checkCrossLeafTypeIds(wmem);
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, BaseState.nonNativeByteOrder);

      Memory mem = wmem.region(0, bytes, BaseState.nativeByteOrder);
      Buffer buf = mem.asBuffer().region(0, bytes, BaseState.nativeByteOrder);


      Memory mem2 = nnwmem.region(0, bytes, BaseState.nonNativeByteOrder);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, BaseState.nonNativeByteOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue((mem.getTypeId() & REGION) > 0);
      assertTrue((mem2.getTypeId() & REGION) > 0);
      assertTrue((buf.getTypeId() & REGION) > 0);
      assertTrue((buf2.getTypeId() & REGION) > 0);
      assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
    }
  }

  @Test
  public void checkMapLeafs() throws IOException {
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

    try (WritableMapHandle h = WritableMemory.map(file, 0L, bytes, BaseState.nativeByteOrder)) {
      WritableMemory mem = h.get(); //native mem
      assertTrue(mem.isMapType());
      assertFalse(mem.isReadOnly());
      checkCrossLeafTypeIds(mem);
      Memory nnreg = mem.region(0, bytes, BaseState.nonNativeByteOrder);

      Memory reg = mem.region(0, bytes, BaseState.nativeByteOrder);
      Buffer buf = reg.asBuffer().region(0, bytes, BaseState.nativeByteOrder);
      Buffer buf4 = buf.duplicate();

      Memory reg2 = nnreg.region(0, bytes, BaseState.nonNativeByteOrder);
      Buffer buf2 = reg2.asBuffer().region(0, bytes, BaseState.nonNativeByteOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue((reg.getTypeId() & REGION) > 0);
      assertTrue((reg2.getTypeId() & REGION) > 0);
      assertTrue((buf.getTypeId() & REGION) > 0);
      assertTrue((buf2.getTypeId() & REGION) > 0);
      assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
      assertTrue((buf4.getTypeId() & DUPLICATE) > 0);
    }
  }

  @Test
  public void checkHeapLeafs() {
    int bytes = 128;
    Memory mem = Memory.wrap(new byte[bytes]);
    assertTrue(mem.isHeapType());
    assertTrue(mem.isReadOnlyType());
    checkCrossLeafTypeIds(mem);
    Memory nnreg = mem.region(0, bytes, BaseState.nonNativeByteOrder);

    Memory reg = mem.region(0, bytes, BaseState.nativeByteOrder);
    Buffer buf = reg.asBuffer().region(0, bytes, BaseState.nativeByteOrder);
    Buffer buf4 = buf.duplicate();

    Memory reg2 = nnreg.region(0, bytes, BaseState.nonNativeByteOrder);
    Buffer buf2 = reg2.asBuffer().region(0, bytes, BaseState.nonNativeByteOrder);
    Buffer buf3 = buf2.duplicate();

    assertFalse((mem.getTypeId() & REGION) > 0);
    assertTrue((reg2.getTypeId() & REGION) > 0);
    assertTrue((buf.getTypeId() & REGION) > 0);
    assertTrue((buf2.getTypeId() & REGION) > 0);
    assertTrue((buf3.getTypeId() & DUPLICATE) > 0);
    assertTrue((buf4.getTypeId() & DUPLICATE) > 0);
  }

  private static void checkCrossLeafTypeIds(Memory mem) {
    Memory reg1 = mem.region(0, mem.getCapacity());
    assertTrue(reg1.isRegionType());

    Buffer buf1 = reg1.asBuffer();
    assertTrue(buf1.isRegionType());
    assertTrue(buf1.isBufferType());

    Buffer buf2 = buf1.duplicate();
    assertTrue(buf2.isRegionType());
    assertTrue(buf2.isBufferType());
    assertTrue(buf2.isDuplicateType());

    Memory mem2 = buf1.asMemory();
    assertTrue(mem2.isRegionType());
    assertFalse(mem2.isBufferType());
    assertFalse(mem2.isDuplicateType());

    Buffer buf3 = buf1.duplicate(BaseState.nonNativeByteOrder);
    assertTrue(buf3.isRegionType());
    assertTrue(buf3.isBufferType());
    assertTrue(buf3.isDuplicateType());
    assertTrue(buf3.isNonNativeType());

    Memory mem3 = buf3.asMemory();
    assertTrue(mem3.isRegionType());
    assertFalse(mem3.isBufferType());
    assertFalse(mem3.isDuplicateType());
    assertFalse(mem3.isNonNativeType());
  }

}
