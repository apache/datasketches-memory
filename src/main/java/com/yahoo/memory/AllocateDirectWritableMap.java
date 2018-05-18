/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.File;

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

  AllocateDirectWritableMap(final File file, final long fileOffsetBytes,
      final long capacityBytes) {
    super(file, fileOffsetBytes, capacityBytes);
  }

  @Override
  public void force() {
    if (super.resourceReadOnly) {
      throw new ReadOnlyException("Memory Mapped File is Read Only.");
    }
    try {
      MAPPED_BYTE_BUFFER_FORCE0_METHOD                 //force0 is effectively static
          .invoke(AccessByteBuffer.ZERO_DIRECT_NIO_BUFFER, // so this is not modified
              super.raf.getFD(),
              super.nativeBaseOffset,
              super.capacityBytes);
    } catch (final Exception e) {
      throw new RuntimeException(String.format("Encountered %s exception in force. "
          + UnsafeUtil.tryIllegalAccessPermit, e.getClass()));
    }
  }
}
