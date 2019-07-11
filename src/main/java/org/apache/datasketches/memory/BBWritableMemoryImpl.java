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

package org.apache.datasketches.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for ByteBuffer, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBWritableMemoryImpl extends WritableMemoryImpl {
  private static final int id = MEMORY | NATIVE | BYTEBUF;
  private final Object unsafeObj;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.
  private final byte typeId;

  BBWritableMemoryImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final ByteBuffer byteBuf) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.byteBuf = byteBuf;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
    return isNativeCpuByteOrder(byteOrder)
        ? new BBWritableMemoryImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, getByteBuffer())
        : new BBNonNativeWritableMemoryImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, getByteBuffer());
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | (readOnly ? READONLY : 0);
    return isNativeCpuByteOrder(byteOrder)
        ? new BBWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, byteBuf, this)
        : new BBNonNativeWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, byteBuf, this);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf;
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
  Object getUnsafeObject() {
    assertValid();
    return unsafeObj;
  }

}
