/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a a WritableMemory for direct memory write operations
 * @author Lee Rhodes
 */
public interface WritableMemoryDirectHandler extends AutoCloseable {

  @Override
  void close();

  /**
   * Return a WritableMemory for direct memory write operations
   * @return a WritableMemory for direct memory write operations
   */
  WritableMemory get();

}
