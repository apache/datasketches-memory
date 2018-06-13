/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for heap-based, native endian byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public class HeapWritableMemoryImpl extends WritableMemoryImpl {
  private final Object unsafeObj;
  private final long regionOffset;
  private final long capacityBytes;
  private final boolean readOnly;
  private final long cumBaseOffset;

  //ctor for all parameters
  HeapWritableMemoryImpl(
      final Object unsafeObj,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly) {
    super(unsafeObj, 0, regionOffset, capacityBytes, readOnly, null, null);
    this.capacityBytes = capacityBytes;
    this.regionOffset = regionOffset;
    this.unsafeObj = unsafeObj;
    this.readOnly = readOnly;
    cumBaseOffset = regionOffset + unsafe.arrayBaseOffset(unsafeObj.getClass());
  }

  @Override
  public ByteBuffer getByteBuffer() {
    return null;
  }

  @Override
  public ByteOrder getByteOrder() {
    return Util.nativeOrder;
  }

  @Override
  public long getCapacity() {
    return capacityBytes;
  }

  @Override
  public long getCumulativeOffset() {
    return cumBaseOffset;
  }

  @Override
  public long getCumulativeOffset(final long offsetBytes) {
    return cumBaseOffset + offsetBytes;
  }

  @Override
  public MemoryRequestServer getMemoryRequestServer() { //remove from baseWMemImpl NOTE WRITABLE ONLY
    return null;
  }

  @Override
  long getNativeBaseOffset() {
    return 0;
  }

  @Override
  public long getRegionOffset() { //remove from baseWMemImpl NOTE WRITABLE ONLY
    return regionOffset;
  }

  @Override
  public long getRegionOffset(final long offsetBytes) { //remove from baseWMemImpl NOTE WRITABLE ONLY
    return regionOffset + offsetBytes;
  }

  @Override
  Object getUnsafeObject() {
    return unsafeObj;
  }

  @Override
  public boolean hasArray() {
    return true;
  }

  @Override
  public boolean hasByteBuffer() {
    return false;
  }

  @Override
  public boolean isDirect() {
    return false;
  }

  @Override
  public boolean isNativeOrder() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  void assertValid() {}

  @Override
  void checkValid() {}

  @Override
  void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertBounds(offsetBytes, lengthBytes, getCapacity());
  }

  @Override
  public void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkBounds(offsetBytes, lengthBytes, getCapacity());
  }

  @Override
  void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkBounds(offsetBytes, lengthBytes, getCapacity());
    if (isReadOnly()) {
      throw new ReadOnlyException("Memory is read-only.");
    }
  }

}
