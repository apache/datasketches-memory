/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;


/**
 * @author Lee Rhodes
 */
public class UnsafeUtilTest {
  public final long testField = 1;

  @Test
  public void checkJDK7methods() {
    try {
      final byte[] byteArr = new byte[16];
      byteArr[0] = (byte) 1;
      final long one = JDK7Compatible.getAndAddLong(byteArr, 16, 1L);
      assertEquals(one, 1L);

      final long two = JDK7Compatible.getAndSetLong(byteArr,  16, 3L);
      assertEquals(two, 2L);
      assertEquals(byteArr[0], 3);

    } catch (Exception e) {
      throw new RuntimeException("Failed");
    }
  }

  @Test
  public void checkJdkString() {
    String jdkVer;
    int[] p = new int[2];
    String[] good1_8Strings = {"1.8.0_121", "1.8.0_162", "9.0.4", "10.0.1", "11", "12b", "12_.2"};
    int len = good1_8Strings.length;
    for (int i = 0; i < len; i++) {
      jdkVer = good1_8Strings[i];
      p = UnsafeUtil.parseJavaVersion(jdkVer);
      assertTrue(UnsafeUtil.checkJavaVersion(jdkVer, p[0], p[1]));
    }
    jdkVer = "1.7.0_80";
    p = UnsafeUtil.parseJavaVersion(jdkVer);
    assertFalse(UnsafeUtil.checkJavaVersion(jdkVer, p[0], p[1]));
    try { //valid string but < 1.7
      jdkVer = "1.6.0_65";
      p = UnsafeUtil.parseJavaVersion(jdkVer);
      UnsafeUtil.checkJavaVersion(jdkVer, p[0], p[1]); //throws
      fail();
    } catch (Error e) {
      println("" + e);
    }
    try { //invalid string
      jdkVer = "b";
      p = UnsafeUtil.parseJavaVersion(jdkVer);
      UnsafeUtil.checkJavaVersion(jdkVer, p[0], p[1]); //throws
      fail();
    } catch (Exception | Error e) {
      println("" + e);
    }
  }

  @Test
  public void checkFieldOffset() {
    long offset = UnsafeUtil.getFieldOffset(this.getClass(), "testField");
    assertEquals(offset, 16);
    try {
      offset = UnsafeUtil.getFieldOffset(this.getClass(), "testField2");
      fail();
    } catch (IllegalStateException e) {
      //OK
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkInts() {
    Ints.checkedCast(1L << 32);
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s String to print
   */
  static void println(final String s) {
    //System.out.println(s);
  }

}
