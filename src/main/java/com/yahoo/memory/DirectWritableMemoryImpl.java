/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for direct memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class DirectWritableMemoryImpl extends WritableMemoryImpl {
  private static final int id = MEMORY | NATIVE | DIRECT;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private MemoryRequestServer memReqSvr = null; //cannot be final;
  private final byte typeId;

  DirectWritableMemoryImpl(
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
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
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
    final int type = typeId | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, memReqSvr, this)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, memReqSvr, this);
  }

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    assertValid();
    return memReqSvr; //cannot be null
  }

  @Override
  long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  @Override
  int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

}
