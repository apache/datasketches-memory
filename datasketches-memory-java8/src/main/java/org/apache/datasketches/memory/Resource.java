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

import org.apache.datasketches.memory.internal.ResourceImpl;

/**
 *  Methods common to all memory access resources, including attributes like byte order and capacity.
 *
 * @author Lee Rhodes
 */
public interface Resource {

  /**
   * Checks that the specified range of bytes is within bounds of this object, throws
   * {@link IllegalArgumentException} if it's not: i. e. if offsetBytes &lt; 0, or length &lt; 0,
   * or offsetBytes + length &gt; {@link #getCapacity()}.
   * @param offsetBytes the given offset in bytes of this object
   * @param lengthBytes the given length in bytes of this object
   */
  void checkValidAndBounds(long offsetBytes, long lengthBytes);

  /**
   * The placeholder for the default MemoryRequestServer, if set at all.
   */
  static final MemoryRequestServer defaultMemReqSvr = null; //new DefaultMemoryRequestServer();



  /**
   * Returns true if the given object is an instance of this class and has equal data contents.
   * @param that the given object
   * @return true if the given Object is an instance of this class and has equal data contents.
   */
  @Override
  boolean equals(Object that);

  /**
   * Returns true if the given object is an instance of this class and has equal contents to
   * this object in the given range of bytes. This will also check two distinct ranges within the
   * same object for equals.
   * @param thisOffsetBytes the starting offset in bytes for this object.
   * @param that the given object
   * @param thatOffsetBytes the starting offset in bytes for the given object
   * @param lengthBytes the size of the range in bytes
   * @return true if the given object has equal contents to this object in the given range of
   * bytes.
   */
  boolean equalTo(long thisOffsetBytes, Object that,
      long thatOffsetBytes, long lengthBytes);

  /**
   * Gets the backing ByteBuffer if it exists, otherwise returns null.
   * @return the backing ByteBuffer if it exists, otherwise returns null.
   */
  ByteBuffer getByteBuffer();

  /**
   * Gets the current ByteOrder.
   * This may be different from the ByteOrder of the backing resource and {@link ByteOrder#nativeOrder()}
   * @return the current ByteOrder.
   */
  ByteOrder getByteOrder();

  /**
   * Gets the capacity of this object in bytes
   * @return the capacity of this object in bytes
   */
  long getCapacity();

  //Monitoring
  /**
   * Gets the current size of active direct memory allocated.
   * @return the current size of active direct memory allocated.
   */
  static long getCurrentDirectMemoryAllocated() {
    return ResourceImpl.getCurrentDirectMemoryAllocated();
  }

  /**
   * Gets the current number of active direct memory allocations.
   * @return the current number of active direct memory allocations.
   */
  static long getCurrentDirectMemoryAllocations() {
    return ResourceImpl.getCurrentDirectMemoryAllocations();
  }

  /**
   * Gets the current size of active direct memory map allocated.
   * @return the current size of active direct memory map allocated.
   */
  static long getCurrentDirectMemoryMapAllocated() {
    return ResourceImpl.getCurrentDirectMemoryMapAllocated();
  }

  /**
   * Gets the current number of active direct memory map allocations.
   * @return the current number of active direct memory map allocations.
   */
  static long getCurrentDirectMemoryMapAllocations() {
    return ResourceImpl.getCurrentDirectMemoryMapAllocations();
  }
  //End Monitoring

  /**
   * Returns the offset of address zero of this object relative to the base address of the
   * backing resource. This does not include the object header for heap arrays nor the initial
   * offset of a memory-mapped file.
   * @return the offset of address zero of this object relative to the base address of the
   * backing resource.
   */
  long getTotalOffset();

  /**
   * Returns true if this object is backed by an on-heap primitive array
   * @return true if this object is backed by an on-heap primitive array
   */
  boolean hasArray();

  /**
   * Returns true if this Memory is backed by a ByteBuffer.
   * @return true if this Memory is backed by a ByteBuffer.
   */
  boolean hasByteBuffer();

  /**
   * Returns the hashCode of this object.
   *
   * <p>The hash code of this object depends upon all of its contents.
   * Because of this, it is inadvisable to use these objects as keys in hash maps
   * or similar data structures unless it is known that their contents will not change.</p>
   *
   * <p>If it is desirable to use these objects in a hash map depending only on object identity,
   * than the {@link java.util.IdentityHashMap} can be used.</p>
   *
   * @return the hashCode of this object.
   */
  @Override
  int hashCode();

  /**
   * Returns true if the Native ByteOrder is the same as the ByteOrder of the
   * current Buffer or Memory and the same ByteOrder as the given byteOrder.
   * @param byteOrder the given ByteOrder
   * @return true if the Native ByteOrder is the same as the ByteOrder of the
   * current Buffer or Memory and the same ByteOrder as the given byteOrder.
   */
  boolean isByteOrderCompatible(ByteOrder byteOrder);

  /**
   * Returns true if the backing resource is direct (off-heap) memory.
   * This is the case for allocated direct memory, memory mapped files,
   * @return true if the backing resource is direct (off-heap) memory.
   */
  boolean isDirect();

  /**
   * Returns true if this object or the backing resource is read-only.
   * @return true if this object or the backing resource is read-only.
   */
  boolean isReadOnly();

  /**
   * Returns true if the backing resource of <i>this</i> is identical with the backing resource
   * of <i>that</i>. The capacities must be the same.  If <i>this</i> is a region,
   * the region offset must also be the same.
   * @param that A different non-null object
   * @return true if the backing resource of <i>this</i> is the same as the backing resource
   * of <i>that</i>.
   */
  boolean isSameResource(Object that);

  /**
   * Returns true if this object is valid and has not been closed.
   * This is relevant only for direct (off-heap) memory and Mapped Files.
   * @return true if this object is valid and has not been closed.
   */
  boolean isValid();

  /**
   * Returns a formatted hex string of a range of this object.
   * Used primarily for testing.
   * @param header a descriptive header
   * @param offsetBytes offset bytes relative to this object start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  String toHexString(String header, long offsetBytes, int lengthBytes);

  /**
   * Returns the 64-bit hash of the sequence of bytes in this object specified by
   * <i>offsetBytes</i>, <i>lengthBytes</i> and a <i>seed</i>.  Note that the sequence of bytes is
   * always processed in the same order independent of endianness.
   *
   * @param offsetBytes the given offset in bytes to the first byte of the byte sequence.
   * @param lengthBytes the given length in bytes of the byte sequence.
   * @param seed the given long seed.
   * @return the 64-bit hash of the sequence of bytes in this object specified by
   * <i>offsetBytes</i> and <i>lengthBytes</i>.
   */
  long xxHash64(long offsetBytes, long lengthBytes, long seed);

  /**
   * Returns a 64-bit hash from a single long. This method has been optimized for speed when only
   * a single hash of a long is required.
   * @param in A long.
   * @param seed A long valued seed.
   * @return the hash.
   */
  long xxHash64(long in, long seed);

}
