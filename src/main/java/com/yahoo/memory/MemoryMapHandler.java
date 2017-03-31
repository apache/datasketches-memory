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
//Implements combo of Memory with Map resource.
public class MemoryMapHandler implements Map {
  AllocateDirectMap dirMap;
  WritableMemoryImpl wMem;

  MemoryMapHandler(final AllocateDirectMap dirMap, final WritableMemoryImpl wMem) {
    this.dirMap = dirMap;
    this.wMem = wMem;
  }

  @SuppressWarnings("resource") //called from memory
  static MemoryMapHandler map(final ResourceState state) throws Exception {
    final AllocateDirectMap dirMap = AllocateDirectMap.map(state);
    final WritableMemoryImpl wMem = new WritableMemoryImpl(state);
    return new MemoryMapHandler(dirMap, wMem);
  }

  /**
   * Gets a Memory for a map resource
   * @return a Memory for a map resource
   */
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
