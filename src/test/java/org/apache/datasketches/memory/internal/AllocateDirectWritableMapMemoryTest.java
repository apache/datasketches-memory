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

package org.apache.datasketches.memory.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.datasketches.memory.internal.ResourceImpl.LS;
import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;
import static org.apache.datasketches.memory.internal.Util.getResourceFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.foreign.Arena;
import java.nio.ByteOrder;
import java.nio.file.InvalidPathException;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AllocateDirectWritableMapMemoryTest {
  private final MemoryRequestServer memReqSvr = Resource.defaultMemReqSvr;

  @BeforeClass
  public void setReadOnly() throws IOException {
    UtilTest.setGettysburgAddressFileToReadOnly();
  }

  @Test
  public void simpleMap() throws IllegalArgumentException, InvalidPathException, IllegalStateException,
      UnsupportedOperationException, IOException, SecurityException {
    File file = getResourceFile("GettysburgAddress.txt");

    try (Memory mem = Memory.map(Arena.ofConfined(), file)) {
      byte[] byteArr = new byte[(int)mem.getCapacity()];
      mem.getByteArray(0, byteArr, 0, byteArr.length);
      String text = new String(byteArr, UTF_8);
      println(text);
      assertTrue(mem.isReadOnly());
    }
  }

  @Test
  public void copyOffHeapToMemoryMappedFile()
      throws IllegalArgumentException, InvalidPathException, IllegalStateException, UnsupportedOperationException,
      IOException, SecurityException {
    long numBytes = 1L << 10; //small for unit tests.  Make it larger than 2GB if you like.
    long numLongs = numBytes >>> 3;

    File file = new File("TestFile.bin"); //create a dummy file
    if (file.exists()) {
      java.nio.file.Files.delete(file.toPath());
    }
    assertTrue(file.createNewFile());
    assertTrue(file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue(file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.

    WritableMemory dstMem = null;
    WritableMemory srcMem = null;
    try (Arena arena = Arena.ofConfined()) { //this scope manages two Memory objects
      dstMem = WritableMemory.writableMap(file, 0, numBytes, ByteOrder.nativeOrder(), arena);
      srcMem = WritableMemory.allocateDirect(arena, numBytes, 8, ByteOrder.nativeOrder(), memReqSvr);

      //load source with consecutive longs
      for (long i = 0; i < numLongs; i++) {
        srcMem.putLong(i << 3, i);
      }
      //off-heap to off-heap copy
      srcMem.copyTo(0, dstMem, 0, srcMem.getCapacity());
      dstMem.force(); //push any remaining to the file
      //check end value
      assertEquals(dstMem.getLong(numLongs - 1L << 3), numLongs - 1L);
    } //both map and direct closed here
  }

  @Test
  public void checkNonNativeFile()
      throws IllegalArgumentException, InvalidPathException, IllegalStateException, UnsupportedOperationException,
      IOException, SecurityException {
    File file = new File("TestFile2.bin");
    if (file.exists()) {
      java.nio.file.Files.delete(file.toPath());
    }
    assertTrue(file.createNewFile());
    assertTrue(file.setWritable(true, false)); //writable=true, ownerOnly=false
    assertTrue(file.isFile());
    file.deleteOnExit();  //comment out if you want to examine the file.

    final long bytes = 8;
    WritableMemory wmem = null;
    wmem = WritableMemory.writableMap(file, 0L, bytes, NON_NATIVE_BYTE_ORDER);
    wmem.putChar(0, (char) 1);
    assertEquals(wmem.getByte(1), (byte) 1);
  }

  @Test
  public void testMapExceptionNoTWR()
      throws IllegalArgumentException, InvalidPathException, IllegalStateException, UnsupportedOperationException,
      IOException, SecurityException {
    File dummy = createTempFile("dummy", ".txt" , ""); //zero length
    try (Arena arena = Arena.ofConfined()) {
      Memory.map(arena, dummy, 0, dummy.length(), ByteOrder.nativeOrder());
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void simpleMap2()
      throws IllegalArgumentException, InvalidPathException, IllegalStateException, UnsupportedOperationException,
      IOException, SecurityException {
    File file = getResourceFile("GettysburgAddress.txt");
    assertTrue(file.canRead());
    assertFalse(file.canWrite());
    try (Arena arena = Arena.ofConfined();
         WritableMemory wmem = WritableMemory.writableMap(file)) { //assumes file is writable!
      wmem.getCapacity();
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkReadException()
      throws IllegalArgumentException, InvalidPathException, IllegalStateException, UnsupportedOperationException,
      IOException, SecurityException {
    File file = getResourceFile("GettysburgAddress.txt");
    WritableMemory wmem = null;
    wmem = WritableMemory.writableMap(file, 0, 1 << 20, ByteOrder.nativeOrder());
    wmem.getCapacity();
  }

  @Test
  public void testForce()
      throws IllegalArgumentException, InvalidPathException, IllegalStateException, UnsupportedOperationException,
      IOException, SecurityException {
    String origStr = "Corectng spellng mistks";
    File origFile = createTempFile("force_original", ".txt", origStr); //23
    assertTrue(origFile.setWritable(true, false));
    long origBytes = origFile.length();
    String correctStr = "Correcting spelling mistakes"; //28
    byte[] correctByteArr = correctStr.getBytes(UTF_8);
    long correctBytesLen = correctByteArr.length;

    Memory mem = null;
    WritableMemory wmem = null;
    try (Arena arena = Arena.ofConfined()) {
      mem = Memory.map(arena, origFile, 0, origBytes, ByteOrder.nativeOrder());
      mem.load();
      //assertTrue(mem.isLoaded()); //incompatible with Windows
      //confirm orig string
      byte[] buf = new byte[(int)origBytes];
      mem.getByteArray(0, buf, 0, (int)origBytes);
      String bufStr = new String(buf, UTF_8);
      assertEquals(bufStr, origStr);

      wmem = WritableMemory.writableMap(origFile, 0, correctBytesLen, ByteOrder.nativeOrder());
      wmem.load();
      //assertTrue(wmem.isLoaded()); //incompatible with Windows
      // over write content
      wmem.putByteArray(0, correctByteArr, 0, (int)correctBytesLen);
      wmem.force();
      //confirm correct string
      byte[] buf2 = new byte[(int)correctBytesLen];
      wmem.getByteArray(0, buf2, 0, (int)correctBytesLen);
      String bufStr2 = new String(buf2, UTF_8);
      assertEquals(bufStr2, correctStr);
    }
  }

  private static File createTempFile(String fileNamePrefix, String fileNameSuffix, String text)
          throws FileNotFoundException, IOException {
    File file = File.createTempFile(fileNamePrefix, fileNameSuffix);
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
