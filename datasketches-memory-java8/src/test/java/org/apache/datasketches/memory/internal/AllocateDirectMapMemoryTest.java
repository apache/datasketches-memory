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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

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
  public void simpleMap() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    assertTrue(AllocateDirectWritableMap.isFileReadOnly(file));
    try (Memory mem = Memory.map(file)) {
      mem.close(); //explicit close
    } //The Try-With-Resources will throw if already closed
  }

  @Test
  public void printGettysbergAddress() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    try (Memory mem = Memory.map(file))
    {
      int len1 = (int)mem.getCapacity();
      println("Mem Cap:       " + len1);
      println("Total Offset:  " + mem.getTotalOffset());
      println("Cum Offset:    " + ((ResourceImpl)mem).getCumulativeOffset(0));
      println("Total Offset: " + mem.getTotalOffset());
      byte[] bArr = new byte[len1];
      mem.getByteArray(0, bArr, 0, len1);
      String s = new String(bArr, StandardCharsets.UTF_8);
      println(s);

      println("");
      Memory mem2 = mem.region(43 + 76, 34);
      int len2 = (int)mem2.getCapacity();
      println("Mem Cap:       " + len2);
      println("Offset:        " + mem.getTotalOffset());
      println("Cum Offset:    " + ((ResourceImpl)mem2).getCumulativeOffset(0));
      println("Total Offset: " + mem2.getTotalOffset());
      byte[] bArr2 = new byte[len2];
      mem2.getByteArray(0, bArr2, 0, len2);
      String s2 = new String(bArr2,StandardCharsets.UTF_8);
      println(s2);
      assertEquals(s2,"a new nation, conceived in Liberty");
    }
  }

  @Test
  public void testIllegalArguments() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
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
  public void testAccessAfterClose() throws IOException {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    try (Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder())) {
      assertEquals(memCapacity, mem.getCapacity());
    } //normal close via TWR
    Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder());
    mem.close(); //normal manual close
    mem.getCapacity(); //isLoaded(); //already closed, invalid
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testReadFailAfterClose() throws IOException  {
    File file = getResourceFile("GettysburgAddress.txt");
    long memCapacity = file.length();
    Memory mem = Memory.map(file, 0, memCapacity, ByteOrder.nativeOrder());
    mem.close();
    mem.isLoaded();
  }

  @Test
  public void testLoad() throws IOException  {
    File file = getResourceFile("GettysburgAddress.txt");
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
      //System.out.print(o.toString()); //disable here
    }
  }

}
