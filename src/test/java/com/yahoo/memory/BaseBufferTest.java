/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class BaseBufferTest {

  @Test
  public void checkLimits() {
    Buffer buf = Memory.wrap(new byte[100]).asBuffer();
    buf.setStartPositionEnd(40, 45, 50);
    buf.setStartPositionEnd(0, 0, 100);
    try {
      buf.setStartPositionEnd(0, 0, 101);
      fail();
    } catch (AssertionError e) {
      //ok
    }
  }

  @Test
  public void checkLimitsAndCheck() {
    Buffer buf = Memory.wrap(new byte[100]).asBuffer();
    buf.setAndCheckStartPositionEnd(40, 45, 50);
    buf.setAndCheckStartPositionEnd(0, 0, 100);
    try {
      buf.setAndCheckStartPositionEnd(0, 0, 101);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
    buf.setAndCheckPosition(100);
    try {
      buf.setAndCheckPosition(101);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
    buf.setPosition(99);
    buf.incrementAndCheckPosition(1L);
    try {
      buf.incrementAndCheckPosition(1L);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void checkCheckValid() {
    WritableMemory wmem;
    Buffer buf;
    try (WritableDirectHandle hand = WritableMemory.allocateDirect(100)) {
      wmem = hand.get();
      buf = wmem.asBuffer();
    }
    try {
      @SuppressWarnings("unused")
      Memory mem = buf.asMemory();
      fail();
    } catch (IllegalStateException e) {
      //ok
    }
  }
}
