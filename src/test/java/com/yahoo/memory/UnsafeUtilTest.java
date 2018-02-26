/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;

import org.testng.annotations.Test;

import com.yahoo.memory.UnsafeUtil.JDKCompatibility;

/**
 * @author Lee Rhodes
 */
public class UnsafeUtilTest {

  @Test
  public void checkJDK7methods() {
    JDKCompatibility jdk7compatible;
    try {
      Class<?> inner = Class.forName("com.yahoo.memory.UnsafeUtil$JDK7Compatible");
      Constructor<?> innerConstructor =
          inner.getDeclaredConstructor(com.yahoo.memory.UnsafeUtil.unsafe.getClass());
      innerConstructor.setAccessible(true);
      jdk7compatible = (com.yahoo.memory.UnsafeUtil.JDKCompatibility)
          innerConstructor.newInstance(com.yahoo.memory.UnsafeUtil.unsafe);
      assertTrue(jdk7compatible != null);

      final byte[] byteArr = new byte[16];
      byteArr[0] = (byte) 1;
      byteArr[8] = (byte) 2;
      if (jdk7compatible != null) {
        final long two = jdk7compatible.getAndAddLong(byteArr, 16, 1L);
        assertEquals(two, 2L);

        final long one = jdk7compatible.getAndSetLong(byteArr,  16, 1L);
        assertEquals(one, 1L);
      } else {
        fail();
      }

    } catch (RuntimeException e) {
      throw new RuntimeException("Failed");
    } catch (Exception e) {
      throw new RuntimeException("Failed");
    }
  }

  @Test
  public void checkJdkString() {
    String[] jdkStr = {"1.7.0_80", "1.8.0_121", "1.8.0_162", "9.0.4", "10.0.1", "11",
        "12b", "12_.2"};
    int len = jdkStr.length;
    for (int i = 0; i < len; i++) {
      String jdkVer = jdkStr[i];
      UnsafeUtil.majorJavaVersion(jdkVer);
    }
    try { //valid but < 1.7
      UnsafeUtil.majorJavaVersion("1.6.0_65");
      fail();
    } catch (ExceptionInInitializerError e) {
      //println("" + e);
    }
    try { //invalid;
      UnsafeUtil.majorJavaVersion("b");
      fail();
    } catch (ExceptionInInitializerError e) {
      //println("" + e);
    }

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
