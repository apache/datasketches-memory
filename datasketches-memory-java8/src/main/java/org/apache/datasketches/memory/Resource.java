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

import java.io.UncheckedIOException;
import java.nio.ByteOrder;

/**
 *  Methods common to all memory access resources, including attributes like byte order and capacity.
 *
 * @author Lee Rhodes
 */
public interface Resource extends AutoCloseable {

  static MemoryRequestServer defaultMemReqSvr = null; //policy choice

  /**
   * Checks that the specified range of bytes is within bounds of this object, throws
   * {@link IllegalArgumentException} if it's not: i. e. if offsetBytes &lt; 0, or length &lt; 0,
   * or offsetBytes + length &gt; {@link #getCapacity()}.
   * @param offsetBytes the given offset in bytes of this object
   * @param lengthBytes the given length in bytes of this object
   */
  void checkValidAndBounds(long offsetBytes, long lengthBytes);

  /**
   * Closes this resource if this can be closed via <em>AutoCloseable</em>.
   * If this operation completes without exceptions, this resource will be marked as <em>not alive</em>,
   * and subsequent operations on this resource will fail with {@link IllegalStateException}.
   *
   * @apiNote This operation is not idempotent; that is, closing an already closed resource <em>always</em>
   * results in an exception being thrown. This reflects a deliberate design choice: resource state transitions
   * should be manifest in the client code; a failure in any of these transitions reveals a bug in the underlying
   * application logic.
   *
   * @throws IllegalStateException if this is an AutoCloseable Resource (Memory mapped files and direct, off-heap
   * memory allocations), and:
   * <ul>
   *  <li>this resource is already closed, or
   *  <li>this method is called from a thread other than the thread owning this resource</li>.
   * </ul>
   *
   * @throws UnsupportedOperationException if this resource is not {@link AutoCloseable}.
   */
  @Override
  default void close() {/* Overridden by the actual AutoCloseable sub-classes. */ }

  /**
   * Returns true if the given object (<em>that</em>) is an instance of this class and has contents equal to
   * this object.
   * @param that the given Resource object
   * @return true if the given object has equal contents to this object.
   */
  default boolean equalTo(Resource that) {
    if (that == null || this.getCapacity() != that.getCapacity()) { return false; }
    else { return equalTo(0, that, 0, that.getCapacity()); }
  }

  /**
   * Returns true if the given Resource has equal contents to
   * this object in the given range of bytes. This will also check two distinct ranges within the
   * same object for equals.
   * @param thisOffsetBytes the starting offset in bytes for this object.
   * @param that the given Resource
   * @param thatOffsetBytes the starting offset in bytes for the given Resource object
   * @param lengthBytes the size of the range in bytes
   * @return true if the given Resource object has equal contents to this object in the given range of bytes.
   */
  boolean equalTo(long thisOffsetBytes, Resource that, long thatOffsetBytes, long lengthBytes);

  /**
   * Forces any changes made to the contents of this memory-mapped Resource to be written to the storage
   * device described by the configured file descriptor.
   *
   * <p>If the file descriptor associated with this memory-mapped Resource resides on a local storage device then when
   * this method returns, it is guaranteed that all changes made to this mapped Resource since it was created, or since
   * this method was last invoked, will have been written to that device.</p>
   *
   * <p>If the file descriptor associated with this memory-mapped Resource does not reside on a local device then no
   * such guarantee is made.</p>
   *
   * <p>If this memory-mapped Resource was not mapped in read/write mode
   * ({@link java.nio.channels.FileChannel.MapMode#READ_WRITE}) then invoking this method may have no effect.
   * In particular, this method has no effect for files mapped in read-only or private
   * mapping modes. This method may or may not have an effect for implementation-specific mapping modes.</p>
   *
   * @throws IllegalStateException if:
   * <ul>
   *  <li>this memory-mapped Resource is not <em>alive</em></li>, or
   *  <li>this method is called from a thread other than the thread owning this resource</li>.
   * </ul>
   *
   * @throws UnsupportedOperationException if this Resource is not memory-mapped, e.g. if
   * {@code isMapped() == false}.
   *
   * @throws UncheckedIOException if there is an I/O error writing the contents of this
   * memory-mapped Resource to the associated storage device
   */
  void force();

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

  /**
   * Returns the MemoryRequestSever or null, if it has not been configured.
   * @return the MemoryRequestSever or null, if it has not been configured.
   */
  MemoryRequestServer getMemoryRequestServer();

  /**
   * Returns the offset of address zero of this object relative to the base address of the
   * backing resource. This does not include the object header for heap arrays nor the initial
   * offset of a memory-mapped file.
   * @return the offset of address zero of this object relative to the base address of the
   * backing resource.
   */
  long getTotalOffset();

  /**
   * Returns true if this Memory is backed by a ByteBuffer.
   * @return true if this Memory is backed by a ByteBuffer.
   */
  boolean isByteBufferResource();

  /**
   * Returns true if the Native ByteOrder is the same as the ByteOrder of the
   * current Buffer or Memory and the same ByteOrder as the given byteOrder.
   * @param byteOrder the given ByteOrder
   * @return true if the Native ByteOrder is the same as the ByteOrder of the
   * current Buffer or Memory and the same ByteOrder as the given byteOrder.
   */
  boolean isByteOrderCompatible(ByteOrder byteOrder);

  /**
   * If true, the backing resource is direct (off-heap) memory.
   * This is the case for allocated direct memory, memory-mapped files,
   * or from a wrapped ByteBuffer that was allocated direct.
   * If false, the backing resource is the Java heap.
   * @return true if the backing resource is direct (off-heap) memory.
   */
  boolean isDirectResource();

  /**
   * Returns true if this instance is a duplicate of a Buffer instance.
   * @return true if this instance is a duplicate of a Buffer instance.
   */
  boolean isDuplicateBufferView();

  /**
   * Returns true if this object is backed by an on-heap primitive array
   * @return true if this object is backed by an on-heap primitive array
   */
  boolean isHeapResource();

  /**
   * Tells whether or not the contents of this memory-mapped Resource is resident in physical memory.
   *
   * <p>A return value of {@code true} implies that it is highly likely that all of the data in this memory-mapped
   * Resource is resident in physical memory and may therefore be accessed without incurring any virtual-memory page
   * faults or I/O operations.</p>
   *
   * <p>A return value of {@code false} does not necessarily imply that all of the data in this memory-mapped Resource
   * is not resident in physical memory.</p>
   *
   * <p>The returned value is a hint, rather than a guarantee, because the underlying operating system may have paged
   * out some of this Resource's data by the time that an invocation of this method returns.</p>
   *
   * @return true if it is likely that all of the data in this memory-mapped Resource is resident in physical memory
   *
   * @throws IllegalStateException if:
   * <ul>
   *  <li>this memory-mapped Resource  is not <em>alive</em></li>, or
   *  <li>this method is called from a thread other than the thread owning this resource</li>.
   * </ul>
   *
   * @throws UnsupportedOperationException if this Resource is not memory-mapped, e.g. if
   * {@code isMapped() == false}.
   */
  boolean isLoaded();

  /**
   * If true, this is a <i>Memory</i> or <i>WritableMemory</i> instance, which provides the Memory API.
   * The Memory API is the principal API for this Memory Component.
   * It provides a rich variety of direct manipulations of four types of resources:
   * On-heap memory, direct (off-heap) memory, memory-mapped files, and ByteBuffers.
   * If false, this is a <i>Buffer</i> or <i>WritableBuffer</i> instance, which provides the Buffer API.
   *
   * <p>The Buffer API is largely parallel to the Memory API except that it adds a positional API
   * similar to that in <i>ByteBuffer</i>.  The positional API is a convenience when iterating over structured
   * arrays, or buffering input or output streams (thus the name).</p>
   *
   * @return true if this is a <i>Memory</i> or <i>WritableMemory</i> instance, which provides the Memory API,
   * otherwise this is a <i>Buffer</i> or <i>WritableBuffer</i> instance, which provides the Buffer API.
   */
  boolean isMemoryApi();

  /**
   * Returns true if the backing resource is a memory-mapped file.
   * @return true if the backing resource is a memory-mapped file.
   */
  boolean isMemoryMappedResource();

  /**
   * If true, all put and get operations will assume the non-native ByteOrder.
   * Otherwise, all put and get operations will assume the native ByteOrder.
   * @return true, if all put and get operations will assume the non-native ByteOrder.
   */
  boolean isNonNativeOrder();

  /**
   * Returns true if this object or the backing resource is read-only.
   * @return true if this object or the backing resource is read-only.
   */
  boolean isReadOnly();

  /**
   * Returns true if this instance is a region view of another Memory or Buffer
   * @return true if this instance is a region view of another Memory or Buffer
   */
  boolean isRegionView();

  /**
   * Returns true if the backing resource of <i>this</i> is identical with the backing resource
   * of <i>that</i>. The capacities must be the same.  If <i>this</i> is a region,
   * the region offset must also be the same.
   * @param that A different non-null Resource
   * @return true if the backing resource of <i>this</i> is the same as the backing resource
   * of <i>that</i>.
   */
  boolean isSameResource(Resource that);

  /**
   * Returns true if this object is valid and has not been closed.
   * This is relevant only for direct (off-heap) memory and Mapped Files.
   * @return true if this object is valid and has not been closed.
   */
  boolean isValid();

  /**
   * Loads the contents of this memory-mapped Resource into physical memory.
   *
   * <p>This method makes a best effort to ensure that, when it returns, this contents of the memory-mapped Resource is
   * resident in physical memory. Invoking this method may cause some number of page faults and
   * I/O operations to occur.</p>
   *
   * @throws IllegalStateException if:
   * <ul>
   *  <li>this memory-mapped Resource  is not <em>alive</em></li>, or
   *  <li>this method is called from a thread other than the thread owning this Resource</li>.
   * </ul>
   *
   * @throws UnsupportedOperationException if this Resource is not memory-mapped, e.g. if
   * {@code isMapped() == false}.
   */
  void load();

  /**
   * Sets the MemoryRequestServer.
   * @param memReqSvr the given MemoryRequestServer.
   */
  void setMemoryRequestServer(MemoryRequestServer memReqSvr);

  /**
   * Returns a description of this object with an optional formatted hex string of the data
   * for the specified a range. Used primarily for testing.
   * @param header a descriptive header
   * @param offsetBytes offset bytes relative to this object start
   * @param lengthBytes number of bytes to convert to a hex string
// @param withData include output listing of byte data in the given range
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
