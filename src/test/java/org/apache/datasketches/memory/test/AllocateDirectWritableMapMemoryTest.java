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

/*
 * Note: Lincoln's Gettysburg Address is in the public domain. See LICENSE.
 */

package org.apache.datasketches.memory.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.datasketches.memory.internal.Util.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.internal.Memory;
import org.apache.datasketches.memory.internal.ReadOnlyException;
import org.apache.datasketches.memory.internal.Util;
import org.apache.datasketches.memory.internal.WritableMemory;
import org.apache.datasketches.memory.WritableMapHandle;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class AllocateDirectWritableMapMemoryTest {
  private static final String LS = System.getProperty("line.separator");

  static final Method IS_FILE_READ_ONLY;
  static final Method GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS;
  
  static {
    IS_FILE_READ_ONLY =
        ReflectUtil.getMethod(ReflectUtil.ALLOCATE_DIRECT_MAP, "isFileReadOnly", File.class);
    GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS =
        ReflectUtil.getMethod(ReflectUtil.BASE_STATE, "getCurrentDirectMemoryMapAllocations", (Class<?>[])null); //static
  }
  
  private static boolean isFileReadOnly(final File file) {
    try {
      return (boolean) IS_FILE_READ_ONLY.invoke(null, file);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private static long getCurrentDirectMemoryMapAllocations() {
    try {
      return (long) GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }
  
  @BeforeClass
  public void setReadOnly() {
    UtilTest.setGettysburgAddressFileToReadOnly();
  }

  @Test
  public void simpleMap() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    try (MapHandle h = Memory.map(file); WritableMapHandle wh = (WritableMapHandle) h) {
      Memory mem = h.get();
      byte[] bytes = new byte[(int)mem.getCapacity()];
      mem.getByteArray(0, bytes, 0, bytes.length);
      String text = new String(bytes, UTF_8);
      println(text);
      try {
        wh.force();
        fail();
      } catch (ReadOnlyException e) {
        //OK
      }
    }
  }

  @Test
  public void copyOffHeapToMemoryMappedFile() throws Exception {
    long bytes = 1L << 10; //small for unit tests.  Make it larger than 2GB if you like.
    long longs = bytes >>> 3;

    File file = new File("TestFile.bin");
    if (file.exists()) {
      try {
        java.nio.file.Files.delete(file.toPath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    assertTrue(file.createNewFile());
    assertTrue (file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue (file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.

    try (
        WritableMapHandle dstHandle
          = WritableMemory.writableMap(file, 0, bytes, ByteOrder.nativeOrder());
        WritableHandle srcHandle = WritableMemory.allocateDirect(bytes)) {

      WritableMemory dstMem = dstHandle.get();
      WritableMemory srcMem = srcHandle.get();

      for (long i = 0; i < longs; i++) {
        srcMem.putLong(i << 3, i); //load source with consecutive longs
      }

      srcMem.copyTo(0, dstMem, 0, srcMem.getCapacity()); //off-heap to off-heap copy

      dstHandle.force(); //push any remaining to the file

      //check end value
      assertEquals(dstMem.getLong(longs - 1L << 3), longs - 1L);
    }
  }

  @Test
  public void checkNonNativeFile() throws IOException {
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

    final long bytes = 8;
    try (WritableMapHandle h = WritableMemory.writableMap(file, 0L, bytes, Util.nonNativeByteOrder)) {
      WritableMemory wmem = h.get();
      wmem.putChar(0, (char) 1);
      assertEquals(wmem.getByte(1), (byte) 1);
    }
  }

  @SuppressWarnings("resource")
  @Test(expectedExceptions = RuntimeException.class)
  public void testMapException() throws IOException {
    File dummy = createFile("dummy.txt", ""); //zero length
    //throws java.lang.reflect.InvocationTargetException
    Memory.map(dummy, 0, dummy.length(), ByteOrder.nativeOrder());
  }

  @Test(expectedExceptions = ReadOnlyException.class)
  public void simpleMap2() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    assertTrue(isFileReadOnly(file));
    try (WritableMapHandle rh = WritableMemory.writableMap(file)) { //throws
      //
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkOverLength()  {
    File file = getResourceFile("GettysburgAddress.txt");
    try (WritableMapHandle rh = WritableMemory.writableMap(file, 0, 1 << 20, ByteOrder.nativeOrder())) {
      //
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testForce() throws Exception {
    String origStr = "Corectng spellng mistks";
    File origFile = createFile("force_original.txt", origStr); //23
    assertTrue(origFile.setWritable(true, false));
    long origBytes = origFile.length();
    String correctStr = "Correcting spelling mistakes"; //28
    byte[] correctByteArr = correctStr.getBytes(UTF_8);
    long corrBytes = correctByteArr.length;

    try (MapHandle rh = Memory.map(origFile, 0, origBytes, ByteOrder.nativeOrder())) {
      Memory map = rh.get();
      rh.load();
      assertTrue(rh.isLoaded());
      //confirm orig string
      byte[] buf = new byte[(int)origBytes];
      map.getByteArray(0, buf, 0, (int)origBytes);
      String bufStr = new String(buf, UTF_8);
      assertEquals(bufStr, origStr);
    }

    try (WritableMapHandle wrh = WritableMemory.writableMap(origFile, 0, corrBytes,
        ByteOrder.nativeOrder())) {
      WritableMemory wMap = wrh.get();
      wrh.load();
      assertTrue(wrh.isLoaded());
      // over write content
      wMap.putByteArray(0, correctByteArr, 0, (int)corrBytes);
      wrh.force();
      //confirm correct string
      byte[] buf = new byte[(int)corrBytes];
      wMap.getByteArray(0, buf, 0, (int)corrBytes);
      String bufStr = new String(buf, UTF_8);
      assertEquals(bufStr, correctStr);
    }
  }

  private static File createFile(String fileName, String text) throws FileNotFoundException {
    File file = new File(fileName);
    file.deleteOnExit();
    PrintWriter writer;
    try {
      writer = new PrintWriter(file, UTF_8.name());
      writer.print(text);
      writer.close();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return file;
  }

  @Test
  public void checkExplicitClose() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    try (MapHandle wmh = Memory.map(file)) {
      wmh.close(); //explicit close.
    } //end of scope call to Cleaner/Deallocator also will be redundant
  }

  @AfterClass
  public void checkDirectCounter() {
    long count =  getCurrentDirectMemoryMapAllocations();
      //final long count = BaseStateImpl.getCurrentDirectMemoryMapAllocations();
      if (count != 0) {
        println(""+count);
        fail();
      }
    } 

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  static void println(final Object o) {
    if (o == null) { print(LS); }
    else { print(o.toString() + LS); }
  }

  /**
   * @param o value to print
   */
  static void print(final Object o) {
    if (o != null) {
      //System.out.print(o.toString()); //disable here
    }
  }

}
