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
import org.apache.datasketches.memory.WritableMemory;

/**
 * Implementation of {@link WritableMemory} for ByteBuffer, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBWritableMemoryImpl extends NativeWritableMemoryImpl {
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.
  private final Object unsafeObj;
  private final long nativeBaseOffset; //raw off-heap address of root allocation (whether direct or ByteBuffer)
  private final long offsetBytes;      //from the root resource including original ByteBuffer position or region offset
  private final long capacityBytes;
  private final int typeId;
  private long cumOffsetBytes;         //includes array header if heap and nativeBaseOffset if off-heap
  private final MemoryRequestServer memReqSvr;

  BBWritableMemoryImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long offsetBytes,
      final long capacityBytes,
      final int typeId,
      final long cumOffsetBytes,
      final MemoryRequestServer memReqSvr,
      final ByteBuffer byteBuf) {
    super();
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.offsetBytes = offsetBytes;
    this.capacityBytes = capacityBytes;
    this.typeId = removeNnBuf(typeId) | BYTEBUF | MEMORY | NATIVE;
    this.cumOffsetBytes = cumOffsetBytes;
    this.memReqSvr = memReqSvr;
    this.byteBuf = byteBuf;
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(
      final long regionOffsetBytes,
      final long capacityBytes,
      final boolean readOnly,
      final ByteOrder byteOrder) {
    final long newOffsetBytes = offsetBytes + regionOffsetBytes;
    final long newCumOffsetBytes = cumOffsetBytes + regionOffsetBytes;
    int typeIdOut = removeNnBuf(typeId) | MEMORY | REGION | (readOnly ? READONLY : 0);

    if (Util.isNativeByteOrder(byteOrder)) {
      typeIdOut |= NATIVE;
      return new BBWritableMemoryImpl(
          unsafeObj, nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes, memReqSvr, byteBuf);
    } else {
      typeIdOut |= NONNATIVE;
      return new BBNonNativeWritableMemoryImpl(
          unsafeObj, nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes, memReqSvr, byteBuf);
    }
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    int typeIdOut = removeNnBuf(typeId) | BUFFER | (readOnly ? READONLY : 0);

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
  public long getTotalOffset() {
    assertValid();
    return offsetBytes;
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

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf;
  }
}
