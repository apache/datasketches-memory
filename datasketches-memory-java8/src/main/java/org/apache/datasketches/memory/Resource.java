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
 * The base class for Memory and Buffer plus some common static variables and check methods.
 *
 * @author Lee Rhodes
 */
public interface Resource extends AutoCloseable {

  //MemoryRequestServer logic
  
  /**
   * The default MemoryRequestServer is used if not specified by the user.
   */
  static MemoryRequestServer defaultMemReqSvr = new DefaultMemoryRequestServer();

  /**
   * Gets the MemoryRequestServer object, if set, for the below resources to request additional memory.
   *
   * <p>WritableMemory enables this for ByteBuffer, Heap and Off-heap Memory backed resources.</p>
   *
   * <p>WritableBuffer enables this for ByteBuffer backed resources. However, the object returned is in the form of
   * a WritableMemory. To convert to WritableBuffer use asWritableBuffer(). To enable for Heap and Off-heap Buffer
   * resources, use the WritableMemory to configure and then call asWritableBuffer().</p>
   *
   * <p>Map backed resources will always return null.</p>
   *
   * <p>The user must customize the actions of the MemoryRequestServer by
   * implementing the MemoryRequestServer interface.</p>
   *
   * <p>For WritableMemory, to enable at runtime set your custom MemoryRequestServer using one of these methods:</p>
   * <ul><li>{@link WritableMemory#allocateDirect(long, ByteOrder, MemoryRequestServer)}</li>
   * <li>{@link WritableMemory#allocate(int, ByteOrder, MemoryRequestServer)}</li>
   * <li>{@link WritableMemory#writableWrap(ByteBuffer, ByteOrder, MemoryRequestServer)}</li>
   * </ul>
   *
   * <p>ForWritableBuffer, to enable at runtime set your custom MemoryRequestServer using the following method:</p>
   * <ul>
   * <li>{@link WritableBuffer#writableWrap(ByteBuffer, ByteOrder, MemoryRequestServer)}</li>
   * </ul>
   *
   * <p>Simple implementation examples include the DefaultMemoryRequestServer in the main source tree, as well as
   * the ExampleMemoryRequestServerTest and the use with ByteBuffer documented in the DruidIssue11544Test
   * in the test source tree.</p>
   *
   * @return the MemoryRequestServer object or null.
   */
  MemoryRequestServer getMemoryRequestServer();
  
  /**
   * Returns true if the MemoryRequestServer has been configured by the user.
   * @return true if the MemoryRequestServer has been configured by the user..
   */
  boolean hasMemoryRequestServer();
  
  /**
   * Sets the Default MemoryRequestServer
   * @param memReqSvr the given MemoryRequestServer.
   */
  void setMemoryRequestServer(MemoryRequestServer memReqSvr);
  
  //***
  
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
   * @throws IllegalStateException if this Resource is not <em>alive</em>.
   * @throws IllegalStateException if this method is not accessed from the owning thread.
   * @throws UnsupportedOperationException if this resource is not {@link AutoCloseable}.
   */
  @Override
  void close();

  /**
   * Return true if this resource is closeable.
   * @return true if this resource is closeable.
   */
  boolean isCloseable();
  
  /**
   * Returns true if the given object (<em>that</em>) is an instance of this class and has contents equal to
   * this object.
   * @param that the given Resource object
   * @return true if the given object has equal contents to this object.
   * @see #equalTo(long, Resource, long, long)
   */
  default boolean equalTo(Resource that) {
    if (that == null || this.getCapacity() != that.getCapacity()) { return false; }
    return equalTo(0, that, 0, that.getCapacity());
  }

  /**
   * Returns true if the given Resource has equal contents to
   * this object in the given range of bytes. This will also check two distinct ranges within the same object for equals.
   * @param thisOffsetBytes the starting offset in bytes for this object.
   * @param that the given Resource
   * @param thatOffsetBytes the starting offset in bytes for the given Resource object
   * @param lengthBytes the size of the range in bytes
   * @return true if the given Resource object has equal contents to this object in the given range of bytes.
   * @throws IllegalStateException if either resource is not <em>alive</em>.
   * @throws MemoryBoundsException if there is a bounds violation.
   */
  boolean equalTo(
      long thisOffsetBytes, 
      Resource that, 
      long thatOffsetBytes, 
      long lengthBytes);

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
   * @throws IllegalStateException if this Resource is not <em>alive</em>.
   * @throws IllegalStateException if this method is not accessed from the owning thread.
   * @throws UnsupportedOperationException if this Resource is not memory-mapped, e.g. if {@code isMapped() == false}.
   * @throws ReadOnlyException if this Resource is read-only.
   * @throws RuntimeException if there is some other error writing the contents of this
   * memory-mapped Resource to the associated storage device.
   */
  void force();

  /**
   * Gets the capacity of this object in bytes
   * @return the capacity of this object in bytes
   */
  long getCapacity();

  /**
   * Gets the cumulative offset in bytes of this object from the backing resource.
   * This offset may also include other offset components such as the native off-heap
   * memory address, DirectByteBuffer split offsets, region offsets, and unsafe arrayBaseOffsets.
   *
   * @return the cumulative offset in bytes of this object from the backing resource.
   */
  default long getCumulativeOffset() {
    return getCumulativeOffset(0);
  }

  /**
   * Gets the cumulative offset in bytes of this object from the backing resource including the given
   * offsetBytes. This offset may also include other offset components such as the native off-heap
   * memory address, DirectByteBuffer split offsets, region offsets, and unsafe arrayBaseOffsets.
   *
   * @param offsetBytes offset to be added to the cumulative offset.
   * @return the cumulative offset in bytes of this object from the backing resource including the
   * given offsetBytes.
   */
  long getCumulativeOffset(long offsetBytes);
  
  /**
   * Returns the offset of address zero of this object relative to the base address of the
   * backing resource. This does not include the object header for heap arrays nor the initial
   * offset of a memory-mapped file.
   * @return the offset of address zero of this object relative to the base address of the
   * backing resource.
   */
  long getRelativeOffset();

  /**
   * Gets the current ByteOrder.
   * This may be different from the ByteOrder of the backing resource and {@link ByteOrder#nativeOrder()}
   * @return the current ByteOrder.
   */
  ByteOrder getTypeByteOrder();
  
  /**
   * Returns true if this Memory is backed by a ByteBuffer.
   * @return true if this Memory is backed by a ByteBuffer.
   */
  boolean hasByteBuffer();

  /**
   * Returns true if the Native ByteOrder is the same as the ByteOrder of the
   * current Buffer or Memory and the same ByteOrder as the given byteOrder.
   * @param byteOrder the given ByteOrder
   * @return true if the Native ByteOrder is the same as the ByteOrder of the
   * current Buffer or Memory and the same ByteOrder as the given byteOrder.
   */
  boolean isByteOrderCompatible(ByteOrder byteOrder);

  /**
   * If true, the backing resource is off-heap memory.
   * This is the case for allocated off-heap memory, memory-mapped files,
   * or from a wrapped ByteBuffer that was allocated off-heap.
   * If false, the backing resource is the Java heap.
   * @return true if the backing resource is off-heap memory.
   */
  boolean isDirect();

  /**
   * Returns true if this instance is a duplicate of a Buffer instance.
   * @return true if this instance is a duplicate of a Buffer instance.
   */
  boolean isDuplicate();

  /**
   * Returns true if this object is backed by an on-heap primitive array or an on-heap ByteBuffer.
   * @return true if this object is backed by an on-heap primitive array or an on-heap ByteBuffer.
   */
  boolean isHeap();

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
   * @throws IllegalStateException if this Resource is not <em>alive</em>.
   * @throws IllegalStateException if this method is not accessed from the owning thread.
   * @throws UnsupportedOperationException if this Resource is not memory-mapped, e.g. if {@code isMapped() == false}.
   */
  boolean isLoaded();

  /**
   * If true, this is a <i>Memory</i> or <i>WritableMemory</i> instance, which provides the Memory API.
   * The Memory API is the principal API for this Memory Component.
   * It provides a rich variety of direct manipulations of four types of resources:
   * On-heap memory, off-heap memory, memory-mapped files, and ByteBuffers.
   * If false, this is a <i>Buffer</i> or <i>WritableBuffer</i> instance, which provides the Buffer API.
   *
   * <p>The Buffer API is largely parallel to the Memory API except that it adds a positional API
   * similar to that in <i>ByteBuffer</i>.  The positional API is a convenience when iterating over structured
   * arrays, or buffering input or output streams (thus the name).</p>
   *
   * @return true if this is a <i>Memory</i> or <i>WritableMemory</i> instance, which provides the Memory API,
   * otherwise this is a <i>Buffer</i> or <i>WritableBuffer</i> instance, which provides the Buffer API.
   */
  boolean isMemory();

  /**
   * Returns true if the backing resource is a memory-mapped file.
   * @return true if the backing resource is a memory-mapped file.
   */
  boolean isMapped();

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
   * Returns true if this object is alive and has not been closed.
   * This is relevant only for off-heap memory and memory-mapped Files.
   * @return true if this object is alive and has not been closed.
   */
  boolean isAlive();

  /**
   * Loads the contents of this memory-mapped Resource into physical memory.
   *
   * <p>This method makes a best effort to ensure that, when it returns, this contents of the memory-mapped Resource is
   * resident in physical memory. Invoking this method may cause some number of page faults and
   * I/O operations to occur.</p>
   *
   * @throws IllegalStateException if this Resource is not <em>alive</em>.
   * @throws IllegalStateException if this method is not accessed from the owning thread.
   * @throws UnsupportedOperationException if this Resource is not memory-mapped, e.g. if {@code isMapped() == false}.
   */
  void load();

  /**
   * Returns a description of this object with an optional formatted hex string of the data
   * for the specified a range. Used primarily for testing.
   * @param header a descriptive header
   * @param offsetBytes offset bytes relative to this object start
   * @param lengthBytes number of bytes to convert to a hex string
   * @param withData include output listing of byte data in the given range
   * @return a formatted hex string in a human readable array
   */
  String toString(
      String header, 
      long offsetBytes, 
      int lengthBytes, 
      boolean withData);

  /**
   * Returns a brief description of this object.
   * @return a brief description of this object.
   */
  @Override
  String toString();
  
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
  long xxHash64(
      long offsetBytes, 
      long lengthBytes, 
      long seed);

  /**
   * Returns a 64-bit hash from a single long. This method has been optimized for speed when only
   * a single hash of a long is required.
   * @param in A long.
   * @param seed A long valued seed.
   * @return the hash.
   */
  long xxHash64(
      long in, 
      long seed);

}
