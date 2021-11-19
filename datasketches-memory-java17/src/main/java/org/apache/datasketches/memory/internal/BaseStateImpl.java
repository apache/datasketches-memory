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

import static org.apache.datasketches.memory.internal.UnsafeUtil.LS;
import static org.apache.datasketches.memory.internal.UnsafeUtil.assertBounds;
import static org.apache.datasketches.memory.internal.UnsafeUtil.checkBounds;
import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.ReadOnlyException;

/**
 * Keeps key configuration state for MemoryImpl and BufferImpl plus some common static variables
 * and check methods.
 *
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
public abstract class BaseStateImpl implements BaseState {

  //Monitoring
  static final AtomicLong currentDirectMemoryAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryAllocated_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocated_ = new AtomicLong();

  //class type IDs. Do not change the bit orders
  //The first 3 bits are set dynamically
  // 0000 0XXX
  static final int READONLY = 1;
  static final int REGION = 2;
  static final int DUPLICATE = 4;

  //The following 4 bits are set by the 16 leaf nodes
  // 000X X000
  static final int HEAP = 0;
  static final int DIRECT = 1 << 3;
  static final int MAP = 2 << 3;
  static final int BYTEBUF = 3 << 3;

  // 00X0 0000
  static final int NATIVE = 0;
  static final int NONNATIVE = 1 << 5;

  // 0X00 0000
  static final int MEMORY = 0;
  static final int BUFFER = 1 << 6;

  private final long capacityBytes_;

  /**
   * This becomes the base offset used by all Unsafe calls. It is cumulative in that in includes
   * all offsets from regions, user-defined offsets when creating MemoryImpl, and the array object
   * header offset when creating MemoryImpl from primitive arrays.
   */
  private final long cumBaseOffset_;

  /**
   *
   * @param unsafeObj The primitive backing array. It may be null. Used by Unsafe calls.
   * @param nativeBaseOffset The off-heap memory address including DirectByteBuffer split offsets.
   * @param regionOffset This offset defines address zero of this object (usually a region)
   * relative to address zero of the backing resource. It is used to compute cumBaseOffset.
   * This will be loaded from heap ByteBuffers, which have a similar field used for slices.
   * It is used by region() and writableRegion().
   * This offset does not include the size of an object array header, if there is one.
   * @param capacityBytes the capacity of this object. Used by all methods when checking bounds.
   */
  BaseStateImpl(final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes) {
    capacityBytes_ = capacityBytes;
    cumBaseOffset_ = regionOffset + (unsafeObj == null
        ? nativeBaseOffset
        : UnsafeUtil.getArrayBaseOffset(unsafeObj.getClass()));
  }

  //Byte Order Related

  @Override
  public final ByteOrder getTypeByteOrder() {
    return isNonNativeType() ? Util.NON_NATIVE_BYTE_ORDER : ByteOrder.nativeOrder();
  }

  /**
   * Returns true if the given byteOrder is the same as the native byte order.
   * @param byteOrder the given byte order
   * @return true if the given byteOrder is the same as the native byte order.
   */
  public static boolean isNativeByteOrder(final ByteOrder byteOrder) {
    if (byteOrder == null) {
      throw new IllegalArgumentException("ByteOrder parameter cannot be null.");
    }
    return ByteOrder.nativeOrder() == byteOrder;
  }

  @Override
  public final boolean isByteOrderCompatible(final ByteOrder byteOrder) {
    final ByteOrder typeBO = getTypeByteOrder();
    return typeBO == ByteOrder.nativeOrder() && typeBO == byteOrder;
  }

  @Override
  public final boolean equals(final Object that) {
    if (this == that) { return true; }
    return that instanceof BaseStateImpl
      ? CompareAndCopy.equals(this, (BaseStateImpl) that)
      : false;
  }

  @Override
  public final boolean equalTo(final long thisOffsetBytes, final Object that,
      final long thatOffsetBytes, final long lengthBytes) {
    return that instanceof BaseStateImpl
      ? CompareAndCopy.equals(this, thisOffsetBytes, (BaseStateImpl) that, thatOffsetBytes, lengthBytes)
      : false;
  }

  //Overridden by ByteBuffer Leafs
  @Override
  public ByteBuffer getByteBuffer() {
    return null;
  }

  @Override
  public final long getCapacity() {
    assertValid();
    return capacityBytes_;
  }

  @Override
  public final long getCumulativeOffset() {
    assertValid();
    return cumBaseOffset_;
  }

  @Override
  public final long getCumulativeOffset(final long offsetBytes) {
    assertValid();
    return cumBaseOffset_ + offsetBytes;
  }

  //Documented in WritableMemory and WritableBuffer interfaces.
  //Implemented in the Leaf nodes; Required here by toHex(...).
  abstract MemoryRequestServer getMemoryRequestServer();

  //Overridden by ByteBuffer, Direct and Map leafs
  long getNativeBaseOffset() {
    return 0;
  }

  @Override
  public final long getRegionOffset() {
    final Object unsafeObj = getUnsafeObject();
    return unsafeObj == null
        ? cumBaseOffset_ - getNativeBaseOffset()
        : cumBaseOffset_ - UnsafeUtil.getArrayBaseOffset(unsafeObj.getClass());
  }

  @Override
  public final long getRegionOffset(final long offsetBytes) {
    return getRegionOffset() + offsetBytes;
  }

  //Overridden by all leafs
  abstract int getTypeId();

  //Overridden by Heap and ByteBuffer Leafs. Made public as getArray() in WritableMemoryImpl and
  // WritableBufferImpl
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public final boolean hasArray() {
    assertValid();
    return getUnsafeObject() != null;
  }

  @Override
  public final int hashCode() {
    return (int) xxHash64(0, capacityBytes_, 0); //xxHash64() calls checkValid()
  }

  @Override
  public final long xxHash64(final long offsetBytes, final long lengthBytes, final long seed) {
    checkValid();
    return XxHash64.hash(getUnsafeObject(), cumBaseOffset_ + offsetBytes, lengthBytes, seed);
  }

  @Override
  public final long xxHash64(final long in, final long seed) {
    return XxHash64.hash(in, seed);
  }

  @Override
  public final boolean hasByteBuffer() {
    assertValid();
    return getByteBuffer() != null;
  }

  @Override
  public final boolean isDirect() {
    return getUnsafeObject() == null;
  }

  @Override
  public final boolean isReadOnly() {
    assertValid();
    return isReadOnlyType();
  }

  @Override
  public final boolean isSameResource(final Object that) {
    checkValid();
    if (that == null) { return false; }
    final BaseStateImpl that1 = (BaseStateImpl) that;
    that1.checkValid();
    if (this == that1) { return true; }

    return cumBaseOffset_ == that1.cumBaseOffset_
            && capacityBytes_ == that1.capacityBytes_
            && getUnsafeObject() == that1.getUnsafeObject()
            && getByteBuffer() == that1.getByteBuffer();
  }

  //Overridden by Direct and Map leafs
  @Override
  public boolean isValid() {
    return true;
  }

  //ASSERTS AND CHECKS
  final void assertValid() {
    assert isValid() : "MemoryImpl not valid.";
  }

  void checkValid() {
    if (!isValid()) {
      throw new IllegalStateException("MemoryImpl not valid.");
    }
  }

  final void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    // capacityBytes_ is intentionally read directly instead of calling getCapacity()
    // because the later can make JVM to not inline the assert code path (and entirely remove it)
    // even though it does nothing in production code path.
    assertBounds(offsetBytes, lengthBytes, capacityBytes_);
  }

  final void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    // capacityBytes_ is intentionally read directly instead of calling getCapacity()
    // because the later can make JVM to not inline the assert code path (and entirely remove it)
    // even though it does nothing in production code path.
    assertBounds(offsetBytes, lengthBytes, capacityBytes_);
    assert !isReadOnly() : "MemoryImpl is read-only.";
  }

  @Override
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    //read capacityBytes_ directly to eliminate extra checkValid() call
    checkBounds(offsetBytes, lengthBytes, capacityBytes_);
  }

  final void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    //read capacityBytes_ directly to eliminate extra checkValid() call
    checkBounds(offsetBytes, lengthBytes, capacityBytes_);
    if (isReadOnly()) {
      throw new ReadOnlyException("MemoryImpl is read-only.");
    }
  }

  //TYPE ID Management
  final boolean isReadOnlyType() {
    return (getTypeId() & READONLY) > 0;
  }

  final static byte setReadOnlyType(final byte type, final boolean readOnly) {
    return (byte)((type & ~1) | (readOnly ? READONLY : 0));
  }

  final boolean isRegionType() {
    return (getTypeId() & REGION) > 0;
  }

  final boolean isDuplicateType() {
    return (getTypeId() & DUPLICATE) > 0;
  }

  //The following are set by the leaf nodes
  final boolean isBufferType() {
    return (getTypeId() & BUFFER) > 0;
  }

  final boolean isNonNativeType() {
    return (getTypeId() & NONNATIVE) > 0;
  }

  final boolean isHeapType() {
    return (getTypeId() >>> 3 & 3) == 0;
  }

  final boolean isDirectType() {
    return (getTypeId() >>> 3 & 3) == 1;
  }

  final boolean isMapType() {
    return (getTypeId() >>> 3 & 3) == 2;
  }

  final boolean isBBType() {
    return (getTypeId() >>> 3 & 3) == 3;
  }


  //TO STRING
  /**
   * Decodes the resource type. This is primarily for debugging.
   * @param typeId the given typeId
   * @return a human readable string.
   */
  public static final String typeDecode(final int typeId) {
    final StringBuilder sb = new StringBuilder();
    final int group1 = typeId & 0x7;
    switch (group1) {
      case 1 : sb.append("ReadOnly, "); break;
      case 2 : sb.append("Region, "); break;
      case 3 : sb.append("ReadOnly Region, "); break;
      case 4 : sb.append("Duplicate, "); break;
      case 5 : sb.append("ReadOnly Duplicate, "); break;
      case 6 : sb.append("Region Duplicate, "); break;
      case 7 : sb.append("ReadOnly Region Duplicate, "); break;
      default: break;
    }
    final int group2 = (typeId >>> 3) & 0x3;
    switch (group2) {
      case 0 : sb.append("Heap, "); break;
      case 1 : sb.append("Direct, "); break;
      case 2 : sb.append("Map, "); break;
      case 3 : sb.append("ByteBuffer, "); break;
      default: break;
    }
    final int group3 = (typeId >>> 5) & 0x1;
    switch (group3) {
      case 0 : sb.append("Native, "); break;
      case 1 : sb.append("NonNative, "); break;
      default: break;
    }
    final int group4 = (typeId >>> 6) & 0x1;
    switch (group4) {
      case 0 : sb.append("Memory"); break;
      case 1 : sb.append("Buffer"); break;
      default: break;
    }
    return sb.toString();
  }

  @Override
  public final String toHexString(final String header, final long offsetBytes,
      final int lengthBytes) {
    checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Parameters     : ").append(call);
    return toHex(this, sb.toString(), offsetBytes, lengthBytes);
  }

  /**
   * Returns a formatted hex string of an area of this object.
   * Used primarily for testing.
   * @param state the BaseStateImpl
   * @param preamble a descriptive header
   * @param offsetBytes offset bytes relative to the MemoryImpl start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  static final String toHex(final BaseStateImpl state, final String preamble, final long offsetBytes,
      final int lengthBytes) {
    final long capacity = state.getCapacity();
    UnsafeUtil.checkBounds(offsetBytes, lengthBytes, capacity);
    final StringBuilder sb = new StringBuilder();
    final Object uObj = state.getUnsafeObject();
    final String uObjStr;
    final long uObjHeader;
    if (uObj == null) {
      uObjStr = "null";
      uObjHeader = 0;
    } else {
      uObjStr =  uObj.getClass().getSimpleName() + ", " + (uObj.hashCode() & 0XFFFFFFFFL);
      uObjHeader = UnsafeUtil.getArrayBaseOffset(uObj.getClass());
    }
    final ByteBuffer bb = state.getByteBuffer();
    final String bbStr = bb == null ? "null"
            : bb.getClass().getSimpleName() + ", " + (bb.hashCode() & 0XFFFFFFFFL);
    final MemoryRequestServer memReqSvr = state.getMemoryRequestServer();
    final String memReqStr = memReqSvr != null
        ? memReqSvr.getClass().getSimpleName() + ", " + (memReqSvr.hashCode() & 0XFFFFFFFFL)
        : "null";
    final long cumBaseOffset = state.getCumulativeOffset();
    sb.append(preamble).append(LS);
    sb.append("UnsafeObj, hashCode : ").append(uObjStr).append(LS);
    sb.append("UnsafeObjHeader     : ").append(uObjHeader).append(LS);
    sb.append("ByteBuf, hashCode   : ").append(bbStr).append(LS);
    sb.append("RegionOffset        : ").append(state.getRegionOffset()).append(LS);
    sb.append("Capacity            : ").append(capacity).append(LS);
    sb.append("CumBaseOffset       : ").append(cumBaseOffset).append(LS);
    sb.append("MemReq, hashCode    : ").append(memReqStr).append(LS);
    sb.append("Valid               : ").append(state.isValid()).append(LS);
    sb.append("Read Only           : ").append(state.isReadOnly()).append(LS);
    sb.append("Type Byte Order     : ").append(state.getTypeByteOrder().toString()).append(LS);
    sb.append("Native Byte Order   : ").append(ByteOrder.nativeOrder().toString()).append(LS);
    sb.append("JDK Runtime Version : ").append(UnsafeUtil.JDK).append(LS);
    //Data detail
    sb.append("Data, littleEndian  :  0  1  2  3  4  5  6  7");

    for (long i = 0; i < lengthBytes; i++) {
      final int b = unsafe.getByte(uObj, cumBaseOffset + offsetBytes + i) & 0XFF;
      if (i % 8 == 0) { //row header
        sb.append(String.format("%n%20s: ", offsetBytes + i));
      }
      sb.append(String.format("%02x ", b));
    }
    sb.append(LS);

    return sb.toString();
  }

  //MONITORING

  /**
   * Gets the current number of active direct memory allocations.
   * @return the current number of active direct memory allocations.
   */
  public static final long getCurrentDirectMemoryAllocations() {
    return BaseStateImpl.currentDirectMemoryAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory allocated.
   * @return the current size of active direct memory allocated.
   */
  public static final long getCurrentDirectMemoryAllocated() {
    return BaseStateImpl.currentDirectMemoryAllocated_.get();
  }

  /**
   * Gets the current number of active direct memory map allocations.
   * @return the current number of active direct memory map allocations.
   */
  public static final long getCurrentDirectMemoryMapAllocations() {
    return BaseStateImpl.currentDirectMemoryMapAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory map allocated.
   * @return the current size of active direct memory map allocated.
   */
  public static final long getCurrentDirectMemoryMapAllocated() {
    return BaseStateImpl.currentDirectMemoryMapAllocated_.get();
  }

  //REACHABILITY FENCE
  static void reachabilityFence(@SuppressWarnings("unused") final Object obj) { }

}
