/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.FileDescriptor;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;

/**
 * Allocates direct memory used to memory map files for write operations
 * (including those &gt; 2GB).
 *
 * @author Praveenkumar Venkatesan
 * @author Lee Rhodes
 */
final class AllocateDirectWritableMap extends AllocateDirectMap implements WritableResourceHandler {

  private AllocateDirectWritableMap(final ResourceState state) {
    super(state);
  }

  /**
   * Factory method for memory mapping a file. This should only be called if the file is indeed
   * writable.
   *
   * <p>Memory maps a file directly in off heap leveraging native map0 method used in
   * FileChannelImpl.c. The owner will have read and write access to that address space.</p>
   *
   * @param state the ResourceState
   * @return A new AllocateDirectWritableMap
   * @throws Exception file not found or RuntimeException, etc.
   */
  //@SuppressWarnings("resource")
  static AllocateDirectWritableMap map(final ResourceState state) throws Exception {
    if (isFileReadOnly(state.getFile())) {
      throw new ReadOnlyMemoryException("File is read-only.");
    }
    return new AllocateDirectWritableMap(mapper(state));
  }

  @Override
  public WritableMemory get() {
    return this;
  }

  @Override
  public void force() {
    try {
      final Method method = MappedByteBuffer.class.getDeclaredMethod("force0",
          FileDescriptor.class, long.class, long.class);
      method.setAccessible(true);
      method.invoke(super.state.getMappedByteBuffer(), super.state.getRandomAccessFile().getFD(),
          super.state.getNativeBaseOffset(), super.capacity);
    } catch (final Exception e) {
      throw new RuntimeException(String.format("Encountered %s exception in force", e.getClass()));
    }
  }

}
