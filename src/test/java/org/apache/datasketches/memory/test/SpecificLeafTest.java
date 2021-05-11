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
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.Util;
import org.apache.datasketches.memory.WritableDirectHandle;
import org.apache.datasketches.memory.WritableMapHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("javadoc")
public class SpecificLeafTest {

  static final Method IS_READ_ONLY_TYPE;
  static final Method IS_BUFFER_TYPE;
  static final Method IS_DUPLICATE_TYPE;
  static final Method IS_REGION_TYPE;
  static final Method IS_NON_NATIVE_TYPE;
  static final Method IS_HEAP_TYPE;
  static final Method IS_DIRECT_TYPE;
  static final Method IS_MAP_TYPE;
  static final Method IS_BB_TYPE;
  
  static {
    IS_READ_ONLY_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isReadOnlyType", (Class<?>[])null); //not static
    IS_BUFFER_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isBufferType", (Class<?>[])null); //not static
    IS_DUPLICATE_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isDuplicateType", (Class<?>[])null); //not static
    IS_REGION_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isRegionType", (Class<?>[])null); //not static
    IS_NON_NATIVE_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isNonNativeType", (Class<?>[])null); //not static
    IS_HEAP_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isHeapType", (Class<?>[])null); //not static
    IS_DIRECT_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isDirectType", (Class<?>[])null); //not static
    IS_MAP_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isMapType", (Class<?>[])null); //not static
    IS_BB_TYPE =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "isBBType", (Class<?>[])null); //not static
  }
  
  private static boolean isReadOnlyType(final Object owner) {
    try {
      return (boolean) IS_READ_ONLY_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isBufferType(final Object owner) {
    try {
      return (boolean) IS_BUFFER_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isDuplicateType(final Object owner) {
    try {
      return (boolean) IS_DUPLICATE_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isRegionType(final Object owner) {
    try {
      return (boolean) IS_REGION_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isNonNativeType(final Object owner) {
    try {
      return (boolean) IS_NON_NATIVE_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isHeapType(final Object owner) {
    try {
      return (boolean) IS_HEAP_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isDirectType(final Object owner) {
    try {
      return (boolean) IS_DIRECT_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isMapType(final Object owner) {
    try {
      return (boolean) IS_MAP_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  private static boolean isBBType(final Object owner) {
    try {
      return (boolean) IS_BB_TYPE.invoke(owner);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  
  
  @Test
  public void checkByteBufferLeafs() {
    int bytes = 128;
    ByteBuffer bb = ByteBuffer.allocate(bytes);
    bb.order(Util.nativeByteOrder);

    Memory mem = Memory.wrap(bb).region(0, bytes, Util.nativeByteOrder);
    assertTrue(isBBType(mem));
    assertTrue(mem.isReadOnly());
    checkCrossLeafTypeIds(mem);
    Buffer buf = mem.asBuffer().region(0, bytes, Util.nativeByteOrder);

    bb.order(Util.nonNativeByteOrder);
    Memory mem2 = Memory.wrap(bb).region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf3 = buf2.duplicate();

    assertTrue(isRegionType(mem));
    assertTrue(isRegionType(mem2));
    assertTrue(isRegionType(buf));
    assertTrue(isRegionType(buf2));
    assertTrue(isDuplicateType(buf3));
  }

  @Test
  public void checkDirectLeafs() {
    int bytes = 128;
    try (WritableDirectHandle h = WritableMemory.allocateDirect(bytes)) {
      WritableMemory wmem = h.get(); //native mem
      assertTrue(isDirectType(wmem));
      assertFalse(wmem.isReadOnly());
      checkCrossLeafTypeIds(wmem);
      WritableMemory nnwmem = wmem.writableRegion(0, bytes, Util.nonNativeByteOrder);

      Memory mem = wmem.region(0, bytes, Util.nativeByteOrder);
      Buffer buf = mem.asBuffer().region(0, bytes, Util.nativeByteOrder);


      Memory mem2 = nnwmem.region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf2 = mem2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue(isRegionType(mem));
      assertTrue(isRegionType(mem2));
      assertTrue(isRegionType(buf));
      assertTrue(isRegionType(buf2));
      assertTrue(isDuplicateType(buf3));
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
      assertTrue(isMapType(mem));
      assertFalse(mem.isReadOnly());
      checkCrossLeafTypeIds(mem);
      Memory nnreg = mem.region(0, bytes, Util.nonNativeByteOrder);

      Memory reg = mem.region(0, bytes, Util.nativeByteOrder);
      Buffer buf = reg.asBuffer().region(0, bytes, Util.nativeByteOrder);
      Buffer buf4 = buf.duplicate();

      Memory reg2 = nnreg.region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
      Buffer buf3 = buf2.duplicate();

      assertTrue(isRegionType(reg));
      assertTrue(isRegionType(reg2));
      assertTrue(isRegionType(buf));
      assertTrue(isRegionType(buf2));
      assertTrue(isDuplicateType(buf3));
      assertTrue(isDuplicateType(buf4));
    }
  }

  @Test
  public void checkHeapLeafs() {
    int bytes = 128;
    Memory mem = Memory.wrap(new byte[bytes]);
    assertTrue(isHeapType(mem));
    assertTrue(isReadOnlyType(mem));
    checkCrossLeafTypeIds(mem);
    Memory nnreg = mem.region(0, bytes, Util.nonNativeByteOrder);

    Memory reg = mem.region(0, bytes, Util.nativeByteOrder);
    Buffer buf = reg.asBuffer().region(0, bytes, Util.nativeByteOrder);
    Buffer buf4 = buf.duplicate();

    Memory reg2 = nnreg.region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf2 = reg2.asBuffer().region(0, bytes, Util.nonNativeByteOrder);
    Buffer buf3 = buf2.duplicate();

    assertFalse(isRegionType(mem));
    assertTrue(isRegionType(reg2));
    assertTrue(isRegionType(buf));
    assertTrue(isRegionType(buf2));
    assertTrue(isDuplicateType(buf3));
    assertTrue(isDuplicateType(buf4));
  }

  private static void checkCrossLeafTypeIds(Memory mem) {
    Memory reg1 = mem.region(0, mem.getCapacity());
    assertTrue(isRegionType(reg1));

    Buffer buf1 = reg1.asBuffer();
    assertTrue(isRegionType(buf1));
    assertTrue(isBufferType(buf1));

    Buffer buf2 = buf1.duplicate();
    assertTrue(isRegionType(buf2));
    assertTrue(isBufferType(buf2));
    assertTrue(isDuplicateType(buf2));

    Memory mem2 = buf1.asMemory();
    assertTrue(isRegionType(mem2));
    assertFalse(isBufferType(mem2));
    assertFalse(isDuplicateType(mem2));

    Buffer buf3 = buf1.duplicate(Util.nonNativeByteOrder);
    assertTrue(isRegionType(buf3));
    assertTrue(isBufferType(buf3));
    assertTrue(isDuplicateType(buf3));
    assertTrue(isNonNativeType(buf3));

    Memory mem3 = buf3.asMemory();
    assertTrue(isRegionType(mem3));
    assertFalse(isBufferType(mem3));
    assertFalse(isDuplicateType(mem3));
    assertFalse(isNonNativeType(mem3));
  }

}
