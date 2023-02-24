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

  static final String NOT_MAPPED_FILE_RESOURCE = "This is not a memory-mapped file resource";
  static final String THREAD_EXCEPTION_TEXT = "Attempted access outside owning thread";

  static {
    final String jdkVer = System.getProperty("java.version");
    final int[] p = parseJavaVersion(jdkVer);
    JDK = p[0] + "." + p[1];
    JDK_MAJOR = (p[0] == 1) ? p[1] : p[0];
  }

  MemoryRequestServer memReqSvr = null; //set by the user.

  Thread owner = null; //set by the leaf nodes.

  /**
   * The root of the Memory inheritance hierarchy
   */
  ResourceImpl() { }

  /**
   * Check the requested offset and length against the allocated size.
   * The invariants equation is: {@code 0 <= reqOff <= reqLen <= reqOff + reqLen <= allocSize}.
   * If this equation is violated an {@link MemoryBoundsException} will be thrown.
   * @param reqOff the requested offset
   * @param reqLen the requested length
   * @param allocSize the allocated size.
   * @Throws MemoryBoundsException if the given arguments constitute a violation
   * of the invariants equation expressed above.
   */
  public static void checkBounds(final long reqOff, final long reqLen, final long allocSize) {
    if ((reqOff | reqLen | (reqOff + reqLen) | (allocSize - (reqOff + reqLen))) < 0) {
      throw new MemoryBoundsException(
          "reqOffset: " + reqOff + ", reqLength: " + reqLen
              + ", (reqOff + reqLen): " + (reqOff + reqLen) + ", allocSize: " + allocSize);
    }
  }

  static void checkJavaVersion(final String jdkVer, final int p0, final int p1 ) {
    final boolean ok = ((p0 == 1) && (p1 == 8)) || (p0 == 8) || (p0 == 11) || (p0 == 17);
    if (!ok) { throw new IllegalArgumentException(
        "Unsupported JDK Major Version. It must be one of 1.8, 8, 11, 17: " + jdkVer);
    }
  }

  void checkNotReadOnly() {
    if (isReadOnly()) {
      throw new ReadOnlyException("Cannot write to a read-only Resource.");
    }
  }

  /**
   * This checks that the current thread is the same as the given owner thread.
   * @Throws IllegalStateException if it is not.
   * @param owner the given owner thread.
   */
  static final void checkThread(final Thread owner) {
    if (owner != Thread.currentThread()) {
      throw new IllegalStateException(THREAD_EXCEPTION_TEXT);
    }
  }

  /**
   * @throws IllegalStateException if this Resource is AutoCloseable, and already closed, i.e., not <em>valid</em>.
   */
  void checkValid() {
    if (!isValid()) {
      throw new IllegalStateException("this Resource is AutoCloseable, and already closed, i.e., not <em>valid</em>.");
    }
  }

  /**
   * Checks that this resource is still valid and throws a MemoryInvalidException if it is not.
   * Checks that the specified range of bytes is within bounds of this resource, throws
   * {@link MemoryBoundsException} if it's not: i. e. if offsetBytes &lt; 0, or length &lt; 0,
   * or offsetBytes + length &gt; {@link #getCapacity()}.
   * @param offsetBytes the given offset in bytes of this object
   * @param lengthBytes the given length in bytes of this object
   * @Throws MemoryInvalidException if this resource is AutoCloseable and is no longer valid, i.e.,
   * it has already been closed.
   * @Throws MemoryBoundsException if this resource violates the memory bounds of this resource.
   */
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, getCapacity());
  }

  /**
   * Checks that this resource is still valid and throws a MemoryInvalidException if it is not.
   * Checks that the specified range of bytes is within bounds of this resource, throws
   * {@link MemoryBoundsException} if it's not: i. e. if offsetBytes &lt; 0, or length &lt; 0,
   * or offsetBytes + length &gt; {@link #getCapacity()}.
   * Checks that this operation is a read-only operation and throws a ReadOnlyException if not.
   * @param offsetBytes the given offset in bytes of this object
   * @param lengthBytes the given length in bytes of this object
   * @Throws MemoryInvalidException if this resource is AutoCloseable and is no longer valid, i.e.,
   * it has already been closed.
   * @Throws MemoryBoundsException if this resource violates the memory bounds of this resource.
   * @Throws ReadOnlyException if the associated operation is not a Read-only operation.
   */
  final void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, getCapacity());
    if (isReadOnly()) {
      throw new ReadOnlyException("Memory is read-only.");
    }
  }

  @Override
  public void close() {
    /* Overridden by the actual AutoCloseable leaf sub-classes. */
    throw new UnsupportedOperationException("This resource is not AutoCloseable.");
  }

  @Override
  public final boolean equalTo(final long thisOffsetBytes, final Resource that,
      final long thatOffsetBytes, final long lengthBytes) {
    if (that == null) { return false; }
    return CompareAndCopy.equals(this, thisOffsetBytes, (ResourceImpl) that, thatOffsetBytes, lengthBytes);
  }

  @Override
  public void force() { //overridden by Map Leaves
    throw new UnsupportedOperationException(NOT_MAPPED_FILE_RESOURCE);
  }

  //Overridden by ByteBuffer Leaves. Used internally and for tests.
  ByteBuffer getByteBuffer() {
    return null;
  }

  @Override
  public final ByteOrder getByteOrder() {
    return isNativeOrder(getTypeId()) ? NATIVE_BYTE_ORDER : NON_NATIVE_BYTE_ORDER;
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
  public MemoryRequestServer getMemoryRequestServer() { return memReqSvr; }

  //Overridden by ByteBuffer, Direct and Map Leaves
  abstract long getNativeBaseOffset();

  //Overridden by all leaves
  abstract int getTypeId();

  //Overridden by Heap and ByteBuffer leaves. Made public as getArray() in WritableMemoryImpl and
  // WritableBufferImpl
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public boolean isByteBufferResource() {
    return (getTypeId() & BYTEBUF) > 0;
  }

  @Override
  public final boolean isByteOrderCompatible(final ByteOrder byteOrder) {
    final ByteOrder typeBO = getByteOrder();
    return typeBO == ByteOrder.nativeOrder() && typeBO == byteOrder;
  }

  final boolean isBufferApi(final int typeId) {
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

  @Override
  public final boolean isHeapResource() {
    checkValid();
    return getUnsafeObject() != null;
  }

  @Override
  public boolean isLoaded() { //overridden by Map Leaves
    throw new IllegalStateException(NOT_MAPPED_FILE_RESOURCE);
  }

  @Override
  public boolean isMemoryMappedResource() {
    return (getTypeId() & MAP) > 0;
  }

  @Override
  public boolean isMemoryApi() {
    return (getTypeId() & BUFFER) == 0;
  }

  final boolean isNativeOrder(final int typeId) { //not used
    return (typeId & NONNATIVE) == 0;
  }

  @Override
  public boolean isNonNativeOrder() {
    return (getTypeId() & NONNATIVE) > 0;
  }

  @Override
  public final boolean isReadOnly() {
    checkValid();
    return (getTypeId() & READONLY) > 0;
  }

  @Override
  public boolean isRegionView() {
    return (getTypeId() & REGION) > 0;
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

  //Overridden by Direct and Map leaves
  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void load() { //overridden by Map leaves
    throw new IllegalStateException(NOT_MAPPED_FILE_RESOURCE);
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

  final static int setReadOnlyBit(final int typeId, final boolean readOnly) {
    return readOnly ? typeId | READONLY : typeId & ~READONLY;
  }

  @Override
  public void setMemoryRequestServer(final MemoryRequestServer memReqSvr) { this.memReqSvr = memReqSvr; }

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
