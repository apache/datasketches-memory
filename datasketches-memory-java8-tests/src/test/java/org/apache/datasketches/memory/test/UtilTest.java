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

import static org.apache.datasketches.memory.internal.Util.characterPad;
import static org.apache.datasketches.memory.internal.Util.getResourceBytes;
import static org.apache.datasketches.memory.internal.Util.getResourceFile;
import static org.apache.datasketches.memory.internal.Util.negativeCheck;
import static org.apache.datasketches.memory.internal.Util.nullCheck;
import static org.apache.datasketches.memory.internal.Util.zeroCheck;
import static org.apache.datasketches.memory.internal.Util.zeroPad;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.datasketches.memory.internal.UnsafeUtil;
import org.apache.datasketches.memory.internal.Util;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class UtilTest {
  private static final String LS = System.getProperty("line.separator");

  //Binary Search
  @Test
  public void checkBinarySearch() {
    int k = 1024; //longs
    WritableMemory wMem = WritableMemory.allocate(k << 3); //1024 longs
    for (int i = 0; i < k; i++) { wMem.putLong(i << 3, i); }
    long idx = Util.binarySearchLongs(wMem, 0, k - 1, k / 2);
    long val = wMem.getLong(idx << 3);
    assertEquals(idx, k/2);
    assertEquals(val, k/2);

    idx = Util.binarySearchLongs(wMem, 0, k - 1, k);
    assertEquals(idx, -1024);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkBoundsTest() {
    UnsafeUtil.checkBounds(999, 2, 1000);
  }

  @Test
  public void checkPadding() {
    String s = "123";
    String t = zeroPad(s, 4);
    assertTrue(t.startsWith("0"));

    t = characterPad(s, 4, '0', true);
    assertTrue(t.endsWith("0"));

    t = characterPad(s, 3, '0', false);
    assertEquals(s, t);
  }

  @Test
  public void checkNullZeroNegativeChecks() {
    Object obj = null;
    try {
      nullCheck(obj, "Test Object");
      fail();
    } catch (IllegalArgumentException e) {
      //OK
    }
    try {
      zeroCheck(0, "Test Long");
      fail();
    } catch (IllegalArgumentException e) {
      //OK
    }
    try {
      negativeCheck(-1L, "Test Long");
      fail();
    } catch (IllegalArgumentException e) {
      //OK
    }
  }

  @Test
  public void checkCodePointArr() {
    final Util.RandomCodePoints rvcp = new Util.RandomCodePoints(true);
    final int n = 1000;
    final int[] cpArr = new int[n];
    rvcp.fillCodePointArray(cpArr);
    for (int i = 0; i < n; i++) {
      int cp = cpArr[i];
      if ((cp >= Character.MIN_SURROGATE) && (cp <= Character.MAX_SURROGATE)) {
        fail();
      }
    }
  }

  @Test
  public void checkCodePoint() {
    final Util.RandomCodePoints rvcp = new Util.RandomCodePoints(true);
    final int n = 1000;
    for (int i = 0; i < n; i++) {
      int cp = rvcp.getCodePoint();
      if ((cp >= Character.MIN_SURROGATE) && (cp <= Character.MAX_SURROGATE)) {
        fail();
      }
    }
  }

  static final String getFileAttributes(File file) {
    try {
    PosixFileAttributes attrs = Files.getFileAttributeView(
        file.toPath(), PosixFileAttributeView.class, new LinkOption[0]).readAttributes();
    String s = String.format("%s: %s %s %s%n",
        file.getPath(),
        attrs.owner().getName(),
        attrs.group().getName(),
        PosixFilePermissions.toString(attrs.permissions()));
    return s;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static final void setGettysburgAddressFileToReadOnly() {
    File file = getResourceFile("GettysburgAddress.txt");
    try {
    Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("r--r--r--"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  //Resources

  @Test
  public void resourceFileExits() {
    final String shortFileName = "GettysburgAddress.txt";
    final File file = getResourceFile(shortFileName);
    assertTrue(file.exists());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void resourceFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    getResourceFile(shortFileName + "123");
  }

  @Test
  public void resourceBytesCorrect() {
    final String shortFileName = "GettysburgAddress.txt";
    final byte[] bytes = getResourceBytes(shortFileName);
    assertTrue(bytes.length == 1541);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void resourceBytesFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    getResourceBytes(shortFileName + "123");
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
