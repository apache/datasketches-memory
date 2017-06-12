/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class ResourceStateTest {

  @Test
  public void checkBaseBufferAndState() {
    ResourceState state = new ResourceState();
    state.putCapacity(1 << 20);
    assertTrue(state.getBaseBuffer() == null);
    BaseBuffer baseBuf = new BaseBuffer(state);
    assertTrue(state.getBaseBuffer() != null);
    assertEquals(baseBuf.getEnd(), 1 << 20);
    state.putCapacity(0);

    try {
      state.putCapacity(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void checkByteOrder() {
    ResourceState state = new ResourceState();
    assertEquals(state.order(), ByteOrder.nativeOrder());
    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
      state.order(ByteOrder.BIG_ENDIAN);
    } else {
      state.order(ByteOrder.LITTLE_ENDIAN);
    }
    assertTrue(state.order() != ByteOrder.nativeOrder());
    assertTrue(state.isSwapBytes());
  }

  @Test
  public void checkExceptions() {
    ResourceState state = new ResourceState();

    try {
      state.putUnsafeObject(null);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }


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
      state.putRandomAccessFile(null);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }

    try {
      state.putMappedByteBuffer(null);
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
    checkStepBoolean(true);
    checkStepBoolean(false);
  }

  private static void checkStepBoolean(boolean init) {
    StepBoolean step = new StepBoolean(init);
    assertTrue(step.get() == init); //confirm init
    assertTrue(step.change());      //1st change was successful
    assertTrue(step.get() != init); //confirm it is different from init
    assertTrue(step.hasChanged());  //confirm it was changed from init
    assertFalse(step.change());     //2nd change, not successful
    assertTrue(step.get() != init); //Still different from init
    assertTrue(step.hasChanged());  //confirm it was changed from initial value
  }

  @Test
  public void checkPrim() {
    assertEquals(Prim.DOUBLE.scale(), ARRAY_DOUBLE_INDEX_SCALE);
  }

}
