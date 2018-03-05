/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * @author Lee Rhodes
 */
final class DefaultMemoryRequestServer implements MemoryRequestServer {
  private WritableDirectHandle handle;

  DefaultMemoryRequestServer(final WritableDirectHandle handle) {
    this.handle = handle;
  }

  @Override
  public WritableMemory request(final long capacityBytes) {
    final WritableMemory mem = WritableMemory.allocate((int)capacityBytes);
    handle = null;
    return mem;
  }

  public WritableMemory requestDirect(final long capacityBytes) {
    handle = WritableMemory.allocateDirect(capacityBytes, this);
    return handle.get();
  }

  @Override
  public void requestClose() {
    if (handle != null) {
      handle.close();
    }
  }
}
