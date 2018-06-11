/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Cleaner;

/**
 * Provides access to direct (native) memory.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class AllocateDirect implements AutoCloseable {
  private static final Logger LOG = LoggerFactory.getLogger(AllocateDirect.class);

  private final Deallocator deallocator;
  private final Cleaner cleaner;
  private final long nativeBaseOffset;

  /**
   * Base Constructor for allocate native memory.
   *
   * <p>Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the Memory interface.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param capacityBytes the the requested capacity of off-heap memory. Cannot be zero.
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
  }

  @Override
  public void close() {
    doClose();
  }

  boolean doClose() {
    if (deallocator.deallocate(false)) {
      // This Cleaner.clean() call effectively just removes the Cleaner from the internal linked list
      // of all cleaners. It will delegate to Deallocator.deallocate() which will be a no-op because
      // the valid state is already changed.
      cleaner.clean();
      return true;
    } else {
      return false;
    }
  }

  long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  StepBoolean getValid() {
    return deallocator.getValid();
  }

  private static final class Deallocator implements Runnable {
    //This is the only place the actual native address is kept for use by unsafe.freeMemory();
    private final long nativeAddress;
    private final long allocationSize;
    private final long capacity;
    private final StepBoolean valid = new StepBoolean(true); //only place for this

    private Deallocator(final long nativeAddress, final long allocationSize, final long capacity) {
      BaseState.currentDirectMemoryAllocations_.incrementAndGet();
      BaseState.currentDirectMemoryAllocated_.addAndGet(capacity);
      this.nativeAddress = nativeAddress;
      this.allocationSize = allocationSize;
      this.capacity = capacity;
      assert (nativeAddress != 0);
    }

    StepBoolean getValid() {
      return valid;
    }

    @Override
    public void run() {
      deallocate(true);
    }

    boolean deallocate(final boolean calledFromCleaner) {
      if (valid.change()) {
        if (calledFromCleaner) {
          // Warn about non-deterministic resource cleanup.
          LOG.warn("A WritableDirectHandle was not closed manually");
        }
        unsafe.freeMemory(nativeAddress);
        NioBits.unreserveMemory(allocationSize, capacity);
        BaseState.currentDirectMemoryAllocations_.decrementAndGet();
        BaseState.currentDirectMemoryAllocated_.addAndGet(-capacity);
        return true;
      } else {
        return false;
      }
    }
  }

}
