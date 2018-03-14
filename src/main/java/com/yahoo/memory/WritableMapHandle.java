/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.File;

/**
 * Gets a WritableMemory for a writable memory-mapped file resource. It is highly recommended
 * that this be created inside a <i>try-with-resources</i> statement.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
//Joins a WritableHandle with an AutoCloseable WritableMap resource
public final class WritableMapHandle extends MapHandle implements WritableMap, WritableHandle {

  private WritableMapHandle(final AllocateDirectWritableMap dirWmap,
      final BaseWritableMemoryImpl wMem) {
    super(dirWmap, wMem);
  }

  @SuppressWarnings("resource") //called from memory. state: RRO, cap, BO
  static WritableMapHandle map(final ResourceState state, final File file, final long fileOffset) {
    final AllocateDirectWritableMap dirMap = AllocateDirectWritableMap.map(state, file, fileOffset);
    final BaseWritableMemoryImpl wMem = new WritableMemoryImpl(state, false);
    return new WritableMapHandle(dirMap, wMem);
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  @Override
  public void force() {
    ((AllocateDirectWritableMap)dirMap).force();
  }
}
