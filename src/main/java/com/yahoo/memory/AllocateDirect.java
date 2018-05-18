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
  private final Deallocator deallocator;
  private final Cleaner cleaner;
  final long nativeBaseOffset;

  /**
   * Base Constructor for allocate native memory.
   *
   * <p>Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the Memory interface.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param capacityBytes the the requested capacity of off-heap memory. Cannot be zero.
   * @param state contains valid, capacity at this point
   */
  AllocateDirect(final long capacityBytes) {
    final boolean pageAligned = NioBits.isPageAligned();
    final long pageSize = NioBits.pageSize();
    final long allocationSize = capacityBytes + (pageAligned ? pageSize : 0);
    NioBits.reserveMemory(allocationSize, capacityBytes);

    final long nativeAddress;
    try {
      nativeAddress = unsafe.allocateMemory(allocationSize);
    } catch (final OutOfMemoryError err) {
      NioBits.unreserveMemory(allocationSize, capacityBytes);
      throw err;
    }
    if (pageAligned && ((nativeAddress % pageSize) != 0)) {
      //Round up to page boundary
      nativeBaseOffset = (nativeAddress & ~(pageSize - 1L)) + pageSize;
    } else {
      nativeBaseOffset = nativeAddress;
    }
    deallocator = new Deallocator(nativeAddress, allocationSize, capacityBytes);
    cleaner = Cleaner.create(this, deallocator);
    ResourceState.currentDirectMemoryAllocations_.incrementAndGet();
    ResourceState.currentDirectMemoryAllocated_.addAndGet(capacityBytes);
  }

  @Override
  public void close() {
    cleaner.clean(); //sets invalid
  }

  void setValid(final StepBoolean valid) {
    deallocator.setStepBoolean(valid);
  }

  private static final class Deallocator implements Runnable {
    //This is the only place the actual native address is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long nativeAddress; //set to 0 when deallocated. Different from nativeBaseOffset
    private final long allocationSize;
    private final long capacity;
    private StepBoolean valid;

    private Deallocator(final long nativeAddress, final long allocationSize, final long capacity) {
      this.nativeAddress = nativeAddress;
      this.allocationSize = allocationSize;
      this.capacity = capacity;
      assert (nativeAddress != 0);
    }

    void setStepBoolean(final StepBoolean valid) {
      this.valid = valid;
    }

    @Override
    public void run() {
      valid.change(); //sets invalid here
      if (nativeAddress > 0) {
        unsafe.freeMemory(nativeAddress);
        NioBits.unreserveMemory(allocationSize, capacity);
      }
      nativeAddress = 0L;
    }
  }

}
