/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static com.yahoo.memory4.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory4.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * @author Lee Rhodes
 */
final class AccessByteBuffer extends WritableMemoryImpl {

  private AccessByteBuffer(final MemoryState state) {
    super(state);
  }

  //The provided ByteBuffer may be either readOnly or writable
  static WritableMemoryImpl wrap(final MemoryState state) {
    final ByteBuffer byteBuf = state.getByteBuffer();
    state.putCapacity(byteBuf.capacity());
    final boolean readOnlyBB = byteBuf.isReadOnly();

    final boolean direct = byteBuf.isDirect();

    if (readOnlyBB) {
      state.setResourceReadOnly();

      //READ-ONLY DIRECT
      if (direct) {
        //address() is already adjusted for direct slices, so regionOffset = 0
        state.putNativeBaseOffset(((sun.nio.ch.DirectBuffer) byteBuf).address());
        return new AccessByteBuffer(state);
      }

      //READ-ONLY HEAP
      //The messy acquisition of arrayOffset() and array() created from a RO slice()
      final Object unsafeObj;
      final long regionOffset;
      try {
        Field field = ByteBuffer.class.getDeclaredField("offset");
        field.setAccessible(true);
        //includes the slice() offset for heap.
        regionOffset = ((Integer)field.get(byteBuf)).longValue() * ARRAY_BYTE_INDEX_SCALE;

        field = ByteBuffer.class.getDeclaredField("hb"); //the backing byte[] from HeapByteBuffer
        field.setAccessible(true);
        unsafeObj = field.get(byteBuf);
      }
      catch (final IllegalAccessException | NoSuchFieldException e) {
        throw new RuntimeException(
            "Could not get offset/byteArray from OnHeap ByteBuffer instance: " + e.getClass());
      }
      state.putUnsafeObjectHeader(ARRAY_BYTE_BASE_OFFSET);
      state.putUnsafeObject(unsafeObj);
      state.putRegionOffset(regionOffset);
      return new AccessByteBuffer(state);
    }

    else { //BB is WRITABLE.

      //WRITABLE-DIRECT  //nativeBaseAddress, byteBuf, capacity
      if (direct) {
        //address() is already adjusted for direct slices, so regionOffset = 0
        state.putNativeBaseOffset(((sun.nio.ch.DirectBuffer) byteBuf).address());
        return new AccessByteBuffer(state);
      }

      //WRITABLE-HEAP  //unsafeObj, unsafeObjHeader, bytBuf, regionOffset, capacity
      state.putUnsafeObject(byteBuf.array());
      state.putUnsafeObjectHeader(ARRAY_BYTE_BASE_OFFSET);
      state.putRegionOffset(byteBuf.arrayOffset() * ARRAY_BYTE_INDEX_SCALE);
      return new AccessByteBuffer(state);
    }
  }

}
