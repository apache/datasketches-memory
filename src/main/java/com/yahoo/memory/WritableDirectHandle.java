/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * A Handle for a writable direct memory resource.
 * Please read Javadocs for {@link Handle}.
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
//Joins a WritableMemory with a writable, AutoCloseable AllocateDirect resource
public final class WritableDirectHandle implements WritableHandle {

  /**
   * Having at least one final field makes this class safe for concurrent publication.
   */
  final AllocateDirect direct;
  private WritableMemory wMem;

  WritableDirectHandle(final AllocateDirect allocatedDirect, final WritableMemory wMem) {
    direct = allocatedDirect;
    this.wMem = wMem;
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  //AutoCloseable

  @Override
  public void close() {
    if (direct.doClose()) {
      wMem = null;
    }
  }
}
