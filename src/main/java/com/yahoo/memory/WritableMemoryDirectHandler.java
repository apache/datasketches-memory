/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a WritableMemory for direct memory write operations
 * @author Lee Rhodes
 */
//Implements combo of WritableMemory with writable AllocateDirect resource
public class WritableMemoryDirectHandler implements AutoCloseable {
  AllocateDirect direct;
  WritableMemory wMem;

  WritableMemoryDirectHandler(final AllocateDirect direct, final WritableMemory wMem) {
    this.direct = direct;
    this.wMem = wMem;
  }

  @SuppressWarnings("resource")
  static WritableMemoryDirectHandler allocDirect(final ResourceState state) {
    final AllocateDirect direct = AllocateDirect.allocDirect(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    return new WritableMemoryDirectHandler(direct, wMem);
  }

  /**
   * Return a WritableMemory for direct memory write operations
   * @return a WritableMemory for direct memory write operations
   */
  public WritableMemory get() {
    return wMem;
  }

  @Override
  public void close() {
    direct.close();
  }

}
