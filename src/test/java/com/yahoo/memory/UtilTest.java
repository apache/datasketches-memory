/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.Util.characterPad;
import static com.yahoo.memory.Util.negativeCheck;
import static com.yahoo.memory.Util.nullCheck;
import static com.yahoo.memory.Util.zeroCheck;
import static com.yahoo.memory.Util.zeroPad;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

public class UtilTest {

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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkByteOrderNull() {
    Util.isNativeOrder(null);
    fail();
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
