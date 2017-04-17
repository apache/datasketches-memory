/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a WritableMemory for a writable direct memory resource. It is highly recommended that
 * this be created inside a <i>try-with-resources</i> statement. This implements a very simple
 * MemoryRequest management function that just allocates any request onto the heap. This class can
 * be overridden if more sophisticated memory management is required.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
//Implements combination of WritableMemory with writable AllocateDirect resource
public class WritableMemoryDirectHandler implements AutoCloseable, MemoryRequest {
  AllocateDirect direct = null;
  WritableMemory wMem = null;

  WritableMemoryDirectHandler(final AllocateDirect direct, final WritableMemory wMem) {
    this.direct = direct;
    this.wMem = wMem;
  }

  /**
   * Factory method used for the allocation of a direct resource passing the given ResourceState
   * @param state the given ResourceState
   * @return WritableMemroyDirectHandler
   */
  @SuppressWarnings("resource")
  static WritableMemoryDirectHandler allocDirect(final ResourceState state) {
    final AllocateDirect direct = AllocateDirect.allocate(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    final WritableMemoryDirectHandler handler = new WritableMemoryDirectHandler(direct, wMem);
    state.setMemoryRequest(handler);
    return handler;
  }

  /**
   * Return a WritableMemory for direct memory write operations
   * @return a WritableMemory for direct memory write operations
   */
  public WritableMemory get() {
    return wMem;
  }

  //AutoCloseable

  @Override
  public void close() {
    direct.close();
  }

  //MemoryRequest

  @Override
  public WritableMemory request(final long capacityBytes) {
    final WritableMemory mem = WritableMemory.allocate((int)capacityBytes); //default allocate on heap
    mem.setMemoryRequest(this);
    return mem;
  }

  @Override
  public void requestClose() {
    if (direct.state.isValid()) {
      close();
    }
  }
}
