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
  private final StepBoolean valid;
  private final long capacity;
  private final Cleaner cleaner;

  /**
   * Base Constructor for allocate native memory.
   *
   * <p>Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the Memory interface.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param capacity the the requested capacity of off-heap memory. Cannot be zero.
   * @param state contains valid, capacity at this point
   */
  private AllocateDirect(final long capacity, final ResourceState state) {
    valid = state.getValid();
    this.capacity = capacity;

    final boolean pageAligned = NioBits.isPageAligned();
    final long pageSize = NioBits.pageSize();
    final long size = capacity + (pageAligned ? pageSize : 0);
    NioBits.reserveMemory(capacity);
    final long nativeBaseOffset;

    long nativeAddress = 0;
    try {
      nativeAddress = unsafe.allocateMemory(size);
    } catch (final OutOfMemoryError err) {
      NioBits.unreserveMemory(capacity);
      throw err;
    }
    if (pageAligned && ((nativeAddress % pageSize) != 0)) {
      //Round up to page boundary
      nativeBaseOffset = (nativeAddress + pageSize) - (nativeAddress & (pageSize - 1L));
    } else {
      nativeBaseOffset = nativeAddress;
    }
    cleaner = Cleaner.create(this, new Deallocator(nativeAddress, capacity, valid));
    state.putNativeBaseOffset(nativeBaseOffset); //computes the cumBaseOffset
    ResourceState.currentDirectMemoryAllocations_.incrementAndGet();
    ResourceState.currentDirectMemoryAllocated_.addAndGet(state.getCapacity());
  }

  static AllocateDirect allocateDirect(final long capacity, final ResourceState state) {
    return new AllocateDirect(capacity, state);
  }

  @Override
  public void close() {
    if (valid.get()) { //is valid
      ResourceState.currentDirectMemoryAllocations_.decrementAndGet();
      ResourceState.currentDirectMemoryAllocated_.addAndGet(-capacity);
    }
    cleaner.clean(); //sets invalid
  }

  private static final class Deallocator implements Runnable {
    //This is the only place the actual native address is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long nativeAddress; //set to 0 when deallocated
    private final long capacity;
    private StepBoolean valid;

    private Deallocator(final long nativeAddress, final long capacity, final StepBoolean valid) {
      this.nativeAddress = nativeAddress;
      this.capacity = capacity;
      this.valid = valid;
      assert (nativeAddress != 0);
    }

    @Override
    public void run() {
      valid.change(); //sets invalid here
      if (nativeAddress > 0) {
        unsafe.freeMemory(nativeAddress);
        NioBits.unreserveMemory(capacity);
      }
      nativeAddress = 0L;
    }
  }

}
