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

package org.apache.datasketches.memory.internal;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;

/**
 * Implementation of {@link WritableBuffer} for map memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class MapWritableBufferImpl extends NativeWritableBufferImpl {
  private final long nativeBaseOffset;
  private final long offsetBytes;
  private final long capacityBytes;
  private final int typeId;
  private long cumOffsetBytes;
  private long regionOffsetBytes;
  private final StepBoolean valid; //a reference only

  MapWritableBufferImpl(
      final long nativeBaseOffset,
      final long offsetBytes,
      final long capacityBytes,
      final int typeId,
      final long cumOffsetBytes,
      final StepBoolean valid) {
    super(capacityBytes);
    this.nativeBaseOffset = nativeBaseOffset;
    this.offsetBytes = offsetBytes;
    this.capacityBytes = capacityBytes;
    this.typeId = removeNnBuf(typeId) | MAP | BUFFER | NATIVE;
    this.cumOffsetBytes = cumOffsetBytes;
    this.regionOffsetBytes = 0;
    this.valid = valid;
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(
      final long regionOffsetBytes,
      final long capacityBytes,
      final boolean readOnly,
      final ByteOrder byteOrder) {
    this.regionOffsetBytes = regionOffsetBytes;
    final long newOffsetBytes = offsetBytes + regionOffsetBytes;
    cumOffsetBytes += regionOffsetBytes;
    int typeIdOut = removeNnBuf(typeId) | MAP | REGION | (readOnly ? READONLY : 0);

    if (Util.isNativeByteOrder(byteOrder)) {
      typeIdOut |= NATIVE;
      return new MapWritableBufferImpl(
          nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, valid);
    } else {
      typeIdOut |= NONNATIVE;
      return new MapNonNativeWritableBufferImpl(
          nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, valid);
    }
  }

  @Override
  BaseWritableMemoryImpl toWritableMemory(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | MEMORY | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE;
      return new MapWritableMemoryImpl(
          nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, valid);
    } else {
      typeIdOut |= NONNATIVE;
      return new MapNonNativeWritableMemoryImpl(
          nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, valid);
    }
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | BUFFER | DUPLICATE | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE;
      return new MapWritableBufferImpl(
          nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, valid);
    } else {
      typeIdOut |= NONNATIVE;
      return new MapNonNativeWritableBufferImpl(
          nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, valid);
    }
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

  @Override
  public long getCapacity() {
    assertValid();
    return capacityBytes;
  }

  @Override
  public long getCumulativeOffset() {
    assertValid();
    return cumOffsetBytes;
  }

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
  }

  @Override
  public long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  @Override
  public long getOffset() {
    assertValid();
    return offsetBytes;
  }

  @Override
  public long getRegionOffset() {
    assertValid();
    return regionOffsetBytes;
  }

  @Override
  int getTypeId() {
    assertValid();
    return typeId;
  }

  @Override
  Object getUnsafeObject() {
    assertValid();
    return null;
  }

}
