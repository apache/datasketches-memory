/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for ByteBuffer, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBWritableMemoryImpl extends WritableMemoryImpl {
  private static final int id = MEMORY | NATIVE | BYTEBUF;
  private final Object unsafeObj;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.
  private final byte typeId;

  BBWritableMemoryImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final ByteBuffer byteBuf) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.byteBuf = byteBuf;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableMemoryImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, getByteBuffer())
        : new BBNonNativeWritableMemoryImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, getByteBuffer());
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, byteBuf, this)
        : new BBNonNativeWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, byteBuf, this);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf;
  }

  @Override
  int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  Object getUnsafeObject() {
    assertValid();
    return unsafeObj;
  }

  @Override
  public boolean isValid() {
    return true;
  }

}
