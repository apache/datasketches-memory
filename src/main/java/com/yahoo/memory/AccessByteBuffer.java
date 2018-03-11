/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Acquires access to a ByteBuffer.
 *
 * @author Lee Rhodes
 * @author Praveenkumar Venkatesan
 */
final class AccessByteBuffer {

  private static final Field BYTE_BUFFER_HB_FIELD;
  private static final long BYTE_BUFFER_OFFSET_FIELD_OFFSET;

  static {
    try {
      BYTE_BUFFER_HB_FIELD = ByteBuffer.class.getDeclaredField("hb");
      BYTE_BUFFER_HB_FIELD.setAccessible(true);

      BYTE_BUFFER_OFFSET_FIELD_OFFSET =
          UnsafeUtil.getFieldOffset(java.nio.ByteBuffer.class, "offset");
    }
    catch (final NoSuchFieldException e) {
      throw new RuntimeException(
          "Could not get offset and byteArray fields from ByteBuffer class: " + e.getClass()
          + UnsafeUtil.tryIllegalAccessPermit);
    }
  }

  private AccessByteBuffer() { }

  //The provided ByteBuffer may be either readOnly or writable
  // The resourceReadOnly is already set by the caller
  static void wrap(final ResourceState state) {
    final ByteBuffer byteBuf = state.getByteBuffer();
    state.putCapacity(byteBuf.capacity());
    final boolean readOnlyBB = state.isResourceReadOnly(); //set by putByteBuffer

    final boolean direct = byteBuf.isDirect();

    if (readOnlyBB) {

      //READ-ONLY DIRECT
      if (direct) {
        //address() is already adjusted for direct slices, so regionOffset = 0
        state.putNativeBaseOffset(((sun.nio.ch.DirectBuffer) byteBuf).address());
        return;
      }

      //READ-ONLY HEAP
      //The messy acquisition of arrayOffset() and array() created from a RO slice()
      final Object unsafeObj;
      final long regionOffset;
      //includes the slice() offset for heap.
      regionOffset = unsafe.getInt(byteBuf, BYTE_BUFFER_OFFSET_FIELD_OFFSET);
      try {
        unsafeObj = BYTE_BUFFER_HB_FIELD.get(byteBuf); //the backing byte[] from HeapByteBuffer
      }
      catch (final IllegalAccessException e) {
        throw new RuntimeException(
            "Could not get offset and byteArray fields from ByteBuffer class: " + e.getClass()
            + UnsafeUtil.tryIllegalAccessPermit);
      }
      state.putUnsafeObjectHeader(ARRAY_BYTE_BASE_OFFSET);
      state.putUnsafeObject(unsafeObj);
      state.putRegionOffset(regionOffset);
      return;
    }

    //BB is WRITABLE-DIRECT  //nativeBaseAddress, byteBuf, capacity
    if (direct) {
      //address() is already adjusted for direct slices, so regionOffset = 0
      state.putNativeBaseOffset(((sun.nio.ch.DirectBuffer) byteBuf).address());
      return;
    }

    //BB is WRITABLE-HEAP  //unsafeObj, unsafeObjHeader, bytBuf, regionOffset, capacity
    state.putUnsafeObject(byteBuf.array());
    state.putUnsafeObjectHeader(ARRAY_BYTE_BASE_OFFSET);
    state.putRegionOffset(byteBuf.arrayOffset() * ARRAY_BYTE_INDEX_SCALE);
  }
}
