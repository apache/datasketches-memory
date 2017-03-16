/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static com.yahoo.memory4.AllocateDirectWritableMap.checkOffsetAndCapacity;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.testng.annotations.Test;

import com.yahoo.memory4.ResourceHandler.ResourceType;

public class AllocateDirectWritableMapTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMapException() throws Exception {
    File dummy = createFile("dummy.txt", ""); //zero length
    Memory.map(dummy, 0, dummy.length());
  }

  @Test(expectedExceptions = ReadOnlyMemoryException.class)
  public void simpleMap2() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (WritableResourceHandler rh = WritableMemory.map(file)) {
      rh.close();
    }
  }

  @Test
  public void testForce() throws Exception {
    String origStr = "Corectng spellng mistks";
    File origFile = createFile("force_original.txt", origStr); //23
    origFile.setWritable(true, false);
    long origBytes = origFile.length();
    String correctStr = "Correcting spelling mistakes"; //28
    byte[] correctByteArr = correctStr.getBytes(UTF_8);
    long corrBytes = correctByteArr.length;

    try (ResourceHandler rh = Memory.map(origFile, 0, origBytes)) {
      Memory map = rh.get();
      rh.load();
      assertTrue(rh.isLoaded());
      //confirm orig string
      byte[] buf = new byte[(int)origBytes];
      map.getByteArray(0, buf, 0, (int)origBytes);
      String bufStr = new String(buf, UTF_8);
      assertEquals(bufStr, origStr);
    }

    try (WritableResourceHandler wrh = WritableMemory.map(origFile, 0, corrBytes)) { //longer
      WritableMemory wMap = wrh.get();
      ResourceType type = wrh.getResourceType();
      wrh.isResourceType(type);
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
}
