/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;

/**
 * Acquires access to a ByteBuffer.
 *
 * @author Lee Rhodes
 * @author Praveenkumar Venkatesan
 */
final class AccessByteBuffer {

  static final ByteBuffer ZERO_DIRECT_BUFFER = ByteBuffer.allocateDirect(0);

  private static final long NIO_BUFFER_ADDRESS_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.Buffer.class, "address");
  private static final long NIO_BUFFER_CAPACITY_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.Buffer.class, "capacity");
  private static final long BYTE_BUFFER_HB_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.ByteBuffer.class, "hb");
  private static final long BYTE_BUFFER_OFFSET_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.ByteBuffer.class, "offset");

  private AccessByteBuffer() { }

  //The provided ByteBuffer may be either readOnly or writable
  // The resourceReadOnly is already set by the caller
  static void wrap(final ResourceState state) {
    final ByteBuffer byteBuf = state.getByteBuffer();
    state.putCapacity(byteBuf.capacity());
    final boolean direct = byteBuf.isDirect();
    if (direct) {
      //address() is already adjusted for direct slices, so regionOffset = 0
      state.putNativeBaseOffset(((sun.nio.ch.DirectBuffer) byteBuf).address());
    } else {
      // arrayOffset() and array() couldn't be called because they throw ReadOnlyBufferException
      // if the ByteBuffer is read-only.
      final Object unsafeObj;
      final long regionOffset;
      //includes the slice() offset for heap.
      regionOffset = unsafe.getInt(byteBuf, BYTE_BUFFER_OFFSET_FIELD_OFFSET);
      unsafeObj = unsafe.getObject(byteBuf, BYTE_BUFFER_HB_FIELD_OFFSET);
      state.putUnsafeObject(unsafeObj);
      state.putRegionOffset(regionOffset);
    }
  }

  /**
   * This method is copied from https://github.com/odnoklassniki/one-nio/blob/
   * 27c768cbd28ece949c299f2d437c9a0ebd874500/src/one/nio/mem/DirectMemory.java#L95
   */
  static ByteBuffer getDummyDirectByteBuffer(final long address, final int capacity) {
    final ByteBuffer buf = ZERO_DIRECT_BUFFER.duplicate();
    unsafe.putLong(buf, NIO_BUFFER_ADDRESS_FIELD_OFFSET, address);
    unsafe.putInt(buf, NIO_BUFFER_CAPACITY_FIELD_OFFSET, capacity);
    buf.limit(capacity);
    return buf;
  }

}
