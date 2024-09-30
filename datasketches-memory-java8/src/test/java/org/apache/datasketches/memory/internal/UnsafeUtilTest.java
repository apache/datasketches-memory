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

package org.apache.datasketches.memory.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class UnsafeUtilTest {
  long testField = 1; //Do not remove & cannot be static. Used in reflection check.

  @Test
  public void checkFieldOffset() {
    assertEquals(testField, 1);
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

  @SuppressWarnings("restriction")
  @Test
  public void checkArrayBaseOffset()
  {
    final List<Class<?>> classes = new ArrayList<>();
    classes.add(byte[].class);
    classes.add(int[].class);
    classes.add(long[].class);
    classes.add(float[].class);
    classes.add(double[].class);
    classes.add(boolean[].class);
    classes.add(short[].class);
    classes.add(char[].class);
    classes.add(Object[].class);
    classes.add(byte[][].class); // An array type that is not cached

    for (Class<?> clazz : classes) {
      assertEquals(
          UnsafeUtil.getArrayBaseOffset(clazz),
          UnsafeUtil.unsafe.arrayBaseOffset(clazz),
          clazz.getTypeName()
      );
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
  }

  /**
   * @param s String to print
   */
  static void println(final String s) {
    //System.out.println(s);
  }

}
