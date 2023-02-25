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
 * Implementation of {@link WritableBuffer} for ByteBuffer, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBNonNativeWritableBufferImpl extends NonNativeWritableBufferImpl {
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.
  private final Object unsafeObj;
  private final long nativeBaseOffset; //raw off-heap address of allocation base if ByteBuffer direct, else 0

  BBNonNativeWritableBufferImpl(
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
    this.offsetBytes = offsetBytes; //in ResourceImpl
    this.capacityBytes = capacityBytes; //in ResourceImpl
    this.typeId = removeNnBuf(typeId) | BYTEBUF | BUFFER | NONNATIVE; //in ResourceImpl
    this.cumOffsetBytes = cumOffsetBytes; //in ResourceImpl
    this.memReqSvr = memReqSvr; //in ResourceImpl
    this.byteBuf = byteBuf;
    if ((this.owner != null) && (this.owner != Thread.currentThread())) {
      throw new IllegalStateException(THREAD_EXCEPTION_TEXT);
    }
    this.owner = Thread.currentThread(); //in ResourceImpl
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(
      final long regionOffsetBytes,
      final long capacityBytes,
      final boolean readOnly,
      final ByteOrder byteOrder) {
    final long newOffsetBytes = offsetBytes + regionOffsetBytes;
    final long newCumOffsetBytes = cumOffsetBytes + regionOffsetBytes;
    int typeIdOut = removeNnBuf(typeId) | BUFFER | REGION | (readOnly ? READONLY : 0);

    if (Util.isNativeByteOrder(byteOrder)) {
      typeIdOut |= NATIVE;
      return new BBWritableBufferImpl(
          unsafeObj, nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes, memReqSvr, byteBuf);
    } else {
      typeIdOut |= NONNATIVE;
      return new BBNonNativeWritableBufferImpl(
          unsafeObj, nativeBaseOffset, newOffsetBytes, capacityBytes, typeIdOut, newCumOffsetBytes, memReqSvr, byteBuf);
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
  public ByteBuffer getByteBuffer() {
    return byteBuf;
  }

  @Override
  Object getUnsafeObject() {
    return unsafeObj;
  }

}
