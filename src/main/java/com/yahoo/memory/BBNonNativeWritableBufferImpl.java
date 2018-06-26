/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for ByteBuffer, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBNonNativeWritableBufferImpl extends NonNativeWritableBufferImpl {
  private static final int id = BUFFER | NONNATIVE | BYTEBUF;
  private final Object unsafeObj;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.
  private final byte typeId;

  BBNonNativeWritableBufferImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final ByteBuffer byteBuf,
      final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, originMemory);
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.byteBuf = byteBuf;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableBufferImpl(
          unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
          type, byteBuf, originMemory)
        : new BBNonNativeWritableBufferImpl(
          unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
          type, byteBuf, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = DUPLICATE | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, byteBuf, originMemory)
        : new BBNonNativeWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, byteBuf, originMemory);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf;
  }

  @Override
  public ByteOrder getByteOrder() {
    assertValid();
    return Util.nonNativeOrder;
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
  public boolean isDirect() {
    return unsafeObj == null;
  }

  @Override
  public boolean isValid() {
    return true;
  }

}
