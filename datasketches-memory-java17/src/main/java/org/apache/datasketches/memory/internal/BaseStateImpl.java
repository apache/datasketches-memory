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

import static jdk.incubator.foreign.MemoryAccess.getByteAtOffset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * Keeps key configuration state for MemoryImpl and BufferImpl plus some common static variables
 * and check methods.
 *
 * @author Lee Rhodes
 */
abstract class BaseStateImpl implements BaseState {
  static final String JDK; //must be at least "1.8"
  static final int JDK_MAJOR; //8, 11, 12, etc

  static final int BOOLEAN_SHIFT    = 0;
  static final int BYTE_SHIFT       = 0;
  static final long SHORT_SHIFT     = 1;
  static final long CHAR_SHIFT      = 1;
  static final long INT_SHIFT       = 2;
  static final long LONG_SHIFT      = 3;
  static final long FLOAT_SHIFT     = 2;
  static final long DOUBLE_SHIFT    = 3;

  //class type IDs.
  // 0000 0XXX
  static final int READONLY  = 1;
  static final int REGION    = 1 << 1;
  static final int DUPLICATE = 1 << 2; //for Buffer only

  // 000X X000
  static final int HEAP   = 0;
  static final int DIRECT = 1 << 3;
  static final int MAP    = 1 << 4; //Map is always Direct also

  // 00X0 0000
  static final int NATIVE    = 0;
  static final int NONNATIVE = 1 << 5;

  // 0X00 0000
  static final int MEMORY = 0;
   static final int BUFFER = 1 << 6;

  // X000 0000
  static final int BYTEBUF = 1 << 7;

  static {
    final String jdkVer = System.getProperty("java.version");
    final int[] p = parseJavaVersion(jdkVer);
    JDK = p[0] + "." + p[1];
    JDK_MAJOR = (p[0] == 1) ? p[1] : p[0];
  }

  final MemorySegment seg;
  final int typeId;

  MemoryRequestServer memReqSvr;

  BaseStateImpl(final MemorySegment seg, final int typeId, final MemoryRequestServer memReqSvr) {
    this.seg = seg;
    this.typeId = typeId;
    this.memReqSvr = memReqSvr;
  }

  /**
   * Check the requested offset and length against the allocated size.
   * The invariants equation is: {@code 0 <= reqOff <= reqLen <= reqOff + reqLen <= allocSize}.
   * If this equation is violated an {@link IllegalArgumentException} will be thrown.
   * @param reqOff the requested offset
   * @param reqLen the requested length
   * @param allocSize the allocated size.
   * @throws IllegalArgumentException for exceeding address bounds
   */
  static void checkBounds(final long reqOff, final long reqLen, final long allocSize) {
    if ((reqOff | reqLen | (reqOff + reqLen) | (allocSize - (reqOff + reqLen))) < 0) {
      throw new IllegalArgumentException(
          "reqOffset: " + reqOff + ", reqLength: " + reqLen
              + ", (reqOff + reqLen): " + (reqOff + reqLen) + ", allocSize: " + allocSize);
    }
  }

  static void checkJavaVersion(final String jdkVer, final int p0) {
    if ( p0 != 17 ) {
      throw new IllegalArgumentException(
          "Unsupported JDK Major Version, must be 17; " + jdkVer);
    }
  }

  /**
   * Returns first two number groups of the java version string.
   * @param jdkVer the java version string from System.getProperty("java.version").
   * @return first two number groups of the java version string.
   * @throws IllegalArgumentException for an improper Java version string.
   */
  static int[] parseJavaVersion(final String jdkVer) {
    final int p0, p1;
    try {
      String[] parts = jdkVer.trim().split("\\.");//grab only number groups and "."
      parts = parts[0].split("\\."); //split out the number groups
      p0 = Integer.parseInt(parts[0]); //the first number group
      p1 = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0; //2nd number group, or 0
    } catch (final NumberFormatException | ArrayIndexOutOfBoundsException  e) {
      throw new IllegalArgumentException("Improper Java -version string: " + jdkVer + "\n" + e);
    }
    checkJavaVersion(jdkVer, p0);
    return new int[] {p0, p1};
  }

  /**
   * Decodes the resource type. This is primarily for debugging.
   * @param typeId the given typeId
   * @return a human readable string.
   */
  static final String typeDecode(final int typeId) {
    final StringBuilder sb = new StringBuilder();
    final int group1 = typeId & 0x7;
    switch (group1) {
      case 0 : sb.append("Writable:\t"); break;
      case 1 : sb.append("ReadOnly:\t"); break;
      case 2 : sb.append("Writable:\tRegion:\t"); break;
      case 3 : sb.append("ReadOnly:\tRegion:\t"); break;
      case 4 : sb.append("Writable:\tDuplicate:\t"); break;
      case 5 : sb.append("ReadOnly:\tDuplicate:\t"); break;
      case 6 : sb.append("Writable:\tRegion:\tDuplicate:\t"); break;
      case 7 : sb.append("ReadOnly:\tRegion:\tDuplicate:\t"); break;
      default: break;
    }
    final int group2 = (typeId >>> 3) & 0x3;
    switch (group2) {
      case 0 : sb.append("Heap:\t"); break;
      case 1 : sb.append("Direct:\t"); break;
      case 2 : sb.append("Map:\t"); break;
      case 3 : sb.append("Direct:\tMap:\t"); break;
      default: break;
    }
    if ((typeId & BYTEBUF) > 0) { sb.append("ByteBuffer:\t"); }

    final int group3 = (typeId >>> 5) & 0x1;
    switch (group3) {
      case 0 : sb.append("NativeOrder:\t"); break;
      case 1 : sb.append("NonNativeOrder:\t"); break;
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

  static final WritableBuffer selectBuffer(
      final MemorySegment segment,
      final int type,
      final MemoryRequestServer memReqSvr,
      final boolean byteBufferType,
      final boolean mapType,
      final boolean nativeBOType) {
    final MemoryRequestServer memReqSvr2 = (byteBufferType || mapType) ? null : memReqSvr;
    final WritableBuffer wbuf;
    if (nativeBOType) {
      wbuf = new NativeWritableBufferImpl(segment, type, memReqSvr2);
    } else { //non-native BO
      wbuf = new NonNativeWritableBufferImpl(segment, type, memReqSvr2);
    }
    return wbuf;
  }

  static final WritableMemory selectMemory(
      final MemorySegment segment,
      final int type,
      final MemoryRequestServer memReqSvr,
      final boolean byteBufferType,
      final boolean mapType,
      final boolean nativeBOType) {
    final MemoryRequestServer memReqSvr2 = (byteBufferType || mapType) ? null : memReqSvr;
    final WritableMemory wmem;
    if (nativeBOType) {
      wmem = new NativeWritableMemoryImpl(segment, type, memReqSvr2);
    } else { //non-native BO
      wmem = new NonNativeWritableMemoryImpl(segment, type, memReqSvr2);
    }
    return wmem;
  }

  /**
   * Returns a formatted hex string of an area of this object.
   * Used primarily for testing.
   * @param state the BaseStateImpl
   * @param comment optional unique description
   * @param offsetBytes offset bytes relative to the MemoryImpl start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  static final String toHex(final BaseStateImpl state, final String comment, final long offsetBytes,
      final int lengthBytes, final boolean withData) {
    final MemorySegment seg = state.seg;
    final long capacity = seg.byteSize();
    checkBounds(offsetBytes, lengthBytes, capacity);
    final StringBuilder sb = new StringBuilder();
    final String theComment = (comment != null) ? comment : "";
    final String addHCStr = "" + Integer.toHexString(seg.address().hashCode());
    final MemoryRequestServer memReqSvr = state.getMemoryRequestServer();
    final String memReqStr = memReqSvr != null
        ? memReqSvr.getClass().getSimpleName() + ", " + Integer.toHexString(memReqSvr.hashCode())
        : "null";

    sb.append(LS + "### DataSketches Memory Component SUMMARY ###").append(LS);
    sb.append("Optional Comment       : ").append(theComment).append(LS);
    sb.append("TypeId String          : ").append(typeDecode(state.typeId)).append(LS);
    sb.append("OffsetBytes            : ").append(offsetBytes).append(LS);
    sb.append("LengthBytes            : ").append(lengthBytes).append(LS);
    sb.append("Capacity               : ").append(capacity).append(LS);
    sb.append("MemoryAddress hashCode : ").append(addHCStr).append(LS);
    sb.append("MemReqSvr, hashCode    : ").append(memReqStr).append(LS);
    sb.append("Read Only              : ").append(state.isReadOnly()).append(LS);
    sb.append("Type Byte Order        : ").append(state.getByteOrder().toString()).append(LS);
    sb.append("Native Byte Order      : ").append(ByteOrder.nativeOrder().toString()).append(LS);
    sb.append("JDK Runtime Version    : ").append(JDK).append(LS);
    //Data detail
    if (withData) {
      sb.append("Data, LittleEndian     :  0  1  2  3  4  5  6  7");
      for (long i = 0; i < lengthBytes; i++) {
        final int b = getByteAtOffset(seg, offsetBytes + i) & 0XFF;
        if (i % 8 == 0) { //row header
          sb.append(String.format("%n%23s: ", offsetBytes + i));
        }
        sb.append(String.format("%02x ", b));
      }
    }
    sb.append(LS + "### END SUMMARY ###");
    sb.append(LS);

    return sb.toString();
  }

  @Override
  public final ByteBuffer asByteBufferView(final ByteOrder order) {
    final ByteBuffer byteBuf = seg.asByteBuffer().order(order);
    return byteBuf;
  }

  @SuppressWarnings("resource")
  @Override
  public void close() {
    if (seg != null && seg.scope().isAlive() && !seg.scope().isImplicit()) {
      if (seg.isNative() || seg.isMapped()) {
        seg.scope().close();
      }
    }
  }

  @Override
  public final boolean equalTo(final long thisOffsetBytes, final Object that,
      final long thatOffsetBytes, final long lengthBytes) {
    return that instanceof BaseStateImpl
      ? CompareAndCopy.equals(seg, thisOffsetBytes, ((BaseStateImpl) that).seg, thatOffsetBytes, lengthBytes)
      : false;
  }

  @Override
  public void force() { seg.force(); }

  @Override
  public final long getCapacity() {
    return seg.byteSize();
  }

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    return memReqSvr;
  }

  @Override
  public final ByteOrder getByteOrder() {
    return (typeId & NONNATIVE) > 0 ? NON_NATIVE_BYTE_ORDER : ByteOrder.nativeOrder();
  }

  @Override
  public final boolean hasByteBuffer() {
    return (typeId & BYTEBUF) > 0;
  }

  @Override
  public boolean hasMemoryRequestServer() {
    return memReqSvr != null;
  }

  @SuppressWarnings("resource")
  @Override
  public boolean isAlive() { return seg.scope().isAlive(); }

  @Override
  public final boolean isByteOrderCompatible(final ByteOrder byteOrder) {
    final ByteOrder typeBO = getByteOrder();
    return typeBO == ByteOrder.nativeOrder() && typeBO == byteOrder;
  }

  @Override
  public final boolean isBuffer() {
    return (typeId & BUFFER) > 0;
  }

  @Override
  public final boolean isDirect() {
    assert seg.isNative() == (typeId & DIRECT) > 0;
    return seg.isNative();
  }

  @Override
  public final boolean isDuplicate() {
    return (typeId & DUPLICATE) > 0;
  }

  @Override
  public final boolean isHeap() {
    return !isDirect() && !isMapped();
  }

  @Override
  public boolean isLoaded() { return seg.isLoaded(); }

  @Override
  public boolean isMapped() {
    assert seg.isMapped() == (typeId & MAP) > 0;
    return seg.isMapped();
  }

  @Override
  public final boolean isMemory() {
    return (typeId & BUFFER) == 0;
  }

  @Override
  public final boolean isReadOnly() {
    assert seg.isReadOnly() == (typeId & READONLY) > 0;
    return seg.isReadOnly();
  }

  @Override
  public final boolean isRegion() {
    return (typeId & REGION) > 0;
  }

  @Override
  public final boolean isSameResource(final Object that) {
    if (this == that) { return true; }
    final MemoryAddress myAdd = seg.address();
    if (that instanceof BaseStateImpl) {
      final MemoryAddress thatAdd = ((BaseStateImpl)that).seg.address();
      return (myAdd.equals(thatAdd));
    }
    return false;
  }

  @Override
  public void load() { seg.load(); }

  @Override
  public ResourceScope scope() { return seg.scope(); }

  //  @Override
  //  public void setMemoryRequestServer(final MemoryRequestServer memReqSvr) {
  //    this.memReqSvr = memReqSvr;
  //  }

  @Override
  public ByteBuffer toByteBuffer(final ByteOrder order) {
    Objects.requireNonNull(order, "The input ByteOrder must not be null");
    return ByteBuffer.wrap(seg.toByteArray());
  }

  @Override
  public final String toHexString(final String comment, final long offsetBytes, final int lengthBytes,
      final boolean withData) {
    return toHex(this, comment, offsetBytes, lengthBytes, withData);
  }

  @Override
  public MemorySegment toMemorySegment() {
    final MemorySegment arrSeg = MemorySegment.ofArray(new byte[(int)seg.byteSize()]);
    arrSeg.copyFrom(seg);
    return arrSeg;
  }

  @Override
  public void unload() { seg.unload(); }

  @Override
  public final long xxHash64(final long in, final long seed) {
    return XxHash64.hash(in, seed);
  }

  @Override
  public final long xxHash64(final long offsetBytes, final long lengthBytes, final long seed) {
    return XxHash64.hash(seg, offsetBytes, lengthBytes, seed);
  }

}
