/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.File;
import java.io.IOException;

/**
 * Allocates direct memory used to memory map files for write operations
 * (including those &gt; 2GB).
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 * @author Praveenkumar Venkatesan
 */
//Called from WritableMemory, implements combo of WritableMemory with WritableMap resource
final class AllocateDirectWritableMap extends AllocateDirectMap implements WritableMap {

  private AllocateDirectWritableMap(final ResourceState state, final File file,
      final long fileOffset) {
    super(state, file, fileOffset);
  }

  /**
   * Factory method for memory mapping a file for write access.
   *
   * <p>Memory maps a file directly in off heap leveraging native map0 method implemented in
   * FileChannelImpl.c. The owner will have read and write access to that address space.</p>
   *
   * @param state the ResourceState
   * @return A new AllocateDirectWritableMap
   * @throws IOException file not found or RuntimeException, etc.
   */
  static AllocateDirectWritableMap map(final ResourceState state, final File file,
      final long fileOffset) {
    return new AllocateDirectWritableMap(state, file, fileOffset); //state: RRO, capacity, BO
  }

  @Override
  public void force() {
    try {
      MAPPED_BYTE_BUFFER_FORCE0_METHOD.invoke(super.mbb, super.raf.getFD(),
              super.state.getNativeBaseOffset(), super.state.getCapacity());
    } catch (final Exception e) {
      throw new RuntimeException(String.format("Encountered %s exception in force. "
          + UnsafeUtil.tryIllegalAccessPermit, e.getClass()));
    }
  }
}
