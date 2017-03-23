/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class AllocateDirectMapBufferTest {
  BufferMapHandler hand = null;

  @Test
  public void simpleMap() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (BufferMapHandler rh = Buffer.map(file)) {
      rh.close();
    }
  }

  @Test
  public void testIllegalArguments() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (BufferMapHandler rh = Buffer.map(file, -1, Integer.MAX_VALUE)) {
      fail("Failed: testIllegalArgumentException: Position was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }

    try (BufferMapHandler rh = Buffer.map(file, 0, -1)) {
      fail("Failed: testIllegalArgumentException: Size was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }

    try (BufferMapHandler rh = Buffer.map(file, Long.MAX_VALUE, 2)) {
      fail("Failed: testIllegalArgumentException: Sum of position + size is negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void testMapAndMultipleClose() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (BufferMapHandler rh = Buffer.map(file, 0, memCapacity)) {
      Buffer map = rh.get();
      assertEquals(memCapacity, map.getCapacity());
      rh.close();
      rh.close();
      map.getCapacity(); //throws assertion error
    } catch (AssertionError e) {
      //OK
    }
    assertEquals(AllocateDirectMap.pageCount(1, 16), 16); //check pageCounter
  }

  @Test
  public void testReadFailAfterClose() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (BufferMapHandler rh = Buffer.map(file, 0, memCapacity)) {
      Buffer mmf = rh.get();
      rh.close();
      mmf.getByte();
    } catch (AssertionError e) {
      //OK
    }
  }

  @Test
  public void testLoad() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (BufferMapHandler rh = Buffer.map(file, 0, memCapacity)) {
      rh.load();
      assertTrue(rh.isLoaded());
      rh.close();
    }
  }

  @Test
  public void testHandlerHandoffWithTWR() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (BufferMapHandler rh = Buffer.map(file, 0, memCapacity)) {
      rh.load();
      assertTrue(rh.isLoaded());
      hand = rh;
    } //TWR closes
    Buffer mem = hand.get();
    assertFalse(mem.isValid());
    //println(""+mem.isValid());
  }

  @Test
  public void testHandoffWithoutClose() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    BufferMapHandler rh = Buffer.map(file, 0, memCapacity);
    rh.load();
    assertTrue(rh.isLoaded());
    hand = rh;
    //The receiver of the handler must close the resource, in this case it is the class.
  }

  @AfterClass
  public void afterAllTests() {
    Buffer mem = hand.get();
    if (mem.isValid()) {
      hand.close();
    }
    assertFalse(mem.isValid());
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }
}
