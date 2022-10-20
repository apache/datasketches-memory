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

import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Acquires access to a ByteBuffer.
 *
 * @author Lee Rhodes
 * @author Praveenkumar Venkatesan
 * @author Roman Leventov
 */
@SuppressWarnings("restriction")
final class AccessByteBuffer {

  static final ByteBuffer ZERO_READ_ONLY_DIRECT_BYTE_BUFFER =
      ByteBuffer.allocateDirect(0).asReadOnlyBuffer();

  private static final long NIO_BUFFER_ADDRESS_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.Buffer.class, "address");
  private static final long NIO_BUFFER_CAPACITY_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.Buffer.class, "capacity");
  private static final long BYTE_BUFFER_HB_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.ByteBuffer.class, "hb");
  private static final long BYTE_BUFFER_OFFSET_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.ByteBuffer.class, "offset");

  final long nativeBaseOffset;
  final long capacityBytes;
  final long regionOffset;
  final Object unsafeObj;
  final boolean resourceReadOnly;
  final ByteOrder byteOrder; //not used externally, here for reference.

  /**
   * The given ByteBuffer may be either readOnly or writable
   * @param byteBuf the given ByteBuffer
   */
  AccessByteBuffer(final ByteBuffer byteBuf) {
    capacityBytes = byteBuf.capacity();
    resourceReadOnly = byteBuf.isReadOnly();
    byteOrder = byteBuf.order();
    final boolean direct = byteBuf.isDirect();
    if (direct) {
      nativeBaseOffset = ((sun.nio.ch.DirectBuffer) byteBuf).address();
      unsafeObj = null;
      regionOffset = 0L; //address() is already adjusted for direct slices, so regionOffset = 0
    } else {
      nativeBaseOffset = 0L;
      // ByteBuffer.arrayOffset() and ByteBuffer.array() throw ReadOnlyBufferException if
      // ByteBuffer is read-only. This uses reflection for both writable and read-only cases.
      // Includes the slice() offset for heap.
      regionOffset = unsafe.getInt(byteBuf, BYTE_BUFFER_OFFSET_FIELD_OFFSET);
      unsafeObj = unsafe.getObject(byteBuf, BYTE_BUFFER_HB_FIELD_OFFSET);
    }
  }

  /**
   * This method is adapted from
   * https://github.com/odnoklassniki/one-nio/blob/master/src/one/nio/mem/DirectMemory.java
   * : wrap(...). See LICENSE.
   */
  static ByteBuffer getDummyReadOnlyDirectByteBuffer(final long address, final int capacity) {
    final ByteBuffer buf = ZERO_READ_ONLY_DIRECT_BYTE_BUFFER.duplicate();
    unsafe.putLong(buf, NIO_BUFFER_ADDRESS_FIELD_OFFSET, address);
    unsafe.putInt(buf, NIO_BUFFER_CAPACITY_FIELD_OFFSET, capacity);
    buf.limit(capacity);
    return buf;
  }

}
