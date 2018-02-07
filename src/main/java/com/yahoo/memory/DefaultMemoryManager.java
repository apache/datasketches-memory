/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Implements a very simple memory management function that allocates new requests onto the heap.
 * @author Lee Rhodes
 */
final class DefaultMemoryManager implements MemoryManager {
  private static final MemoryManager memMgr = new DefaultMemoryManager();

  private DefaultMemoryManager() {}

  static MemoryManager getInstance() {
    return memMgr;
  }

  /**
   *
   * @param capacityBytes given capacity in bytes
   * @return handler
   */
  @Override
  @SuppressWarnings("resource")
  public WritableDirectHandle allocateDirect(final long capacityBytes) {
    final ResourceState state = new ResourceState();
    state.putCapacity(capacityBytes);
    final AllocateDirect direct = AllocateDirect.allocate(state);
    final WritableMemory wMem = new WritableMemoryImpl(state);
    final WritableDirectHandle handle = new WritableDirectHandle(direct, wMem);
    state.setMemoryRequestServer(this);
    state.setHandle(handle);
    return handle;
  }

  @Override
  public WritableMemory request(final long capacityBytes) { //default allocate on heap
    final WritableMemory mem = WritableMemory.allocate((int)capacityBytes);
    mem.setMemoryRequest(this);
    mem.setHandle(null);
    return mem;
  }

  @Override
  public void requestClose(final WritableMemory memoryToClose, final WritableMemory newMemory) {
    if (memoryToClose.getHandle() != null) {
      memoryToClose.getHandle().close();
    }
  }

}
