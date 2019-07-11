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

import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for heap-based, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class HeapNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private static final int id = MEMORY | NONNATIVE | HEAP;
  private final Object unsafeObj;
  private final byte typeId;

  HeapNonNativeWritableMemoryImpl(
      final Object unsafeObj,
      final long regionOffset,
      final long capacityBytes,
      final int typeId) {
    super(unsafeObj, 0L, regionOffset, capacityBytes);
    this.unsafeObj = unsafeObj;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
    return isNativeCpuByteOrder(byteOrder)
        ? new HeapWritableMemoryImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes,
            type)
        : new HeapNonNativeWritableMemoryImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes,
            type);
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | (readOnly ? READONLY : 0);
    return isNativeCpuByteOrder(byteOrder)
        ? new HeapWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(),
            type, this)
        : new HeapNonNativeWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(),
            type, this);
  }

  @Override
  int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  Object getUnsafeObject() {
    return unsafeObj;
  }

}
