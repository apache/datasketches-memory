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
  static final int WRITABLE = 0;
  static final int READONLY = 1;
  static final int REGION = 2;
  static final int DUPLICATE = 4;

  //The following 4 bits are set by the 16 leaf nodes
  // 000X X000
  static final int HEAP = 0;
  static final int DIRECT = 1 << 3;
  static final int MAP = 2 << 3;

  // 00X0 0000
  static final int NATIVE = 0;
  static final int NONNATIVE = 1 << 5;

  // 0X00 0000
  static final int MEMORY = 0;
  static final int BUFFER = 1 << 6;

  // X000 0000
  static final int BYTEBUF = 1 << 7;
  /**
   * The root of the Memory inheritance hierarchy
   */
  BaseStateImpl() { }

  final void assertValid() {
    assert isValid() : "MemoryImpl not valid.";
  }

  final void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    // capacityBytes_ is intentionally read directly instead of calling getCapacity()
    // because the later can make JVM to not inline the assert code path (and entirely remove it)
    // even though it does nothing in production code path.
    assertBounds(offsetBytes, lengthBytes, getCapacity());
  }

  final void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    // capacityBytes_ is intentionally read directly instead of calling getCapacity()
    // because the later can make JVM to not inline the assert code path (and entirely remove it)
    // even though it does nothing in production code path.
    assertBounds(offsetBytes, lengthBytes, getCapacity());
    assert !isReadOnly() : "MemoryImpl is read-only.";
  }

  void checkValid() {
    if (!isValid()) {
      throw new IllegalStateException("MemoryImpl not valid.");
    }
  }

  @Override
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    //read capacityBytes_ directly to eliminate extra checkValid() call
    checkBounds(offsetBytes, lengthBytes, getCapacity());
  }

  final void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    //read capacityBytes_ directly to eliminate extra checkValid() call
    checkBounds(offsetBytes, lengthBytes, getCapacity());
    if (isReadOnly()) {
      throw new ReadOnlyException("MemoryImpl is read-only.");
    }
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

  //MONITORING

  /**
   * Gets the current size of active direct memory allocated.
   * @return the current size of active direct memory allocated.
   */
  public static final long getCurrentDirectMemoryAllocated() {
    return BaseStateImpl.currentDirectMemoryAllocated_.get();
  }

  /**
   * Gets the current number of active direct memory allocations.
   * @return the current number of active direct memory allocations.
   */
  public static final long getCurrentDirectMemoryAllocations() {
    return BaseStateImpl.currentDirectMemoryAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory map allocated.
   * @return the current size of active direct memory map allocated.
   */
  public static final long getCurrentDirectMemoryMapAllocated() {
    return BaseStateImpl.currentDirectMemoryMapAllocated_.get();
  }

  /**
   * Gets the current number of active direct memory map allocations.
   * @return the current number of active direct memory map allocations.
   */
  public static final long getCurrentDirectMemoryMapAllocations() {
    return BaseStateImpl.currentDirectMemoryMapAllocations_.get();
  }
  //END monitoring

  //Documented in WritableMemory and WritableBuffer interfaces.
  //Implemented in the Leaf nodes; Required here by toHex(...).
  abstract MemoryRequestServer getMemoryRequestServer();

  //Overridden by ByteBuffer, Direct and Map leafs
  abstract long getNativeBaseOffset();

  abstract long getOffset();

  @Override
  public final ByteOrder getTypeByteOrder() {
    return isNonNativeType(getTypeId()) ? Util.NON_NATIVE_BYTE_ORDER : ByteOrder.nativeOrder();
  }

  //Overridden by all leafs
  abstract int getTypeId();

  //Overridden by Heap and ByteBuffer Leafs. Made public as getArray() in WritableMemoryImpl and
  // WritableBufferImpl
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public final boolean hasByteBuffer() {
    assertValid();
    return getByteBuffer() != null;
  }

  @Override
  public final boolean hasArray() {
    assertValid();
    return getUnsafeObject() != null;
  }

  @Override
  public final int hashCode() {
    return (int) xxHash64(0, getCapacity(), 0); //xxHash64() calls checkValid()
  }

  final boolean isByteBufferType(final int typeId) {
    return (typeId & BYTEBUF) > 0;
  }

  @Override
  public final boolean isByteOrderCompatible(final ByteOrder byteOrder) {
    final ByteOrder typeBO = getTypeByteOrder();
    return typeBO == ByteOrder.nativeOrder() && typeBO == byteOrder;
  }

  final boolean isBufferType(final int typeId) {
    return (typeId & BUFFER) > 0;
  }

  @Override
  public final boolean isDirect() {
    return getUnsafeObject() == null;
  }

  final boolean isDirectType(final int typeId) {
    return (typeId & (MAP | DIRECT)) > 0;
  }

  final boolean isDuplicateType(final int typeId) {
    return (typeId & DUPLICATE) > 0;
  }

  final boolean isHeapType(final int typeId) {
    return (typeId & (MAP | DIRECT)) == 0;
  }

  final boolean isMapType(final int typeId) { //not used
    return (typeId & MAP) > 0;
  }

  final boolean isMemoryType(final int typeId) { //not used
    return (typeId & BUFFER) == 0;
  }

  final boolean isNativeType(final int typeId) { //not used
    return (typeId & NONNATIVE) == 0;
  }

  final boolean isNonNativeType(final int typeId) {
    return (typeId & NONNATIVE) > 0;
  }

  @Override
  public final boolean isReadOnly() {
    assertValid();
    return isReadOnlyType(getTypeId());
  }

  final boolean isReadOnlyType(final int typeId) {
    return (typeId & READONLY) > 0;
  }

  final boolean isRegionType(final int typeId) {
    return (typeId & REGION) > 0;
  }

  final boolean isWritableType(final int typeId) { //not used
    return (typeId & READONLY) == 0;
  }

  final static int removeNnBuf(final int typeId) { return typeId & ~NONNATIVE & ~BUFFER; }

  final static int setReadOnlyType(final int typeId, final boolean readOnly) {
    return readOnly ? typeId | READONLY : typeId & ~READONLY;
  }

  final

  @Override
  public boolean isSameResource(final Object that) {
    checkValid();
    if (that == null) { return false; }
    final BaseStateImpl that1 = (BaseStateImpl) that;
    that1.checkValid();
    if (this == that1) { return true; }

    return getCumulativeOffset(0) == that1.getCumulativeOffset(0)
            && getCapacity() == that1.getCapacity()
            && getUnsafeObject() == that1.getUnsafeObject()
            && getByteBuffer() == that1.getByteBuffer();
  }

  //Overridden by Direct and Map leafs
  @Override
  public boolean isValid() {
    return true;
  }

  //REACHABILITY FENCE
  static void reachabilityFence(final Object obj) { }

  //TO STRING

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
    final long cumBaseOffset = state.getCumulativeOffset(0);
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
   * Decodes the resource type. This is primarily for debugging.
   * @param typeId the given typeId
   * @return a human readable string.
   */
  public static final String typeDecode(final int typeId) {
    final StringBuilder sb = new StringBuilder();
    sb.append(typeId + ": ");
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
      case 3 : sb.append("Map Direct, "); break;
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
      case 0 : sb.append("Memory, "); break;
      case 1 : sb.append("Buffer, "); break;
      default: break;
    }
    final int group5 = (typeId >>> 7) & 0x1;
    switch (group5) {
      case 1 : sb.append("ByteBuffer"); break;
    }
    return sb.toString();
  }

  @Override
  public final long xxHash64(final long offsetBytes, final long lengthBytes, final long seed) {
    checkValid();
    return XxHash64.hash(getUnsafeObject(), getCumulativeOffset(0) + offsetBytes, lengthBytes, seed);
  }

  @Override
  public final long xxHash64(final long in, final long seed) {
    return XxHash64.hash(in, seed);
  }

//@Override
//public final long getCumulativeOffset(final long offsetBytes) {
//  assertValid();
//  return cumBaseOffset_ + offsetBytes;
//}

//@Override
//public final long getRegionOffset() {
//  final Object unsafeObj = getUnsafeObject();
//  final long nativeBaseOff = getNativeBaseOffset();
//  return unsafeObj == null
//      ? cumBaseOffset_ - nativeBaseOff
//      : cumBaseOffset_ - UnsafeUtil.getArrayBaseOffset(unsafeObj.getClass());
//}

//@Override
//public final long getRegionOffset(final long offsetBytes) {
//  return getRegionOffset() + offsetBytes;
//}

}
