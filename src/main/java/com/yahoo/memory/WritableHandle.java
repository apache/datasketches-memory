/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * A Handle for writable direct memory or a memory-mapped, writable file resource.
 * Please read Javadocs for {@link Handle}.
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
public interface WritableHandle extends Handle {

  /**
   * Gets a WritableMemory
   * @return a WritableMemory
   */
  @Override
  WritableMemory get();
}
