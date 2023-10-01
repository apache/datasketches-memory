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

import static org.apache.datasketches.memory.internal.TestUtil.gettysPath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.Memory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AllocateDirectMapMemoryTest {
  private static final String LS = System.getProperty("line.separator");

  @BeforeClass
  public void setReadOnly() {
    UtilTest.setGettysburgAddressFileToReadOnly();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void simpleMap() {
    File file = gettysPath.resolve("GettysburgAddress.txt").toFile();
    assertTrue(AllocateDirectWritableMap.isFileReadOnly(file));
    try (Memory mem = Memory.map(file)) {
      mem.close(); //explicit close
    } //The Try-With-Resources will throw if already closed
  }

  @Test
  public void printGettysbergAddress()  {
    File file = gettysPath.resolve("GettysburgAddress.txt").toFile();
    try (Memory mem = Memory.map(file))
    {
      println("Mem Cap:       " + mem.getCapacity());
      println("Total Offset:  " + mem.getTotalOffset());
      println("Cum Offset:    " + ((ResourceImpl)mem).getCumulativeOffset(0));
      println("Total Offset: " + mem.getTotalOffset());
      StringBuilder sb = new StringBuilder();
      mem.getCharsFromUtf8(43, 176, sb);
      println(sb.toString());

      println("");
      Memory mem2 = mem.region(43 + 76, 20);
      println("Mem Cap:       " + mem2.getCapacity());
      println("Offset:        " + mem.getTotalOffset());
      println("Cum Offset:    " + ((ResourceImpl)mem2).getCumulativeOffset(0));
      println("Total Offset: " + mem2.getTotalOffset());
      StringBuilder sb2 = new StringBuilder();
      mem2.getCharsFromUtf8(0, 12, sb2);
      println(sb2.toString());
    }
  }

  @Test
  public void testIllegalArguments() {
    File file = gettysPath.resolve("GettysburgAddress.txt").toFile();
    try (Memory mem = Memory.map(file, -1, Integer.MAX_VALUE, ByteOrder.nativeOrder())) {
      fail("Failed: Position was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }

    try (Memory mem = Memory.map(file, 0, -1, ByteOrder.nativeOrder())) {
      fail("Failed: Size was negative.");
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testAccessAfterClose() {
    File file = gettysPath.resolve("GettysburgAddress.txt").toFile();
    long memCapacity = file.length();
    try (Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      assertEquals(memCapacity, mem.getCapacity());
    } //normal close via TWR
    Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder());
    mem.close(); //normal manual close
    mem.getCapacity(); //isLoaded(); //already closed, invalid
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testReadFailAfterClose()  {
    File file = gettysPath.resolve("GettysburgAddress.txt").toFile();
    long memCapacity = file.length();
    Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder());
    mem.close();
    mem.isLoaded();
  }

  @Test
  public void testLoad()  {
    File file = gettysPath.resolve("GettysburgAddress.txt").toFile();
    long memCapacity = file.length();
    try (Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      mem.load();
      assertTrue(mem.isLoaded());
    } //normal TWR close
  }

  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
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
      System.out.print(o.toString()); //disable here
    }
  }

}
