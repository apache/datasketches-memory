/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.AllocateDirectMap.checkOffsetAndCapacity;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class AllocateDirectWritableMapMemoryTest {

  @Test
  public void simpleMap() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (MapHandle h = Memory.map(file)) {
      Memory mem = h.get();
      byte[] bytes = new byte[(int)mem.getCapacity()];
      mem.getByteArray(0, bytes, 0, bytes.length);
      String text = new String(bytes, UTF_8);
      println(text);
    }
  }

  @Test
  public void copyOffHeapToMemoryMappedFile() throws Exception {
    long bytes = 1L << 10; //small for unit tests.  Make it larger than 2GB if you like.
    long longs = bytes >>> 3;

    File file = new File("TestFile.bin");
    if (file.exists()) { file.delete(); }
    assert file.createNewFile();
    assert file.setWritable(true, false);
    assert file.isFile();
    file.deleteOnExit();  //comment out if you want to examine the file.

    try (
        WritableMapHandle dstHandle
          = WritableMemory.writableMap(file, 0, bytes, ByteOrder.nativeOrder());
        WritableDirectHandle srcHandle = WritableMemory.allocateDirect(bytes)) {

      WritableMemory dstMem = dstHandle.get();
      WritableMemory srcMem = srcHandle.get();

      for (long i = 0; i < (longs); i++) {
        srcMem.putLong(i << 3, i); //load source with consecutive longs
      }

      srcMem.copyTo(0, dstMem, 0, srcMem.getCapacity()); //off-heap to off-heap copy

      dstHandle.force(); //push any remaining to the file

      //check end value
      assertEquals(dstMem.getLong((longs - 1L) << 3), longs - 1L);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMapException() throws Exception {
    File dummy = createFile("dummy.txt", ""); //zero length
    Memory.map(dummy, 0, dummy.length(), ByteOrder.nativeOrder());
  }



  @Test(expectedExceptions = ReadOnlyException.class)
  public void simpleMap2() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (WritableMapHandle rh = WritableMemory.writableMap(file)) {
      //rh.close();
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

  @Test
  public void checkOffsetNCapacity() {
    try {
      checkOffsetAndCapacity(-1, 1);
      fail();
    } catch (IllegalArgumentException e) {
      //OK
    }

    try {
      checkOffsetAndCapacity(0, 0);
      fail();
    } catch (IllegalArgumentException e) {
      //OK
    }

    try {
      checkOffsetAndCapacity(Long.MAX_VALUE, 2L);
      fail();
    } catch (IllegalArgumentException e) {
      //OK
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
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s String to print
   */
  static void println(final String s) {
    //System.out.println(s);
  }
}
