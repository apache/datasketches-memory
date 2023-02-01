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
import static org.apache.datasketches.memory.internal.Util.characterPad;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryBoundsException;
import org.apache.datasketches.memory.MemoryInvalidException;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.ReadOnlyException;
import org.apache.datasketches.memory.Resource;

/**
 * Implements the root Resource methods.
 *
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
public abstract class ResourceImpl implements Resource {
  static final String JDK; //must be at least "1.8"
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
  static final int WRITABLE = 0;
  static final int READONLY = 1;
  static final int REGION = 1 << 1;
  static final int DUPLICATE = 1 << 2; //for Buffer only

  // 000X X000 Group 2
  static final int HEAP = 0;
  static final int DIRECT = 1 << 3;
  static final int MAP = 1 << 4; //Map is always Direct also

  // 00X0 0000 Group 3
  static final int NATIVE = 0;
  static final int NONNATIVE = 1 << 5;

  // 0X00 0000 Group 4
  static final int MEMORY = 0;
  static final int BUFFER = 1 << 6;

  // X000 0000 Group 5
  static final int BYTEBUF = 1 << 7;

  /**
   * The java line separator character as a String.
   */
  public static final String LS = System.getProperty("line.separator");

  public static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

  public static final ByteOrder NON_NATIVE_BYTE_ORDER =
      (NATIVE_BYTE_ORDER == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

  static {
    final String jdkVer = System.getProperty("java.version");
    final int[] p = parseJavaVersion(jdkVer);
    JDK = p[0] + "." + p[1];
    JDK_MAJOR = (p[0] == 1) ? p[1] : p[0];
  }

  MemoryRequestServer memReqSvr = null; //selected by the user

  /**
   * The root of the Memory inheritance hierarchy
   */
  ResourceImpl() { }

  /**
   * Check the requested offset and length against the allocated size.
   * The invariants equation is: {@code 0 <= reqOff <= reqLen <= reqOff + reqLen <= allocSize}.
   * If this equation is violated an {@link IllegalArgumentException} will be thrown.
   * @param reqOff the requested offset
   * @param reqLen the requested length
   * @param allocSize the allocated size.
   */
  public static void checkBounds(final long reqOff, final long reqLen, final long allocSize) {
    if ((reqOff | reqLen | (reqOff + reqLen) | (allocSize - (reqOff + reqLen))) < 0) {
      throw new MemoryBoundsException(
          "reqOffset: " + reqOff + ", reqLength: " + reqLen
              + ", (reqOff + reqLen): " + (reqOff + reqLen) + ", allocSize: " + allocSize);
    }
  }

  static void checkJavaVersion(final String jdkVer, final int p0, final int p1 ) {
    final boolean ok = ((p0 == 1) && (p1 == 8)) || (p0 == 8) || (p0 == 11);
    if (!ok) { throw new IllegalArgumentException(
        "Unsupported JDK Major Version. It must be one of 1.8, 8, 11: " + jdkVer);
    }
  }

  public static final void checkThread(final Thread owner) {
    if (owner != Thread.currentThread()) {
      throw new IllegalStateException("Attempted access outside owning thread");
    }
  }

  void checkValid() {
    if (!isValid()) {
      throw new MemoryInvalidException();
    }
  }

  @Override
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    ResourceImpl.checkBounds(offsetBytes, lengthBytes, getCapacity());
  }

  final void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    ResourceImpl.checkBounds(offsetBytes, lengthBytes, getCapacity());
    if (isReadOnly()) {
      throw new ReadOnlyException("Memory is read-only.");
    }
  }

  @Override
  public final boolean equalTo(final long thisOffsetBytes, final Resource that,
      final long thatOffsetBytes, final long lengthBytes) {
    if (that == null) { return false; }
    return CompareAndCopy.equals(this, thisOffsetBytes, (ResourceImpl) that, thatOffsetBytes, lengthBytes);
  }

  @Override
  public void force() { //overridden by Map Leaves
    throw new IllegalStateException("This resource is not a memory-mapped file type.");
  }

  //Overridden by ByteBuffer Leafs
  ByteBuffer getByteBuffer() {
    return null;
  }

  @Override
  public final ByteOrder getByteOrder() {
    return isNonNativeType(getTypeId()) ? Util.NON_NATIVE_BYTE_ORDER : ByteOrder.nativeOrder();
  }

  /**
   * Gets the cumulative offset in bytes of this object from the backing resource.
   * This offset may also include other offset components such as the native off-heap
   * memory address, DirectByteBuffer split offsets, region offsets, and unsafe arrayBaseOffsets.
   *
   * @return the cumulative offset in bytes of this object from the backing resource.
   */
  abstract long getCumulativeOffset();

  /**
   * Gets the cumulative offset in bytes of this object from the backing resource including the given
   * localOffsetBytes. This offset may also include other offset components such as the native off-heap
   * memory address, DirectByteBuffer split offsets, region offsets, and object arrayBaseOffsets.
   *
   * @param localOffsetBytes offset to be added to the cumulative offset.
   * @return the cumulative offset in bytes of this object from the backing resource including the
   * given offsetBytes.
   */
  public long getCumulativeOffset(final long localOffsetBytes) {
    return getCumulativeOffset() + localOffsetBytes;
  }

  //Documented in WritableMemory and WritableBuffer interfaces.
  //Implemented in the Leaf nodes; Required here by toHex(...).
  @Override
  public abstract MemoryRequestServer getMemoryRequestServer();

  //Overridden by ByteBuffer, Direct and Map Leaves
  abstract long getNativeBaseOffset();

  //Overridden by all leafs
  abstract int getTypeId();

  //Overridden by Heap and ByteBuffer Leafs. Made public as getArray() in WritableMemoryImpl and
  // WritableBufferImpl
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public boolean isByteBufferResource() {
    return (getTypeId() & BYTEBUF) > 0;
  }

  final boolean isByteBufferType(final int typeId) {
    return (typeId & BYTEBUF) > 0;
  }

  @Override
  public final boolean isByteOrderCompatible(final ByteOrder byteOrder) {
    final ByteOrder typeBO = getByteOrder();
    return typeBO == ByteOrder.nativeOrder() && typeBO == byteOrder;
  }

  final boolean isBufferType(final int typeId) {
    return (typeId & BUFFER) > 0;
  }

  @Override
  public final boolean isDirectResource() {
    return getUnsafeObject() == null;
  }

  @Override
  public boolean isDuplicateBufferView() {
    return (getTypeId() & DUPLICATE) > 0;
  }

  final boolean isDuplicateType(final int typeId) {
    return (typeId & DUPLICATE) > 0;
  }

  @Override
  public final boolean isHeapResource() {
    checkValid();
    return getUnsafeObject() != null;
  }

  final boolean isHeapType(final int typeId) {
    return (typeId & (MAP | DIRECT)) == 0;
  }

  @Override
  public boolean isLoaded() { //overridden by Map Leaves
    throw new IllegalStateException("This resource is not a memory-mapped file type.");
  }

  @Override
  public boolean isMemoryMappedResource() {
    return (getTypeId() & MAP) > 0;
  }

  final boolean isMapType(final int typeId) { //not used
    return (typeId & MAP) > 0;
  }

  @Override
  public boolean isMemoryApi() {
    return (getTypeId() & BUFFER) == 0;
  }

  final boolean isMemoryType(final int typeId) { //not used
    return (typeId & BUFFER) == 0;
  }

  final boolean isNativeType(final int typeId) { //not used
    return (typeId & NONNATIVE) == 0;
  }

  @Override
  public boolean isNonNativeOrder() {
    return (getTypeId() & NONNATIVE) > 0;
  }

  final boolean isNonNativeType(final int typeId) {
    return (typeId & NONNATIVE) > 0;
  }

  @Override
  public final boolean isReadOnly() {
    checkValid();
    return isReadOnlyType(getTypeId());
  }

  final boolean isReadOnlyType(final int typeId) {
    return (typeId & READONLY) > 0;
  }

  @Override
  public boolean isRegionView() {
    return (getTypeId() & REGION) > 0;
  }

  final boolean isRegionType(final int typeId) {
    return (typeId & REGION) > 0;
  }

  @Override
  public boolean isSameResource(final Resource that) {
    checkValid();
    if (that == null) { return false; }
    final ResourceImpl that1 = (ResourceImpl) that;
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

  final boolean isWritableType(final int typeId) { //not used
    return (typeId & READONLY) == 0;
  }

  @Override
  public void load() { //overridden by Map leafs
    throw new IllegalStateException("This resource is not a memory-mapped file type.");
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
      String[] parts = jdkVer.trim().split("^0-9\\.");//grab only number groups and "."
      parts = parts[0].split("\\."); //split out the number groups
      p0 = Integer.parseInt(parts[0]); //the first number group
      p1 = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0; //2nd number group, or 0
    } catch (final NumberFormatException | ArrayIndexOutOfBoundsException  e) {
      throw new IllegalArgumentException("Improper Java -version string: " + jdkVer + "\n" + e);
    }
    checkJavaVersion(jdkVer, p0, p1);
    return new int[] {p0, p1};
  }

  //REACHABILITY FENCE
  static void reachabilityFence(final Object obj) { }

  final static int removeNnBuf(final int typeId) { return typeId & ~NONNATIVE & ~BUFFER; }

  @Override
  public void setMemoryRequestServer(final MemoryRequestServer memReqSvr) {
    this.memReqSvr = memReqSvr;
  }

  final static int setReadOnlyType(final int typeId, final boolean readOnly) {
    return readOnly ? typeId | READONLY : typeId & ~READONLY;
  }

  /**
   * Returns a formatted hex string of an area of this object.
   * Used primarily for testing.
   * @param state the ResourceImpl
   * @param preamble a descriptive header
   * @param offsetBytes offset bytes relative to the MemoryImpl start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  static final String toHex(final ResourceImpl state, final String preamble, final long offsetBytes,
      final int lengthBytes) {
    final long capacity = state.getCapacity();
    ResourceImpl.checkBounds(offsetBytes, lengthBytes, capacity);
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
    sb.append("RegionOffset        : ").append(state.getTotalOffset()).append(LS);
    sb.append("Capacity            : ").append(capacity).append(LS);
    sb.append("CumBaseOffset       : ").append(cumBaseOffset).append(LS);
    sb.append("MemReq, hashCode    : ").append(memReqStr).append(LS);
    sb.append("Valid               : ").append(state.isValid()).append(LS);
    sb.append("Read Only           : ").append(state.isReadOnly()).append(LS);
    sb.append("Type Byte Order     : ").append(state.getByteOrder().toString()).append(LS);
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
    switch (group2) { // 000X X000
      case 0 : sb.append(pad("Heap + ",15)); break;
      case 1 : sb.append(pad("Direct + ",15)); break;
      case 2 : sb.append(pad("Map + Direct + ",15)); break;
      case 3 : sb.append(pad("Map + Direct + ",15)); break;
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

  @Override
  public final long xxHash64(final long offsetBytes, final long lengthBytes, final long seed) {
    checkValid();
    return XxHash64.hash(getUnsafeObject(), getCumulativeOffset(0) + offsetBytes, lengthBytes, seed);
  }

  @Override
  public final long xxHash64(final long in, final long seed) {
    return XxHash64.hash(in, seed);
  }

}
