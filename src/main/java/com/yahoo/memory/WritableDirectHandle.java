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
//Joins a WritableMemory with a writable, AutoCloseable AllocateDirect resource
public final class WritableDirectHandle implements WritableHandle {
  private static final MemoryRequestServer defaultMemReqSvr = new DefaultMemoryRequestServer();

  AllocateDirect direct;
  WritableMemory wMem;
  MemoryRequestServer memReqSvr = defaultMemReqSvr;

  WritableDirectHandle(final AllocateDirect allocatedDirect, final WritableMemory wMem,
      final MemoryRequestServer memReqSvr) {
    direct = allocatedDirect;
    this.wMem = wMem;
    this.memReqSvr = (memReqSvr != null) ? memReqSvr : defaultMemReqSvr;
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  //AutoCloseable

  @Override
  public void close() {
    if (direct != null) { //may already be invalid
      direct.close();
      direct = null;
      wMem.zeroNativeBaseOffset();
    }
  }
}
