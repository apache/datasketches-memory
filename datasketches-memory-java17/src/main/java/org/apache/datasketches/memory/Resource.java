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

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * Keeps key configuration state for Memory and Buffer plus some common methods.
 *
 * @author Lee Rhodes
 */
public interface Resource {

  /**
   * Currently used only for test, hold for possible future use
   */
  static final MemoryRequestServer defaultMemReqSvr = null; //policy choice;

  /**
   * Returns true if the given object is an instance of this class and has equal contents to
   * this object.
   * @param that the given Resource object
   * @return true if the given object has equal contents to this object.
   */
  default boolean equalTo(Resource that) {
    if (that == null || this.getCapacity() != that.getCapacity()) return false;
    return equalTo(0, that, 0, that.getCapacity());
  }

  /**
   * Returns true if the given object is an instance of this class and has equal contents to
   * this object in the given range of bytes. This will also check two distinct ranges within the
   * same object for equals.
   * @param thisOffsetBytes the starting offset in bytes for this object.
   * @param that the given Resource object
   * @param thatOffsetBytes the starting offset in bytes for the given Resource object
   * @param lengthBytes the size of the range in bytes
   * @return true if the given Resource object has equal contents to this object in the given range of bytes.
   */
  boolean equalTo(long thisOffsetBytes, Resource that, long thatOffsetBytes, long lengthBytes);

  /**
   * Gets the current Type ByteOrder.
   * This may be different from the ByteOrder of the backing resource and of the Native Byte Order.
   * @return the current Type ByteOrder.
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
   * Is the underlying resource scope alive?
   * @return true, if the underlying resource scope is alive.
   * @see #close()
   */
  boolean isAlive();

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
   * This is the case for allocated direct memory, memory mapped files,
   * or from a wrapped ByteBuffer that was allocated direct.
   * If false, the backing resource is the normal Java heap.
   * @return true if the backing resource is direct (off-heap) memory.
   */
  boolean isDirectResource();

  /**
   * Returns true if this instance is a duplicate of a Buffer instance.
   * @return true if this instance is a duplicate of a Buffer instance.
   */
  boolean isDuplicateBufferView();

  /**
   * If true, this is a <i>Memory</i> or <i>WritableMemory</i> instance, which provides
   * the Memory API.
   * The Memory API is the principal API for this Memory Component.
   * It provides a rich variety of direct manipulations of four types of resources:
   * On-heap memory, direct (off-heap) memory, memory-mapped files, and ByteBuffers.
   * If false, this is a <i>Buffer</i> or <i>WritableBuffer</i> instance, which provides the Buffer API.
   * The Buffer API is largely parallel to the Memory API except that it adds a positional API
   * similar to that in <i>ByteBuffer</i>.  The positional API is a convenience when iterating over structured
   * arrays, or buffering input or output streams (thus the name).
   * @return true if this is a Buffer or WritableBuffer instance, which provides the Buffer API.
   */
  boolean isMemoryApi();

  /**
   * Returns true if the backing resource is a memory mapped file.
   * @return true if the backing resource is a memory mapped file.
   */
  boolean isMemoryMappedFileResource();

  /**
   * If true, all put and get operations will assume the non-native ByteOrder.
   * Otherwise, all put and get operations will assume the native ByteOrder.
   * @return true, if all put and get operations will assume the non-native ByteOrder.
   */
  boolean isNonNativeOrder();

  /**
   * Returns true if this or the backing resource is read-only.
   * @return true if this or the backing resource is read-only.
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
   * @param that A different non-null object
   * @return true if the backing resource of <i>this</i> is the same as the backing resource
   * of <i>that</i>.
   */
  boolean isSameResource(Resource that);

  /**
   * Returns a description of this object with an optional formatted hex string of the data
   * for the specified a range. Used primarily for testing.
   * @param comment a description
   * @param offsetBytes offset bytes relative to this object start
   * @param lengthBytes number of bytes to convert to a hex string
   * @param withData include output listing of byte data in the given range
   * @return a description and hex output in a human readable format.
   */
  String toHexString(String comment, long offsetBytes, int lengthBytes, boolean withData);

  /**
   * Returns a 64-bit hash from a single long. This method has been optimized for speed when only
   * a single hash of a long is required.
   * @param in A long.
   * @param seed A long valued seed.
   * @return the hash.
   */
  long xxHash64(long in, long seed);

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

  //NEW HERE WITH JAVA 17

  /**
   * Returns a ByteBuffer view of this Memory object with the given ByteOrder.
   * Some of the properties of the returned buffer are linked to the properties of this Memory object.
   * For instance, if this Memory object is immutable (i.e., read-only, see isReadOnly()),
   * then the resulting buffer is read-only (see Buffer.isReadOnly().
   * Additionally, if this is a native memory segment, the resulting buffer is direct
   * (see ByteBuffer.isDirect()). The endianness of the returned buffer will be set to
   * the given ByteOrder.
   * @param order the given ByteOrder.
   * @return a ByteBuffer view of this Memory object with the given ByteOrder.
   * @throws UnsupportedOperationException - if this segment cannot be mapped onto a ByteBuffer instance,
   * e.g. because it models an heap-based segment that is not based on a byte[]),
   * or if its size is greater than Integer.MAX_VALUE.
   */
  ByteBuffer asByteBufferView(ByteOrder order);

  /**
   * For off-heap segments, this closes the controlling ResourceScope. If the segment is
   * not off-heap, this does nothing.
   */
  void close();

  /**
   * Forces any changes made to the contents of this mapped segment to be written to the storage device described
   * by the mapped segment's file descriptor. Please refer to
   * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#force()">force()</a>
   */
  void force();

  /**
   * Tells whether or not the contents of this mapped segment is resident in physical memory. Please refer to
   * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#isLoaded()">isLoaded()</a>
   * @return true if it is likely that the contents of this segment is resident in physical memory.
   */
  boolean isLoaded();

  /**
   * Loads the contents of this mapped segment into physical memory. Please refer to
   * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#load()">load()</a>
   *
   * @throws IllegalStateException if the scope associated with the underlying MemorySegment has been closed,
   * or if access occurs from a thread other than the thread owning that scope.
   * @throws UnsupportedOperationException if this segment is not a mapped memory segment, e.g. if
   * {@code isMapped() == false}.
   */
  void load();

  /**
   * See <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#mismatch(jdk.incubator.foreign.MemorySegment)">mismatch</a>
   * @param that the other Resource
   * @return the relative offset, in bytes, of the first mismatch between this and the given other Resource object,
   * otherwise -1 if no mismatch
   */
  long mismatch(Resource that);

  /**
   * Returns the resource scope associated with this memory segment.
   * @return the resource scope associated with this memory segment.
   */
  ResourceScope scope();

  //  /**
  //   * Sets the default MemoryRequestServer to be used in case of capacity overflow of off-heap
  //   * (Direct or Native) allocated Memory or of on-heap allocated Memory.
  //   * @param memReqSvr the given default MemoryRequestServer
  //   */
  //  void setMemoryRequestServer(MemoryRequestServer memReqSvr);

  /**
   * Returns a new ByteBuffer with a copy of the data from this Memory object.
   * This new ByteBuffer will be writable, on heap, and with the endianness specified
   * by the given ByteOrder.
   * @param order the given ByteOrder.
   * @return a new ByteBuffer with a copy of the data from this Memory object.
   */
  ByteBuffer toByteBuffer(ByteOrder order);

  /**
   * Returns a copy of the underlying MemorySegment.
   * The size is limited to <i>Integer.MAX_VALUE</i>.
   * @return a copy of the underlying MemorySegment
   */
  MemorySegment toMemorySegment();

  /**
   * Unloads the contents of this mapped segment from physical memory. Please refer to
   * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#unload()">unload()</a>
   */
  void unload();

}
