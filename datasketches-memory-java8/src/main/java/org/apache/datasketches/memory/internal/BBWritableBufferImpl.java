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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;

/**
 * Implementation of {@link WritableBuffer} for ByteBuffer, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBWritableBufferImpl extends NativeWritableBufferImpl {
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.
  private final Object unsafeObj;
  private final long nativeBaseOffset;
  private final long offsetBytes;
  private final long capacityBytes;
  private final int typeId;
  private long cumOffsetBytes;
  private long regionOffsetBytes;
  private final MemoryRequestServer memReqSvr;

  BBWritableBufferImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long offsetBytes,
      final long capacityBytes,
      final int typeId,
      final long cumOffsetBytes,
      final MemoryRequestServer memReqSvr,
      final ByteBuffer byteBuf) {
    super(capacityBytes);
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.offsetBytes = offsetBytes;
    this.capacityBytes = capacityBytes;
    this.typeId = removeNnBuf(typeId) | BYTEBUF | BUFFER | NATIVE;
    this.cumOffsetBytes = cumOffsetBytes;
    this.regionOffsetBytes = 0;
    this.memReqSvr = memReqSvr;
    this.byteBuf = byteBuf;
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
    int typeIdOut = removeNnBuf(typeId) | BUFFER | REGION | (readOnly ? READONLY : 0);

    if (Util.isNativeByteOrder(byteOrder)) {
      typeIdOut |= NATIVE;
      return new BBWritableBufferImpl(
          unsafeObj, nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, memReqSvr, byteBuf);
    } else {
      typeIdOut |= NONNATIVE;
      return new BBNonNativeWritableBufferImpl(
          unsafeObj, nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, memReqSvr, byteBuf);
    }
  }

  @Override
  BaseWritableMemoryImpl toWritableMemory(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | MEMORY | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE;
      return new BBWritableMemoryImpl(
          unsafeObj, nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, memReqSvr, byteBuf);
    } else {
      typeIdOut |= NONNATIVE;
      return new BBNonNativeWritableMemoryImpl(
          unsafeObj, nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, memReqSvr, byteBuf);
    }
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | BUFFER | DUPLICATE | (readOnly ? READONLY : 0);

    if (byteOrder == ByteOrder.nativeOrder()) {
      typeIdOut |= NATIVE;
      return new BBWritableBufferImpl(
          unsafeObj, nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, memReqSvr, byteBuf);
    } else {
      typeIdOut |= NONNATIVE;
      return new BBNonNativeWritableBufferImpl(
          unsafeObj, nativeBaseOffset, offsetBytes, capacityBytes, typeIdOut, cumOffsetBytes, memReqSvr, byteBuf);
    }
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
    return memReqSvr;
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
    return unsafeObj;
  }

}
