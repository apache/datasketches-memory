/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static com.yahoo.memory4.UnsafeUtil.unsafe;

import sun.misc.Cleaner;

/**
 * @author Lee Rhodes
 */
final class AllocateDirect extends WritableMemoryImpl implements WritableResourceHandler {
  private final Cleaner cleaner;

  /**
   * Base Constructor for allocate native memory.
   *
   * <p>Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the Memory interface.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * @param state contains the capacity and optionally the MemoryRequest
   */
  private AllocateDirect(final MemoryState state) {
    super(state);
    this.cleaner = Cleaner.create(this, new Deallocator(state));
  }

  static WritableMemoryImpl allocDirect(final MemoryState state) {
    state.putNativeBaseOffset(unsafe.allocateMemory(state.getCapacity()));
    return new AllocateDirect(state);
  }

  @Override
  public void close() {
    try {
      this.cleaner.clean();
      super.state.setInvalid();
    } catch (final Exception e) {
      throw e;
    }
  }

  private static final class Deallocator implements Runnable {
    //This is the only place the actual native offset is kept for use by unsafe.freeMemory();
    //It can never be modified until it is deallocated.
    private long actualNativeBaseOffset; //
    private final MemoryState parentStateRef;

    private Deallocator(final MemoryState state) {
      this.actualNativeBaseOffset = state.getNativeBaseOffset();
      assert (actualNativeBaseOffset != 0);
      this.parentStateRef = state;
    }

    @Override
    public void run() {
      if (this.actualNativeBaseOffset == 0) {
        // Paranoia
        return;
      }
      unsafe.freeMemory(this.actualNativeBaseOffset);
      this.actualNativeBaseOffset = 0L;
      this.parentStateRef.setInvalid(); //The only place valid is set invalid.
    }
  }

  @Override
  public WritableMemory get() {
    return this;
  }

  @Override
  public void load() {
    // No-op
  }

  @Override
  public boolean isLoaded() {
    // means nothing.
    return false;
  }

  @Override
  public void force() {
    // No-op
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.NATIVE_MEMORY;
  }

  @Override
  public boolean isResourceType(final ResourceType resourceType) {
    return resourceType == ResourceType.NATIVE_MEMORY;
  }

}
