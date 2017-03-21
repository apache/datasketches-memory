/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a WritableBuffer for a map resource
 * @author Lee Rhodes
 */
//Implements combo of WritableBuffer with WritableMap resource
public class WritableBufferMapHandler implements WritableMap {
  AllocateDirectWritableMap dirWmap;
  WritableBufferImpl wBuf;

  WritableBufferMapHandler(final AllocateDirectWritableMap dirWmap, final WritableBufferImpl wBuf) {
    this.dirWmap = dirWmap;
    this.wBuf = wBuf;
  }

  @SuppressWarnings("resource") //called from memory
  static WritableBufferMapHandler map(final ResourceState state) throws Exception {
    final AllocateDirectWritableMap dirMap = AllocateDirectWritableMap.map(state);
    final WritableBufferImpl wBuf = new WritableBufferImpl(state);
    return new WritableBufferMapHandler(dirMap, wBuf);
  }

  /**
   * Gets a WritableBuffer for a map resource
   * @return a WritableBuffer for a map resource
   */
  public WritableBuffer get() {
    return wBuf;
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
