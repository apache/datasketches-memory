/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import java.nio.ByteBuffer;

import org.testng.annotations.Test;

public class WritableMemoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrapWrongEndian() {
    ByteBuffer bb = ByteBuffer.allocate(64);
    WritableMemory.wrap(bb);
  }



}
