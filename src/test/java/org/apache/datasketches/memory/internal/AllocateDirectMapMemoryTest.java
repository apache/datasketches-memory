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

import static org.apache.datasketches.memory.internal.ResourceImpl.LS;
import static org.apache.datasketches.memory.internal.Util.getResourceFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Memory;
import org.testng.annotations.Test;

public class AllocateDirectMapMemoryTest {

  @Test
  public void simpleMap() throws IOException {
    File file = UtilTest.setGettysburgAddressFileToReadOnly();
    Memory mem = null;
    try (Arena arena = Arena.ofConfined()) {
      mem = Memory.map(file, arena);
      arena.close();
    } //The Try-With-Resources will throw since it is already closed
    catch (IllegalStateException e) { /* OK */ }
    if (mem != null) { assertFalse(mem.isAlive()); }
  }

  @Test
  public void testIllegalArguments() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    Memory mem = null;
    try (Arena arena = Arena.ofConfined()) {
      mem = Memory.map(file, -1, Integer.MAX_VALUE, ByteOrder.nativeOrder(), arena);
      fail("Failed: test IllegalArgumentException: Position was negative.");
      mem.getCapacity();
    }
    catch (IllegalArgumentException e) { /* OK */ }
    if (mem != null) { assertFalse(mem.isAlive()); }
    try (Arena arena = Arena.ofConfined()) {
      mem = Memory.map(file, 0, -1, ByteOrder.nativeOrder(), arena);
      fail("Failed: test IllegalArgumentException: Size was negative.");
    }
    catch (IllegalArgumentException e) { /* OK */ }
    if (mem != null) { assertFalse(mem.isAlive()); }
  }

  @Test
  public void testMapAndMultipleClose() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    Memory mem = null;
    Memory mem2 = null;
    try {
      try (Arena arena = Arena.ofConfined()) {
        mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder(), arena);
        mem2 = mem;
        assertEquals(memCapacity, mem.getCapacity());
        arena.close();
        assertFalse(mem.isAlive());
      } //a close inside the TWR block will throw here
    }
    catch (IllegalStateException e) { /* expected */ }
    if (mem != null) { assertFalse(mem.isAlive()); }
    if (mem2 != null) { assertFalse(mem2.isAlive()); }
  }

  @Test
  public void testLoad() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    try (Arena arena = Arena.ofConfined()) {
      Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder(), arena);
      mem.load();
      //assertTrue(mem.isLoaded()); //incompatible with Windows
      assertTrue(mem.isAlive());
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
