/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.File;
import java.nio.ByteOrder;

/**
 * Gets a Memory for a memory-mapped, read-only file resource, It is highly recommended that this
 * be created inside a <i>try-with-resources</i> statement.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
//Joins a Read-only Handle with an AutoCloseable Map resource.
public class MapHandle implements Map, Handle {
  AllocateDirectMap dirMap;
  BaseWritableMemoryImpl wMem;

  MapHandle(final AllocateDirectMap dirMap, final BaseWritableMemoryImpl wMem) {
    this.dirMap = dirMap;
    this.wMem = wMem;
  }

  @SuppressWarnings("resource") //called from memory
  static MapHandle map(final File file, final long fileOffset, final long capacityBytes,
      final ByteOrder byteOrder) {
    final boolean resourceReadOnly = AllocateDirectMap.isFileReadOnly(file);
    final AllocateDirectMap dirMap = AllocateDirectMap.map(file, fileOffset, capacityBytes);
    if (byteOrder == ByteOrder.nativeOrder()) {
      wMem = new WritableMemoryImpl(nativeBaseOffset, capacityBytes, localReadOnly,
          resourceReadOnly);
    } else {
      wMem = new NonNativeWritableMemoryImpl(nativeBaseOffset, capacityBytes, localReadOnly,
          resourceReadOnly);
    }

    final BaseWritableMemoryImpl wMem = BaseWritableMemoryImpl.newInstance(state, true);
    dirMap.setStepBoolean(wMem.getValid());
    return new MapHandle(dirMap, wMem);
  }

  @Override
  public Memory get() {
    return wMem;
  }

  @Override
  public void close() {
    if (wMem.isValid()) {
      ResourceState.currentDirectMemoryMapAllocations_.decrementAndGet();
      ResourceState.currentDirectMemoryMapAllocated_.addAndGet(-wMem.capacity);
    }
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
