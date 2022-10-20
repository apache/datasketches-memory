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

import static org.apache.datasketches.memory.internal.TestUtils.getResourceFile;
import static org.apache.datasketches.memory.internal.TestUtils.getResourceBytes;
import static org.apache.datasketches.memory.internal.TestUtils.zeroPad;
import static org.apache.datasketches.memory.internal.TestUtils.characterPad;
import static org.apache.datasketches.memory.internal.TestUtils.nullCheck;
import static org.apache.datasketches.memory.internal.Util.negativeCheck;
import static org.apache.datasketches.memory.internal.Util.zeroCheck;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;

import org.apache.datasketches.memory.DefaultMemoryFactory;
import org.apache.datasketches.memory.WritableMemory;
import org.apache.datasketches.memory.internal.unsafe.UnsafeUtil;
import org.testng.annotations.Test;

public class UtilTest {
  //Binary Search
  @Test
  public void checkBinarySearch() {
    int k = 1024; //longs
    WritableMemory wMem = DefaultMemoryFactory.DEFAULT.allocate(k << 3); //1024 longs
    for (int i = 0; i < k; i++) { wMem.putLong(i << 3, i); }
    long idx = TestUtils.binarySearchLongs(wMem, 0, k - 1, k / 2);
    long val = wMem.getLong(idx << 3);
    assertEquals(idx, k/2);
    assertEquals(val, k/2);

    idx = TestUtils.binarySearchLongs(wMem, 0, k - 1, k);
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
    final RandomCodePoints rvcp = new RandomCodePoints(true);
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
    final RandomCodePoints rvcp = new RandomCodePoints(true);
    final int n = 1000;
    for (int i = 0; i < n; i++) {
      int cp = rvcp.getCodePoint();
      if ((cp >= Character.MIN_SURROGATE) && (cp <= Character.MAX_SURROGATE)) {
        fail();
      }
    }
  }

  //Resources

  @Test
  public void resourceFileExits() {
    final String shortFileName = "GettysburgAddress.txt";
    final File file = getResourceFile(shortFileName);
    assertTrue(file.exists());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void resourceFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    getResourceFile(shortFileName + "123");
  }

  @Test
  public void resourceBytesCorrect() {
    final String shortFileName = "GettysburgAddress.txt";
    final byte[] bytes = getResourceBytes(shortFileName);
    assertEquals(bytes.length, 1534 + (System.lineSeparator().length() * 7));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void resourceBytesFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    getResourceBytes(shortFileName + "123");
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  static void println(final Object o) {
    if (o == null) { print(TestUtils.LS); }
    else { print(o.toString() + TestUtils.LS); }
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
