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

import static org.apache.datasketches.memory.internal.Util.getResourceFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Memory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

public class AllocateDirectMapMemoryTest {
  private static final String LS = System.getProperty("line.separator");

  @Test
  public void simpleMap() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    file.setReadOnly();
    Memory mem = null;
    try (ResourceScope scope = (mem = Memory.map(file)).scope()) {
    }
    assertFalse(mem.isAlive());
  }

  @Test
  public void testIllegalArguments() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    Memory mem = null;
    try (ResourceScope scope = (mem = Memory.map(file, -1, Integer.MAX_VALUE, ByteOrder.nativeOrder())).scope();) {
      fail("Failed: test IllegalArgumentException: Position was negative.");
      mem.getCapacity();
    }
    catch (IllegalArgumentException e) {
      //ok
    }
    try (ResourceScope scope = (mem = Memory.map(file, 0, -1, ByteOrder.nativeOrder())).scope()) {
      fail("Failed: testIllegalArgumentException: Size was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void testMapAndMultipleClose() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    Memory mem = null;
    try (ResourceScope scope = (mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())).scope()) {
      assertEquals(memCapacity, mem.getCapacity());
      //mem.close(); //a close inside the TWR block will throw exception when the TWR block ends
    }
    mem.close(); //multiple closes outside the TWR block are OK, but unnecessary
    mem.close();
  }

  @Test
  public void testLoad() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    Memory mem = null;
    try (ResourceScope scope = (mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())).scope()) {
      mem.load();
      assertTrue(mem.isLoaded());
    }
  }

  @SuppressWarnings("resource")
  @Test
  public void testHandleHandoff() throws Exception {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder());
    ResourceScope scope = mem.scope();
    ResourceScope.Handle handle = scope.acquire();
    try {
      mem.load();
      assertTrue(mem.isLoaded());
    } finally {
      mem.scope().release(handle);
    }
    assertTrue(mem.isAlive());
    mem.close(); //handle must be released before close
    assertFalse(mem.isAlive());
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
