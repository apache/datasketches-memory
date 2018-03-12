/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import sun.misc.Cleaner;

/**
 * Provides access to direct (native) memory.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class AllocateDirect implements AutoCloseable {
  final ResourceState state;
  private final Cleaner cleaner;

  /**
   * Base Constructor for allocate native memory.
   *
   * <p>Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the Memory interface.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param state contains the capacity and optionally the MemoryRequest
   */
  private AllocateDirect(final ResourceState state) {
    this.state = state;
    cleaner = Cleaner.create(this, new Deallocator(state));
    ResourceState.currentDirectMemoryAllocations_.incrementAndGet();
    ResourceState.currentDirectMemoryAllocated_.addAndGet(state.getCapacity());
  }

  static AllocateDirect allocate(final ResourceState state) {
    final long capacity = state.getCapacity();
    state.putNativeBaseOffset(unsafe.allocateMemory(capacity));
    NioBits.reserveMemory(capacity);
    return new AllocateDirect(state);
  }

  @Override
  public void close() {
    if (state.isValid()) {
      ResourceState.currentDirectMemoryAllocations_.decrementAndGet();
      ResourceState.currentDirectMemoryAllocated_.addAndGet(-state.getCapacity());
    }
    cleaner.clean(); //sets invalid
  }

  private static final class Deallocator implements Runnable {
    //This is the only place the actual native offset is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long actualNativeBaseOffset; //set to 0 when deallocated
    private final long capacity;
    private final ResourceState myState;

    private Deallocator(final ResourceState state) {
      actualNativeBaseOffset = state.getNativeBaseOffset();
      capacity = state.getCapacity();
      assert (actualNativeBaseOffset != 0);
      myState = state;
    }

    @Override
    public void run() {
      if (actualNativeBaseOffset > 0) {
        unsafe.freeMemory(actualNativeBaseOffset);
        NioBits.unreserveMemory(capacity);
      }
      actualNativeBaseOffset = 0L;
      myState.setInvalid();
    }
  }

}
