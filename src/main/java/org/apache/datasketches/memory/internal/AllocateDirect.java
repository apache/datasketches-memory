/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.util.logging.Logger;

/**
 * Provides access to direct (native) memory.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
final class AllocateDirect {
  static final Logger LOG = Logger.getLogger(AllocateDirect.class.getCanonicalName());

  private final Deallocator deallocator;
  private final long nativeBaseOffset;
  private final MemoryCleaner cleaner;

  /**
   * Base Constructor for allocate native memory.
   *
   * <p>Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the MemoryImpl interface.
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
      throw new RuntimeException(err);
    }
    if (pageAligned && ((nativeAddress % pageSize) != 0)) {
      //Round up to page boundary
      nativeBaseOffset = (nativeAddress & ~(pageSize - 1L)) + pageSize;
    } else {
      nativeBaseOffset = nativeAddress;
    }
    deallocator = new Deallocator(nativeAddress, allocationSize, capacityBytes);
    cleaner = new MemoryCleaner(this, deallocator);
  }

  boolean doClose() {
    try {
      if (deallocator.deallocate(false)) {
        // This Cleaner.clean() call effectively just removes the Cleaner from the internal linked
        // list of all cleaners. It will delegate to Deallocator.deallocate() which will be a no-op
        // because the valid state is already changed.
        cleaner.clean();
        return true;
      }
      return false;
    } finally {
      BaseStateImpl.reachabilityFence(this);
    }
  }

  long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  StepBoolean getValid() {
    return deallocator.getValid();
  }

  static final class Deallocator implements Runnable {
    //This is the only place the actual native address is kept for use by unsafe.freeMemory();
    private final long nativeAddress;
    private final long allocationSize;
    private final long capacity;
    private final StepBoolean valid = new StepBoolean(true); //only place for this

    Deallocator(final long nativeAddress, final long allocationSize, final long capacity) {
      BaseStateImpl.currentDirectMemoryAllocations_.incrementAndGet();
      BaseStateImpl.currentDirectMemoryAllocated_.addAndGet(capacity);
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
          LOG.warning("A WritableHandle was not closed manually");
        }
        unsafe.freeMemory(nativeAddress);
        NioBits.unreserveMemory(allocationSize, capacity);
        BaseStateImpl.currentDirectMemoryAllocations_.decrementAndGet();
        BaseStateImpl.currentDirectMemoryAllocated_.addAndGet(-capacity);
        return true;
      }
      return false;
    }
  }

}
