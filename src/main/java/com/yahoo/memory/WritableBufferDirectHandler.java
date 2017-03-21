/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a WritableBuffer for direct memory write operations
 * @author Lee Rhodes
 */
//Implements combo of WritableBuffer with writable AllocateDirect resource
public class WritableBufferDirectHandler implements AutoCloseable {
  AllocateDirect direct;
  WritableBuffer wBuf;

  WritableBufferDirectHandler(final AllocateDirect direct, final WritableBuffer wBuf) {
    this.direct = direct;
    this.wBuf = wBuf;
  }

  @SuppressWarnings("resource")
  static WritableBufferDirectHandler allocDirect(final ResourceState state) {
    final AllocateDirect direct = AllocateDirect.allocDirect(state);
    final WritableBufferImpl wBuf = new WritableBufferImpl(state);
    return new WritableBufferDirectHandler(direct, wBuf);
  }

  /**
   * Return a WritableBuffer for direct memory write operations
   * @return a WritableBuffer for direct memory write operations
   */
  public WritableBuffer get() {
    return wBuf;
  }

  @Override
  public void close() {
    direct.close();
  }

}
