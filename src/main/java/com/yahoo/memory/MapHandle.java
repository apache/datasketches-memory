/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * A Handle for a memory-mapped, read-only file resource.
 * Please read Javadocs for {@link Handle}.
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
//Joins a Read-only Handle with an AutoCloseable Map resource.
public class MapHandle implements Map, Handle {
  /**
   * Having at least one final field makes this class safe for concurrent publication.
   */
  final AllocateDirectMap dirMap;
  private BaseWritableMemoryImpl wMem;

  MapHandle(final AllocateDirectMap dirMap, final BaseWritableMemoryImpl wMem) {
    this.dirMap = dirMap;
    this.wMem = wMem;
  }

  @Override
  public Memory get() {
    return wMem;
  }

  @Override
  public void close() {
    if (dirMap.doClose()) {
      wMem = null;
    }
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
