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

import org.apache.datasketches.memory.WritableBuffer;

/**
 * Implementation of {@link WritableBuffer} for map memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class MapWritableBufferImpl extends NativeWritableBufferImpl {
  private final AllocateDirectWritableMap dirWMap;

  MapWritableBufferImpl(
      final AllocateDirectWritableMap dirWMap,
      final long offsetBytes,
      final long capacityBytes,
      final int typeId,
      final long cumOffsetBytes) {
    super(capacityBytes);
    this.dirWMap = dirWMap;
    this.offsetBytes = offsetBytes;
    this.capacityBytes = capacityBytes;
    this.typeId = removeNnBuf(typeId) | MAP | BUFFER | NATIVE_BO;
    this.cumOffsetBytes = cumOffsetBytes;
    if ((this.owner != null) && (this.owner != Thread.currentThread())) {
      throw new IllegalStateException(THREAD_EXCEPTION_TEXT);
    }
    this.owner = Thread.currentThread();
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(
      final long regionOffsetBytes,
      final long capacityBytes,
      final boolean readOnly,
      final ByteOrder byteOrder) {
    final long newOffsetBytes = offsetBytes + regionOffsetBytes;
    final long newCumOffsetBytes = cumOffsetBytes + regionOffsetBytes;
    int typeIdOut = removeNnBuf(typeId) | MAP | REGION | (readOnly ? READONLY : 0);

    if (Util.isNativeByteOrder(byteOrder)) {
      typeIdOut |= NATIVE_BO;
      return new MapWritableBufferImpl(
          dirWMap, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes);
    } else {
      typeIdOut |= NONNATIVE_BO;
      return new MapNonNativeWritableBufferImpl(
          dirWMap, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes);
    }
  }

  @Override
  BaseWritableMemoryImpl toWritableMemory(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | MEMORY | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE_BO;
      return new MapWritableMemoryImpl(
          dirWMap, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes);
    } else {
      typeIdOut |= NONNATIVE_BO;
      return new MapNonNativeWritableMemoryImpl(
          dirWMap, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes);
    }
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | BUFFER | DUPLICATE | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE_BO;
      return new MapWritableBufferImpl(
          dirWMap, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes);
    } else {
      typeIdOut |= NONNATIVE_BO;
      return new MapNonNativeWritableBufferImpl(
          dirWMap, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes);
    }
  }

  @Override
  public void close() {
    checkValid();
    checkThread(owner);
    dirWMap.close(); //checksValidAndThread
  }

  @Override
  public void force() {
    checkValid();
    checkThread(owner);
    checkNotReadOnly();
    dirWMap.force(); //checksValidAndThread
  }

  @Override
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public boolean isLoaded() {
    checkValid();
    checkThread(owner);
    return dirWMap.isLoaded(); //checksValidAndThread
  }

  @Override
  public boolean isAlive() {
    return dirWMap.getValid().get();
  }

  @Override
  public void load() {
    checkValid();
    checkThread(owner);
    dirWMap.load(); //checksValidAndThread
  }

}
