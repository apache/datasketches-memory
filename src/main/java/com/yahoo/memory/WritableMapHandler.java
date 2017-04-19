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
//Joins a WritableHandler with an AutoCloseable WritableMap resource
public final class WritableMapHandler implements WritableMap, WritableHandle {
  AllocateDirectWritableMap dirWmap;
  WritableMemoryImpl wMem;

  private WritableMapHandler(final AllocateDirectWritableMap dirWmap, final WritableMemoryImpl wMem) {
    this.dirWmap = dirWmap;
    this.wMem = wMem;
  }

  @SuppressWarnings("resource") //called from memory
  static WritableMapHandler map(final ResourceState state) throws Exception {
    final AllocateDirectWritableMap dirMap = AllocateDirectWritableMap.map(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    return new WritableMapHandler(dirMap, wMem);
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  @Override
  public void close() {
    dirWmap.close();
  }

  @Override
  public void load() {
    dirWmap.load();
  }

  @Override
  public boolean isLoaded() {
    return dirWmap.isLoaded();
  }

  @Override
  public void force() {
    dirWmap.force();
  }

}
