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
public final class WritableDirectHandle implements WritableHandle {
  AllocateDirect direct;
  WritableMemory wMem;

  WritableDirectHandle(final AllocateDirect direct, final WritableMemory wMem) {
    this.direct = direct;
    this.wMem = wMem;
  }

  @SuppressWarnings("resource")
  static WritableDirectHandle allocateDirect(final ResourceState state) {
    final AllocateDirect direct = AllocateDirect.allocate(state);
    final WritableMemory wMem = new WritableMemoryImpl(state, false);
    final WritableDirectHandle handle = new WritableDirectHandle(direct, wMem);
    state.setHandle(handle);
    return handle;
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  //AutoCloseable

  @Override
  public void close() {
    if (direct != null) {
      direct.close();
      direct = null;
    }
  }
}
