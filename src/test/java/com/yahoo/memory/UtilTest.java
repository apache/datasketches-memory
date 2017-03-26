/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.Util.characterPad;
import static com.yahoo.memory.Util.zeroPad;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
