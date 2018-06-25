/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for direct memory, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class DirectNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private static final int id = MEMORY | NONNATIVE | DIRECT;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private MemoryRequestServer memReqSvr = null; //cannot be final;
  private final byte typeId;

  DirectNonNativeWritableMemoryImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final StepBoolean valid,
      final MemoryRequestServer memReqSvr) {
    super(null, nativeBaseOffset, regionOffset, capacityBytes);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
    this.memReqSvr = (memReqSvr == null) ? defaultMemReqSvr : memReqSvr;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, valid, memReqSvr)
        : new DirectNonNativeWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, valid, memReqSvr);
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = readOnly ? READONLY : 0;
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, memReqSvr, this)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, memReqSvr, this);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    return null;
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
  public MemoryRequestServer getMemoryRequestServer() {
    assertValid();
    return memReqSvr; //cannot be null
  }

  @Override
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public boolean isDirect() {
    return true;
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

}
