/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class MemoryStateTest {

  @Test
  public void checkPositional() {
    MemoryState state = new MemoryState();
    assertFalse(state.isPositional());
    state.setPositional(true);
    assertTrue(state.isPositional());
  }

  @Test
  public void checkOrder() {
    MemoryState state = new MemoryState();
    assertEquals(state.order(), ByteOrder.nativeOrder());
    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
      state.order(ByteOrder.BIG_ENDIAN);
    } else {
      state.order(ByteOrder.LITTLE_ENDIAN);
    }
    assertTrue(state.order() != ByteOrder.nativeOrder());
    assertTrue(state.swapBytes());
  }

  @Test
  public void checkExceptions() {
    MemoryState state = new MemoryState();
    try {
      state.putUnsafeObjectHeader( -16L);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }

    try {
      state.putByteBuffer(null);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }

    try {
      state.putFile(null);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }

    try {
      state.putRegionOffset( -16L);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  //StepBoolean checks
  @Test
  public void checkStepBoolean() {
    StepBoolean step = new StepBoolean(false);
    step.change();
    assertTrue(step.hasChanged());
  }



}
