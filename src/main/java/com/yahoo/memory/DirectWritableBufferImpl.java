/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for direct memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class DirectWritableBufferImpl extends WritableBufferImpl {
  private static final int id = BUFFER | NATIVE | DIRECT;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private MemoryRequestServer memReqSvr = null; //cannot be final;
  private final byte typeId;

  DirectWritableBufferImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final StepBoolean valid,
      final MemoryRequestServer memReqSvr,
      final BaseWritableMemoryImpl originMemory) {
    super(null, nativeBaseOffset, regionOffset, capacityBytes, originMemory);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
    assert memReqSvr != null;
    this.memReqSvr = memReqSvr; //should not be null
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, valid, memReqSvr, originMemory)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, valid, memReqSvr, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | DUPLICATE | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, memReqSvr, originMemory)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, memReqSvr, originMemory);
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
