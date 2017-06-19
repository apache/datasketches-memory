/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * References a WritableMemory for a writable direct memory resource. It is highly recommended that
 * this be created inside a <i>try-with-resources</i> statement.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
//Joins a WritableHandle with writable, AutoCloseable AllocateDirect resource
public class WritableDirectHandle implements AutoCloseable, WritableHandle {
  AllocateDirect direct = null;
  WritableMemory wMem = null;

  public WritableDirectHandle(final AllocateDirect direct, final WritableMemory wMem) {
    this.direct = direct;
    this.wMem = wMem;
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  //AutoCloseable

  @Override
  public void close() {
    if ((direct != null) && (direct.state.isValid())) {
      direct.close();
      direct = null;
    }
  }
}
