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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.datasketches.memory.internal.Buffer;
import org.apache.datasketches.memory.internal.Memory;
import org.apache.datasketches.memory.internal.Util;
import org.apache.datasketches.memory.internal.WritableDirectHandle;
import org.apache.datasketches.memory.internal.WritableMapHandle;
import org.apache.datasketches.memory.internal.WritableMemory;
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
    bb.order(Util.nativeByteOrder);

    Memory mem = Memory.wrap(bb).region(0, bytes, Util.nativeByteOrder);
    assertTrue(ReflectUtil.isBBType(mem));
    assertTrue(mem.isReadOnly());
    checkCrossLeafTypeIds(mem);
    Buffer buf = mem.asBuffer().region(0, bytes, Util.nativeByteOrder);

    bb.order(Util.nonNativeByteOrder);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf3 = buf2.duplicate();

    assertTrue(ReflectUtil.isRegionType(mem));
    assertTrue(ReflectUtil.isRegionType(mem2));
    assertTrue(ReflectUtil.isRegionType(buf));
    assertTrue(ReflectUtil.isRegionType(buf2));
    assertTrue(ReflectUtil.isDuplicateType(buf3));
  }

  @Test
  public void checkDirectLeafs() {
    int bytes = 128;
    try (WritableDirectHandle h = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wmem = h.get(); //native mem
      assertTrue(ReflectUtil.isDirectType(wmem));
      assertFalse(wmem.isReadOnly());
      checkCrossLeafTypeIds(wmem);
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, Util.nonNativeByteOrder);

      Memory mem = wmem.region(0, bytes, Util.nativeByteOrder);
      Buffer buf = mem.asBuffer().region(0, bytes, Util.nativeByteOrder);


      Memory mem2 = nnwmem.region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue(ReflectUtil.isRegionType(mem));
      assertTrue(ReflectUtil.isRegionType(mem2));
      assertTrue(ReflectUtil.isRegionType(buf));
      assertTrue(ReflectUtil.isRegionType(buf2));
      assertTrue(ReflectUtil.isDuplicateType(buf3));
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

    try (WritableMapHandle h = WritableMemory.map(file, 0L, bytes, Util.nativeByteOrder)) {
      WritableMemory mem = h.get(); //native mem
      assertTrue(ReflectUtil.isMapType(mem));
      assertFalse(mem.isReadOnly());
      checkCrossLeafTypeIds(mem);
      Memory nnreg = mem.region(0, bytes, Util.nonNativeByteOrder);

      Memory reg = mem.region(0, bytes, Util.nativeByteOrder);
      Buffer buf = reg.asBuffer().region(0, bytes, Util.nativeByteOrder);
      Buffer buf4 = buf.duplicate();

      Memory reg2 = nnreg.region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue(ReflectUtil.isRegionType(reg));
      assertTrue(ReflectUtil.isRegionType(reg2));
      assertTrue(ReflectUtil.isRegionType(buf));
      assertTrue(ReflectUtil.isRegionType(buf2));
      assertTrue(ReflectUtil.isDuplicateType(buf3));
      assertTrue(ReflectUtil.isDuplicateType(buf4));
    }
  }

  @Test
  public void checkHeapLeafs() {
    int bytes = 128;
    Memory mem = Memory.wrap(new byte[bytes]);
    assertTrue(ReflectUtil.isHeapType(mem));
    assertTrue(ReflectUtil.isReadOnlyType(mem));
    checkCrossLeafTypeIds(mem);
    Memory nnreg = mem.region(0, bytes, Util.nonNativeByteOrder);

    Memory reg = mem.region(0, bytes, Util.nativeByteOrder);
    Buffer buf = reg.asBuffer().region(0, bytes, Util.nativeByteOrder);
    Buffer buf4 = buf.duplicate();

    Memory reg2 = nnreg.region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf3 = buf2.duplicate();

    assertFalse(ReflectUtil.isRegionType(mem));
    assertTrue(ReflectUtil.isRegionType(reg2));
    assertTrue(ReflectUtil.isRegionType(buf));
    assertTrue(ReflectUtil.isRegionType(buf2));
    assertTrue(ReflectUtil.isDuplicateType(buf3));
    assertTrue(ReflectUtil.isDuplicateType(buf4));
  }

  private static void checkCrossLeafTypeIds(Memory mem) {
    Memory reg1 = mem.region(0, mem.getCapacity());
    assertTrue(ReflectUtil.isRegionType(reg1));

    Buffer buf1 = reg1.asBuffer();
    assertTrue(ReflectUtil.isRegionType(buf1));
    assertTrue(ReflectUtil.isBufferType(buf1));

    Buffer buf2 = buf1.duplicate();
    assertTrue(ReflectUtil.isRegionType(buf2));
    assertTrue(ReflectUtil.isBufferType(buf2));
    assertTrue(ReflectUtil.isDuplicateType(buf2));

    Memory mem2 = buf1.asMemory();
    assertTrue(ReflectUtil.isRegionType(mem2));
    assertFalse(ReflectUtil.isBufferType(mem2));
    assertFalse(ReflectUtil.isDuplicateType(mem2));

    Buffer buf3 = buf1.duplicate(Util.nonNativeByteOrder);
    assertTrue(ReflectUtil.isRegionType(buf3));
    assertTrue(ReflectUtil.isBufferType(buf3));
    assertTrue(ReflectUtil.isDuplicateType(buf3));
    assertTrue(ReflectUtil.isNonNativeType(buf3));

    Memory mem3 = buf3.asMemory();
    assertTrue(ReflectUtil.isRegionType(mem3));
    assertFalse(ReflectUtil.isBufferType(mem3));
    assertFalse(ReflectUtil.isDuplicateType(mem3));
    assertFalse(ReflectUtil.isNonNativeType(mem3));
  }

}
