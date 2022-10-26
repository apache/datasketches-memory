/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal.direct;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.apache.datasketches.memory.internal.BaseWritableBufferImpl;
import org.apache.datasketches.memory.internal.BaseWritableMemoryImpl;
import org.apache.datasketches.memory.internal.NonNativeWritableMemoryImpl;
import org.apache.datasketches.memory.internal.Util;
import org.apache.datasketches.memory.internal.unsafe.StepBoolean;

/**
 * Implementation of {@link WritableMemory} for direct memory, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public final class DirectNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private static final int id = MEMORY | NONNATIVE | DIRECT;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private final MemoryRequestServer memReqSvr;
  private final byte typeId;

  public DirectNonNativeWritableMemoryImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final StepBoolean valid,
      final MemoryRequestServer memReqSvr) {
    super(null, nativeBaseOffset, regionOffset, capacityBytes);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
    this.memReqSvr = memReqSvr;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  protected BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = setReadOnlyType(typeId, readOnly) | REGION;
    return Util.isNativeByteOrder(byteOrder)
        ? new DirectWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, type, valid, memReqSvr)
        : new DirectNonNativeWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, type, valid, memReqSvr);
  }

  @Override
  protected BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = setReadOnlyType(typeId, readOnly);
    return Util.isNativeByteOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), type, valid, memReqSvr)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), type, valid, memReqSvr);
  }

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    assertValid();
    return memReqSvr;
  }

  @Override
  protected long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  @Override
  protected int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

  @Override
  protected Object getUnsafeObject() {
      return null;
  }
}