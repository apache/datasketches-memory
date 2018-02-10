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
public final class WritableMapHandle extends MapHandle implements WritableMap, WritableHandle
{
  AllocateDirectWritableMap dirWmap;
  WritableMemoryImpl wMemImpl;

  private WritableMapHandle(final AllocateDirectWritableMap dirWmap, final WritableMemoryImpl wMem) {
    super(dirWmap, wMem);
    this.dirWmap = dirWmap;
    wMemImpl = wMem;
  }

  @SuppressWarnings("resource") //called from memory
  static WritableMapHandle map(final ResourceState state) throws Exception {
    final AllocateDirectWritableMap dirMap = AllocateDirectWritableMap.map(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    return new WritableMapHandle(dirMap, wMem);
  }

  @Override
  public WritableMemory get() {
    return wMemImpl;
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
