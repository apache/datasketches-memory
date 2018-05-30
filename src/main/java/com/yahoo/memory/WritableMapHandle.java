/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a WritableMemory for a writable memory-mapped file resource. It is highly recommended
 * that this be created inside a <i>try-with-resources</i> statement.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
//Joins a WritableHandle with an AutoCloseable WritableMap resource
public final class WritableMapHandle extends MapHandle implements WritableMap, WritableHandle {

  WritableMapHandle(final AllocateDirectWritableMap dirWmap,
      final BaseWritableMemoryImpl wMem) {
    super(dirWmap, wMem);
  }

  @Override
  public WritableMemory get() {
    return (WritableMemory) super.get();
  }

  @Override
  public void force() {
    ((AllocateDirectWritableMap)dirMap).force();
  }
}
