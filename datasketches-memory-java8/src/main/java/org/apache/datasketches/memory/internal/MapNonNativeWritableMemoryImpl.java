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
import org.apache.datasketches.memory.WritableMemory;

/**
 * Implementation of {@link WritableMemory} for map memory, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class MapNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private final AllocateDirectWritableMap dirWMap;
  private final long offsetBytes;
  private final long capacityBytes;
  private final int typeId;
  private long cumOffsetBytes;

  MapNonNativeWritableMemoryImpl(
      final AllocateDirectWritableMap dirWMap,
      final long offsetBytes,
      final long capacityBytes,
      final int typeId,
      final long cumOffsetBytes) {
    super();
    this.dirWMap = dirWMap;
    this.offsetBytes = offsetBytes;
    this.capacityBytes = capacityBytes;
    this.typeId = removeNnBuf(typeId) | MAP | MEMORY | NONNATIVE;
    this.cumOffsetBytes = cumOffsetBytes;
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(
      final long regionOffsetBytes,
      final long capacityBytes,
      final boolean readOnly,
      final ByteOrder byteOrder) {
    final long newOffsetBytes = offsetBytes + regionOffsetBytes;
    final long newCumOffsetBytes = cumOffsetBytes + regionOffsetBytes;
    int typeIdOut = removeNnBuf(typeId) | MAP | REGION | (readOnly ? READONLY : 0);

    if (Util.isNativeByteOrder(byteOrder)) {
      typeIdOut |= NATIVE;
      return new MapWritableMemoryImpl(
          dirWMap, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes);
    } else {
      typeIdOut |= NONNATIVE;
      return new MapNonNativeWritableMemoryImpl(
          dirWMap, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes);
    }
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | BUFFER | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE;
      return new MapWritableBufferImpl(
          dirWMap, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes);
    } else {
      typeIdOut |= NONNATIVE;
      return new MapNonNativeWritableBufferImpl(
          dirWMap, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes);
    }
  }

  @Override
  public boolean isValid() {
    return dirWMap.getValid().get();
  }

  @Override
  public long getCapacity() {
    checkValid();
    return capacityBytes;
  }

  @Override
  public long getCumulativeOffset() {
    checkValid();
    return cumOffsetBytes;
  }

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    checkValid();
    return null;
  }

  @Override
  public long getNativeBaseOffset() {
    checkValid();
    return dirWMap.nativeBaseOffset;
  }

  @Override
  public long getTotalOffset() {
    checkValid();
    return offsetBytes;
  }

  @Override
  int getTypeId() {
    checkValid();
    return typeId;
  }

  @Override
  Object getUnsafeObject() {
    checkValid();
    return null;
  }

  @Override
  public void close() {
    dirWMap.close();
  }

  @Override
  public void force() {
    dirWMap.force();
  }

  @Override
  public boolean isLoaded() {
    return dirWMap.isLoaded();
  }

  @Override
  public void load() {
    dirWMap.load();
  }

}
