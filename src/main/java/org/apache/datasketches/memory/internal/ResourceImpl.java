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
import static org.apache.datasketches.memory.internal.Util.characterPad;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * Implements the root Resource methods plus some common static variables and check methods.
 *
 * @author Lee Rhodes
 */
abstract class ResourceImpl implements Resource {
  static final String JDK; //must be at least "17"
  static final int JDK_MAJOR; //8, 11, 17, etc

  //Used to convert "type" to bytes:  bytes = longs << LONG_SHIFT
  static final int BOOLEAN_SHIFT    = 0;
  static final int BYTE_SHIFT       = 0;
  static final long SHORT_SHIFT     = 1;
  static final long CHAR_SHIFT      = 1;
  static final long INT_SHIFT       = 2;
  static final long LONG_SHIFT      = 3;
  static final long FLOAT_SHIFT     = 2;
  static final long DOUBLE_SHIFT    = 3;

  //class type IDs. Do not change the bit orders
  //The lowest 3 bits are set dynamically
  // 0000 0XXX Group 1
  static final int WRITABLE  = 0; //bit 0 = 0
  static final int READONLY  = 1; //bit 0
  static final int REGION    = 2; //bit 1
  static final int DUPLICATE = 4; //bit 2, for Buffer only

  // 000X X000 Group 2                         43
  static final int HEAP   = 0;    //bits 3,4 = 00
  static final int DIRECT = 8;    //bit 3    = 01
  static final int MAP    = 16;   //bit 4,   = 10 Map is effectively Direct

  // 00X0 0000 Group 3 ByteOrder
  static final int NATIVE_BO    = 0; //bit 5 = 0
  static final int NONNATIVE_BO = 32;//bit 5

  // 0X00 0000 Group 4
  static final int MEMORY  = 0;   //bit 6 = 0
   static final int BUFFER = 64;  //bit 6

  // X000 0000 Group 5
  static final int BYTEBUF = 128; //bit 7,

  /**
   * The java line separator character as a String.
   */
  static final String LS = System.getProperty("line.separator");

  /**
   * The static final for <i>ByteOrder.nativeOrder()</i>.
   */
  static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

  /**
   * The static final for NON <i>ByteOrder.nativeOrder()</i>.
   */
  static final ByteOrder NON_NATIVE_BYTE_ORDER =
      (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

  static {
    final String jdkVer = System.getProperty("java.version");
    final int[] p = parseJavaVersion(jdkVer);
    JDK = p[0] + "." + p[1];
    JDK_MAJOR = (p[0] == 1) ? p[1] : p[0];
  }

  final MemorySegment seg;
  final int typeId;

  /**
   * Root constructor.
   * @param seg the given, one and only one MemorySegment
   * @param typeId the given typeId
   * @param memReqSvr the given MemoryRequestServer, or null.
   */
  ResourceImpl(final MemorySegment seg, final int typeId, final MemoryRequestServer memReqSvr) {
    this.seg = seg;
    this.typeId = typeId;
    this.memReqSvr = memReqSvr;
  }

  //MemoryReqestServer logic

  /**
   * User specified MemoryRequestServer. Set here and by leaf nodes.
   */
  MemoryRequestServer memReqSvr = null;

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    final int mask = READONLY | MAP ;
    final int test = 0; //!READONLY & !MAP
    return (typeId & mask) != test ? null : memReqSvr;
  }

  @Override
  public boolean hasMemoryRequestServer() {
    return memReqSvr != null;
  }

  @Override
  public void setMemoryRequestServer(final MemoryRequestServer memReqSvr) {
    this.memReqSvr = memReqSvr;
  }

  //***

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

  private static String pad(final String s, final int fieldLen) {
    return characterPad(s, fieldLen, ' ' , true);
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
      throw new IllegalArgumentException("Improper Java -version string: " + jdkVer + LS + e);
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
    switch (group1) { // 0000 0XXX
      case 0 : sb.append(pad("Writable + ",32)); break;
      case 1 : sb.append(pad("ReadOnly + ",32)); break;
      case 2 : sb.append(pad("Writable + Region + ",32)); break;
      case 3 : sb.append(pad("ReadOnly + Region + ",32)); break;
      case 4 : sb.append(pad("Writable + Duplicate + ",32)); break;
      case 5 : sb.append(pad("ReadOnly + Duplicate + ",32)); break;
      case 6 : sb.append(pad("Writable + Region + Duplicate + ",32)); break;
      case 7 : sb.append(pad("ReadOnly + Region + Duplicate + ",32)); break;
      default: break;
    }
    final int group2 = (typeId >>> 3) & 0x3;
    switch (group2) { // 000X X000                            43
      case 0 : sb.append(pad("Heap + ",15)); break;   //      00
      case 1 : sb.append(pad("Direct + ",15)); break; //      01
      case 2 : sb.append(pad("Map + Direct + ",15)); break; //10
      case 3 : sb.append(pad("Map + Direct + ",15)); break; //11
      default: break;
    }
    final int group3 = (typeId >>> 5) & 0x1;
    switch (group3) { // 00X0 0000
      case 0 : sb.append(pad("NativeOrder + ",17)); break;
      case 1 : sb.append(pad("NonNativeOrder + ",17)); break;
      default: break;
    }
    final int group4 = (typeId >>> 6) & 0x1;
    switch (group4) { // 0X00 0000
      case 0 : sb.append(pad("Memory + ",9)); break;
      case 1 : sb.append(pad("Buffer + ",9)); break;
      default: break;
    }
    final int group5 = (typeId >>> 7) & 0x1;
    switch (group5) { // X000 0000
      case 0 : sb.append(pad("",10)); break;
      case 1 : sb.append(pad("ByteBuffer",10)); break;
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
   * @param resourceImpl the ResourceImpl
   * @param comment optional unique description
   * @param offsetBytes offset bytes relative to the MemoryImpl start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  static final String toHex(final ResourceImpl resourceImpl, final String comment, final long offsetBytes,
      final int lengthBytes, final boolean withData) {
    final MemorySegment seg = resourceImpl.seg;
    final long capacity = seg.byteSize();
    checkBounds(offsetBytes, lengthBytes, capacity);
    final StringBuilder sb = new StringBuilder();
    final String theComment = (comment != null) ? comment : "";
    final String addHCStr = "" + Integer.toHexString(seg.address().hashCode());
    final MemoryRequestServer memReqSvr = resourceImpl.getMemoryRequestServer();
    final String memReqStr = memReqSvr != null
        ? memReqSvr.getClass().getSimpleName() + ", " + Integer.toHexString(memReqSvr.hashCode())
        : "null";

    sb.append(LS + "### DataSketches Memory Component SUMMARY ###").append(LS);
    sb.append("Optional Comment       : ").append(theComment).append(LS);
    sb.append("TypeId String          : ").append(typeDecode(resourceImpl.typeId)).append(LS);
    sb.append("OffsetBytes            : ").append(offsetBytes).append(LS);
    sb.append("LengthBytes            : ").append(lengthBytes).append(LS);
    sb.append("Capacity               : ").append(capacity).append(LS);
    sb.append("MemoryAddress hashCode : ").append(addHCStr).append(LS);
    sb.append("MemReqSvr, hashCode    : ").append(memReqStr).append(LS);
    sb.append("Read Only              : ").append(resourceImpl.isReadOnly()).append(LS);
    sb.append("Type Byte Order        : ").append(resourceImpl.getTypeByteOrder().toString()).append(LS);
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
    final ByteBuffer byteBuffer = seg.asByteBuffer().order(order);
    return byteBuffer;
  }

  @Override
  public void close() {
    seg.scope().close(); //not idempotent
  }

  @Override
  public final boolean equalTo(final long thisOffsetBytes, final Resource that,
      final long thatOffsetBytes, final long lengthBytes) {
    if (that == null) { return false; }
    return CompareAndCopy.equals(seg, thisOffsetBytes, ((ResourceImpl) that).seg, thatOffsetBytes, lengthBytes);
  }

  @Override
  public void force() { seg.force(); }

  @Override
  public final long getCapacity() {
    return seg.byteSize();
  }

  @Override
  public Thread getOwnerThread() {
    return seg.scope().ownerThread();
  }

  @Override
  public final long getRelativeOffset(final Resource that) {
    final ResourceImpl that2 = (ResourceImpl) that;
    return this.seg.address().segmentOffset(that2.seg);
  }

  @Override
  public final ByteOrder getTypeByteOrder() {
    return (typeId & NONNATIVE_BO) > 0 ? NON_NATIVE_BYTE_ORDER : NATIVE_BYTE_ORDER;
  }

  @Override
  public final boolean hasByteBuffer() {
    return (typeId & BYTEBUF) > 0;
  }

  //@SuppressWarnings("resource")
  @Override
  public boolean isAlive() { return seg.scope().isAlive(); }

  @Override
  public final boolean isByteOrderCompatible(final ByteOrder byteOrder) {
    final ByteOrder typeBO = getTypeByteOrder();
    return typeBO == ByteOrder.nativeOrder() && typeBO == byteOrder;
  }

  @Override
  public final boolean isBuffer() {
    return (typeId & BUFFER) > 0;
  }

  @Override
  public boolean isCloseable() {
    return ((seg.isNative() || seg.isMapped()) && seg.scope().isAlive() && !seg.scope().isImplicit());
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
  public final boolean isSameResource(final Resource that) {
    final ResourceImpl that2 = (ResourceImpl) that;
    return this.seg.address().equals(that2.seg.address());
  }

  @Override
  public void load() { seg.load(); }

  @Override
  public long mismatch(final Resource that) {
    Objects.requireNonNull(that);
    if (!that.isAlive()) { throw new IllegalArgumentException("Given argument is not alive."); }
    final ResourceImpl thatBSI = (ResourceImpl) that;
    return seg.mismatch(thatBSI.seg);
  }

  @Override
  public final long nativeOverlap(final Resource that) {
    if (that == null) { return 0; }
    if (!that.isAlive()) { return 0; }
    final ResourceImpl thatBSI = (ResourceImpl) that;
    if (this == thatBSI) { return seg.byteSize(); }
    return nativeOverlap(seg, thatBSI.seg);
  }

  static final long nativeOverlap(final MemorySegment segA, final MemorySegment segB) { //used in test
    if (!segA.isNative() || !segB.isNative()) { return 0; } //both segments must be native
    //Assume that memory addresses increase from left to right.
    //Identify the left and right edges of two regions, A and B in memory.
    final long bytesA = segA.byteSize();
    final long bytesB = segB.byteSize();
    final long lA = segA.address().toRawLongValue(); //left A
    final long lB = segB.address().toRawLongValue(); //left B
    final long rA = lA + bytesA; //right A
    final long rB = lB + bytesB; //right B
    if ((rA <= lB) || (rB <= lA)) { return 0; } //Eliminate the totally disjoint case:

    final long result = (bytesA == bytesB) //Two major cases: equal and not equal in size
        ? nativeOverlapEqualSizes(lA, rA, lB, rB)
        : nativeOverlapNotEqualSizes(lA, rA, lB, rB);

    return (lB < lA) ? -result : result; //if lB is lower in memory than lA, we return a negative result
  }

  private static final long nativeOverlapEqualSizes(final long lA, final long rA, final long lB, final long rB) {
    if (lA == lB) { return rA - lA; } //Exact overlap, return either size
    return (lA < lB)
        ? rA - lB  //Partial overlap on right portion of A
        : rB - lA; //else partial overlap on left portion of A
  }

  private static final long nativeOverlapNotEqualSizes(final long lA, final long rA, final long lB, final long rB) {
    return (rB - lB < rA - lA) //whichever is larger we assign to parameters 1 and 2
        ? biggerSmaller(lA, rA, lB, rB)  //A bigger than B
        : biggerSmaller(lB, rB, lA, rA); //B bigger than A, reverse parameters
  }

  private static final long biggerSmaller(final long lLarge, final long rLarge, final long lSmall, final long rSmall) {
    if ((rSmall <= rLarge) && (lLarge <= lSmall)) { return rSmall - lSmall; } //Small is totally within Large
    return (rLarge < rSmall)
        ? rLarge - lSmall  //Partial overlap on right portion of Large
        : rSmall - lLarge; //Partial overlap on left portion of Large
  }

  @Override
  public ResourceScope scope() { return seg.scope(); }

  @Override
  public ByteBuffer toByteBuffer(final ByteOrder order) {
    Objects.requireNonNull(order, "The input ByteOrder must not be null");
    return ByteBuffer.wrap(seg.toByteArray());
  }

  @Override
  public String toString() {
    return toString("", 0, (int)this.getCapacity(), false);
  }

  @Override
  public final String toString(final String comment, final long offsetBytes, final int lengthBytes,
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