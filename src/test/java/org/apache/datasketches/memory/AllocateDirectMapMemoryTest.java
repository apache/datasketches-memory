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

package org.apache.datasketches.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.nio.ByteOrder;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AllocateDirectMapMemoryTest {
  MapHandle hand = null;

  @BeforeClass
  public void setReadOnly() {
    UtilTest.setGettysburgAddressFileToReadOnly(this);
  }

  @Test
  public void simpleMap() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (MapHandle rh = Memory.map(file)) {
      rh.close();
    }
  }

  @Test
  public void testIllegalArguments() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    try (MapHandle rh = Memory.map(file, -1, Integer.MAX_VALUE, ByteOrder.nativeOrder())) {
      fail("Failed: testIllegalArgumentException: Position was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }

    try (MapHandle rh = Memory.map(file, 0, -1, ByteOrder.nativeOrder())) {
      fail("Failed: testIllegalArgumentException: Size was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void testMapAndMultipleClose() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (MapHandle rh = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      Memory map = rh.get();
      assertEquals(memCapacity, map.getCapacity());
      rh.close();
      rh.close();
      map.getCapacity(); //throws assertion error
    } catch (AssertionError e) {
      //OK
    }
  }

  @Test
  public void testReadFailAfterClose() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (MapHandle rh = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      Memory mmf = rh.get();
      rh.close();
      mmf.getByte(0);
    } catch (AssertionError e) {
      //OK
    }
  }

  @Test
  public void testLoad() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    try (MapHandle rh = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      rh.load();
      assertTrue(rh.isLoaded());
      rh.close();
    }
  }

  @Test
  public void testHandlerHandoffWithTWR() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    Memory mem;
    try (MapHandle rh = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      rh.load();
      assertTrue(rh.isLoaded());
      hand = rh;
      mem = rh.get();
    } //TWR closes
    assertFalse(mem.isValid());
    //println(""+mem.isValid());
  }

  @Test
  public void testHandoffWithoutClose() throws Exception {
    File file = new File(getClass().getClassLoader().getResource("GettysburgAddress.txt").getFile());
    long memCapacity = file.length();
    MapHandle rh = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder());
    rh.load();
    assertTrue(rh.isLoaded());
    hand = rh;
    //The receiver of the handler must close the resource, in this case it is the class.
  }

  @AfterClass
  public void afterAllTests() {
      if (hand != null) {
      Memory mem = hand.get();
      if ((mem != null) && mem.isValid()) {
        hand.close();
        assertFalse(mem.isValid());
      }
    }
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
