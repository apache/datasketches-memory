/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a Buffer for a map resource
 * @author Lee Rhodes
 */
//Implements combo of Buffer with Map resource.
public class BufferMapHandler implements Map {
  AllocateDirectMap dirMap;
  WritableBufferImpl wBuf;

  BufferMapHandler(final AllocateDirectMap dirMap, final WritableBufferImpl wBuf) {
    this.dirMap = dirMap;
    this.wBuf = wBuf;
  }

  @SuppressWarnings("resource") //called from memory
  static BufferMapHandler map(final ResourceState state) throws Exception {
    final AllocateDirectMap dirMap = AllocateDirectMap.map(state);
    final WritableBufferImpl wMem = new WritableBufferImpl(state);
    return new BufferMapHandler(dirMap, wMem);
  }

  /**
   * Gets a Buffer for a map resource
   * @return a Buffer for a map resource
   */
  public Buffer get() {
    return wBuf;
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
