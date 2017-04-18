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
//Joins a WritableHandler with writable, AutoCloseable AllocateDirect resource
public class WritableDirectHandler implements AutoCloseable, MemoryRequest, WritableHandler {
  AllocateDirect direct = null;
  WritableMemory wMem = null;

  protected WritableDirectHandler(final AllocateDirect direct, final WritableMemory wMem) {
    this.direct = direct;
    this.wMem = wMem;
  }

  /**
   * Factory method used for the allocation of a direct resource passing the given ResourceState
   * @param state the given ResourceState
   * @return WritableDirectHandler
   */
  @SuppressWarnings("resource")
  static WritableDirectHandler allocDirect(final ResourceState state) {
    final AllocateDirect direct = AllocateDirect.allocate(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    final WritableDirectHandler handler = new WritableDirectHandler(direct, wMem);
    state.setMemoryRequest(handler);
    return handler;
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

  //MemoryRequest

  @Override
  public WritableMemory request(final long capacityBytes) {
    final WritableMemory wmem = WritableMemory.allocate((int)capacityBytes); //default allocate on heap
    wMem = wmem;
    wmem.setMemoryRequest(this);
    return wmem;
  }

  @Override
  public void requestClose(final WritableMemory memoryToClose, final WritableMemory newMemory) {
    direct.close();
  }
}
