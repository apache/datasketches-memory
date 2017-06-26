/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a Memory for a memory-mapped, read-only file resource, It is highly recommended that this
 * be created inside a <i>try-with-resources</i> statement.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
//Joins a Read-only Handle with an AutoCloseable Map resource.
public final class MapHandle implements Map, Handle {
  AllocateDirectMap dirMap;
  WritableMemoryImpl wMem;

  private MapHandle(final AllocateDirectMap dirMap, final WritableMemoryImpl wMem) {
    this.dirMap = dirMap;
    this.wMem = wMem;
  }

  @SuppressWarnings("resource") //called from memory
  static MapHandle map(final ResourceState state) throws Exception {
    final AllocateDirectMap dirMap = AllocateDirectMap.map(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    return new MapHandle(dirMap, wMem);
  }

  @Override
  public Memory get() {
    return wMem;
  }

  @Override
  public void close() {
    dirMap.close();
  }

  @Override
  public void load() {
    dirMap.load();
  }

  @Override
  public boolean isLoaded() {
    return dirMap.isLoaded();
  }

}
