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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.internal.WritableMemoryImpl;

import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

/**
 * Defines the writable API for offset access to a resource.
 *
 * @author Lee Rhodes
 */
public interface WritableMemory extends Memory {

  //BYTE BUFFER
  /**
   * Provides a view of the given <i>ByteBuffer</i> for write operations.
   * The view is of the entire ByteBuffer independent of position and limit.
   * The returned <i>WritableMemory</i> will assume the <i>ByteOrder</i> of the given <i>ByteBuffer</i>.
   * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null and writable.
   * @return a new <i>WritableMemory</i> for write operations on the given <i>ByteBuffer</i>.
   */
  static WritableMemory writableWrap(ByteBuffer byteBuffer) {
    return writableWrap(byteBuffer, byteBuffer.order(), null);
  }

  /**
   * Provides a view of the given <i>ByteBuffer</i> for write operations.
   * The view is of the entire ByteBuffer independent of position and limit.
   * The returned <i>WritableMemory</i> will assume the given <i>ByteOrder</i>,
   * independent of the ByteOrder of the given ByteBuffer.
   * This does not affect the ByteOrder of data already in the ByteBuffer.
   * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null and writable.
   * @param byteOrder the byte order to be used. It must be non-null.
   * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which may be null.
   * This is a callback mechanism for a user client to request a larger <i>WritableMemory</i>.
   * @return a new <i>WritableMemory</i> for write operations on the given <i>ByteBuffer</i>.
   * @throws IllegalArgumentException if ByteBuffer is not writable.
   */
  static WritableMemory writableWrap(
      ByteBuffer byteBuffer,
      ByteOrder byteOrder,
      MemoryRequestServer memReqSvr) {
    return WritableMemoryImpl.wrapByteBuffer(byteBuffer, false, byteOrder, memReqSvr);
  }

  //Duplicates make no sense here

  //MAP
  /**
   * Maps the entire given file into native-ordered WritableMemory for write operations
   * Calling this method is equivalent to calling
   * {@link #writableMap(File, long, long, ByteOrder) writableMap(file, 0, file.length(), scope, ByteOrder.nativeOrder())}.
   * @param file the given file to map. It must be non-null and writable.
   * @return a file-mapped WritableMemory
   * @throws IllegalArgumentException if file is not readable or not writable.
   * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
   * @throws SecurityException If a security manager is installed and it denies an unspecified permission
   * required by the implementation.
   */
  static WritableMemory writableMap(File file) throws IOException {
    return writableMap(file, 0, file.length(), ByteOrder.nativeOrder());
  }

  /**
   * Maps the specified portion of the given file into Memory for write operations.
   * @param file the given file to map. It must be non-null and writable.
   * @param fileOffsetBytes the position in the given file in bytes. It must not be negative.
   * @param capacityBytes the size of the mapped Memory.
   * @param byteOrder the given <i>ByteOrder</i>. It must be non-null.
   * @return a file-mapped WritableMemory.
   * @throws IllegalArgumentException if file is not readable or not writable.
   * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
   * @throws SecurityException If a security manager is installed and it denies an unspecified permission
   * required by the implementation.
   */
  static WritableMemory writableMap(
      File file,
      long fileOffsetBytes,
      long capacityBytes,
      ByteOrder byteOrder) throws IOException {
    final ResourceScope scope = ResourceScope.newConfinedScope();
    return WritableMemoryImpl.wrapMap(file, fileOffsetBytes, capacityBytes, scope, false, byteOrder);
  }

  /**
   * Maps the specified portion of the given file into Memory for write operations with a ResourceScope.
   * @param file the given file to map. It must be non-null with a non-negative length and writable.
   * @param fileOffsetBytes the position in the given file in bytes. It must not be negative.
   * @param capacityBytes the size of the mapped Memory. It must be &ge; 0.
   * @param scope the given ResourceScope.
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @return mapped WritableMemory.
   * @throws IllegalArgumentException -- if file is not readable or writable.
   * @throws IllegalArgumentException -- if file is not writable.
   * @throws IOException - if the specified path does not point to an existing file, or if some other I/O error occurs.
   * @throws SecurityException - If a security manager is installed and it denies an unspecified permission
   * required by the implementation.
   */
  static WritableMemory writableMap(
      File file,
      long fileOffsetBytes,
      long capacityBytes,
      ResourceScope scope,
      ByteOrder byteOrder) throws IOException {
    return WritableMemoryImpl.wrapMap(file, fileOffsetBytes, capacityBytes, scope, false, byteOrder);
  }

  //ALLOCATE DIRECT

  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory.
   * Native byte order is assumed.
   * The allocated memory will be 8-byte aligned.
   *
   * <p><b>NOTE:</b> Native/Direct memory acquired may have garbage in it.
   * It is the responsibility of the using application to clear this memory, if required,
   * and to call <i>close()</i> when done.</p>
   *
   * @param capacityBytes the size of the desired memory in bytes.
   * @return WritableMemory for this off-heap, native resource.
   */
  static WritableMemory allocateDirect(long capacityBytes) {
    return allocateDirect(capacityBytes, 8, ByteOrder.nativeOrder(), new DefaultMemoryRequestServer());
  }

  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory.
   * The allocated memory will be aligned to the given <i>alignmentBytes</i>.
   *
   * <p><b>NOTE:</b> Native/Direct memory acquired may have garbage in it.
   * It is the responsibility of the using application to clear this memory, if required,
   * and to call <i>close()</i> when done.</p>
   *
   * @param capacityBytes the size of the desired memory in bytes.
   * @param alignmentBytes requested segment alignment. Typically 1, 2, 4 or 8.
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @param memReqSvr A user-specified MemoryRequestServer, which may be null.
   * This is a callback mechanism for a user client of direct memory to request more memory.
   * @return a WritableMemory for this off-heap resource.
   */
  static WritableMemory allocateDirect(
      long capacityBytes,
      long alignmentBytes,
      ByteOrder byteOrder,
      MemoryRequestServer memReqSvr) {
    final ResourceScope scope = ResourceScope.newConfinedScope();
    return WritableMemoryImpl.wrapDirect(capacityBytes, alignmentBytes, scope, byteOrder, memReqSvr);
  }

  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory with a ResourceScope.
   * The allocated memory will be aligned to the given <i>alignmentBytes</i>.
   *
   * <p><b>NOTICE:</b> It is the responsibility of the using application to
   * call <i>close()</i> when done.</p>
   *
   * @param capacityBytes the size of the desired memory in bytes.
   * @param alignmentBytes requested segment alignment. Typically 1, 2, 4 or 8.
   * @param scope the given ResourceScope.
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @param memReqSvr A user-specified MemoryRequestServer, which may be null.
   * This is a callback mechanism for a user client of direct memory to request more memory.
   * @return WritableMemory
   */
  static WritableMemory allocateDirect(
      long capacityBytes,
      long alignmentBytes,
      ResourceScope scope,
      ByteOrder byteOrder,
      MemoryRequestServer memReqSvr) {
    return WritableMemoryImpl.wrapDirect(capacityBytes, alignmentBytes, scope, byteOrder, memReqSvr);
  }

  //REGIONS
  /**
   * A writable region is a writable view of this object.
   * This returns a new <i>WritableMemory</i> representing the defined writable region with the
   * given offsetBytes and capacityBytes.
   * <ul>
   * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
   * <li>Returned object's capacity = <i>capacityBytes</i></li>
   * </ul>
   *
   * @param offsetBytes the starting offset with respect to this object.
   * @param capacityBytes the capacity of the returned object in bytes.
   * @return a new <i>WritableMemory</i> representing the defined writable region.
   */
  default WritableMemory writableRegion(
      long offsetBytes,
      long capacityBytes) {
    return writableRegion(offsetBytes, capacityBytes, getTypeByteOrder());
  }

  /**
   * A writable region is a writable view of this object.
   * This returns a new <i>WritableMemory</i> representing the defined writable region with the
   * given offsetBytes, capacityBytes and byte order.
   * <ul>
   * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
   * <li>Returned object's capacity = <i>capacityBytes</i></li>
   * <li>Returned object's byte order = <i>byteOrder</i></li>
   * </ul>
   *
   * @param offsetBytes the starting offset with respect to this object.
   * @param capacityBytes the capacity of the returned object in bytes.
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @return a new <i>WritableMemory</i> representing the defined writable region.
   */
  WritableMemory writableRegion(
      long offsetBytes,
      long capacityBytes,
      ByteOrder byteOrder);

  //AS WRITABLE BUFFER
  /**
   * Returns a new <i>WritableBuffer</i> with a writable view of this object.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's capacity</li>
   * <li>Returned object's <i>capacity</i> = this object's capacity</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable</li>
   * </ul>
   * @return a new <i>WritableBuffer</i> with a view of this WritableMemory
   */
  default WritableBuffer asWritableBuffer() {
    return asWritableBuffer(getTypeByteOrder());
  }

  /**
   * Returns a new <i>WritableBuffer</i> with a writable view of this object
   * with the given byte order.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's capacity</li>
   * <li>Returned object's <i>capacity</i> = this object's capacity</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable</li>
   * </ul>
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @return a new <i>WritableBuffer</i> with a view of this WritableMemory
   */
  WritableBuffer asWritableBuffer(ByteOrder byteOrder);

  //ALLOCATE HEAP BYTE ARRAYS

  /**
   * Creates on-heap WritableMemory with the given capacity and the native byte order.
   * @param capacityBytes the given capacity in bytes.
   * @return a new WritableMemory for write operations on a new byte array.
   */
  static WritableMemory allocate(int capacityBytes) {
    return allocate(capacityBytes, ByteOrder.nativeOrder(), null);
  }

  /**
   * Creates on-heap WritableMemory with the given capacity and the given byte order.
   * @param capacityBytes the given capacity in bytes.
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @return a new WritableMemory for write operations on a new byte array.
   */
  static WritableMemory allocate(
      int capacityBytes,
      ByteOrder byteOrder) {
    return allocate(capacityBytes, byteOrder, null);
  }

  /**
   * Creates on-heap WritableMemory with the given capacity and the given byte order.
   * @param capacityBytes the given capacity in bytes.
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which may be null.
   * This is a callback mechanism for a user client to request a larger <i>WritableMemory</i>.
   * @return a new WritableMemory for write operations on a new byte array.
   */
  static WritableMemory allocate(
      int capacityBytes,
      ByteOrder byteOrder,
      MemoryRequestServer memReqSvr) {
    final byte[] arr = new byte[capacityBytes];
    return writableWrap(arr, 0, capacityBytes, byteOrder, memReqSvr);
  }

  //WRITABLE WRAP - ACCESS PRIMITIVE HEAP ARRAYS for WRITE

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(byte[] array) {
    return writableWrap(array, 0, array.length, ByteOrder.nativeOrder(), null);
  }

  /**
   * Wraps the given primitive array for write operations with the given byte order.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param array the given primitive array. It must be non-null.
   * @param byteOrder the byte order to be used. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(
      byte[] array,
      ByteOrder byteOrder) {
    return writableWrap(array, 0, array.length, byteOrder, null);
  }

  /**
   * Wraps the given primitive array for write operations with the given byte order.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param array the given primitive array. It must be non-null.
   * @param offsetBytes the byte offset into the given array.
   * @param lengthBytes the number of bytes to include from the given array.
   * @param byteOrder the byte order to be used. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(
      byte[] array,
      int offsetBytes,
      int lengthBytes,
      ByteOrder byteOrder) {
    return writableWrap(array, offsetBytes, lengthBytes, byteOrder, null);
  }

  /**
   * Wraps the given primitive array for write operations with the given byte order. If the given
   * lengthBytes is zero, backing storage, byte order and read-only status of the returned
   * WritableMemory object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param array the given primitive array. It must be non-null.
   * @param offsetBytes the byte offset into the given array.
   * @param lengthBytes the number of bytes to include from the given array.
   * @param byteOrder the byte order to be used. It must be non-null.
   * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which may be null.
   * This is a callback mechanism for a user client to request a larger <i>WritableMemory</i>.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(
      byte[] array,
      int offsetBytes,
      int lengthBytes,
      ByteOrder byteOrder,
      MemoryRequestServer memReqSvr) {
    final MemorySegment slice = MemorySegment.ofArray(array).asSlice(offsetBytes, lengthBytes);
    return WritableMemoryImpl.wrapSegmentAsArray(slice, byteOrder, memReqSvr);
  }

  //intentionally removed writableWrap(boolean[])

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(char[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array);
    return WritableMemoryImpl.wrapSegmentAsArray(seg, ByteOrder.nativeOrder(), null);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(short[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array);
    return WritableMemoryImpl.wrapSegmentAsArray(seg, ByteOrder.nativeOrder(), null);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(int[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array);
    return WritableMemoryImpl.wrapSegmentAsArray(seg, ByteOrder.nativeOrder(), null);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(long[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array);
    return WritableMemoryImpl.wrapSegmentAsArray(seg, ByteOrder.nativeOrder(), null);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(float[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array);
    return WritableMemoryImpl.wrapSegmentAsArray(seg, ByteOrder.nativeOrder(), null);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  static WritableMemory writableWrap(double[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array);
    return WritableMemoryImpl.wrapSegmentAsArray(seg, ByteOrder.nativeOrder(), null);
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putX() and putXArray()

  /**
   * Puts the boolean value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putBoolean(
      long offsetBytes,
      boolean value);

  //intentionally removed putBooleanArray(...)

  /**
   * Puts the byte value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putByte(
      long offsetBytes,
      byte value);

  /**
   * Puts the byte array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  void putByteArray(
      long offsetBytes,
      byte[] srcArray,
      int srcOffsetBytes,
      int lengthBytes);

  /**
   * Puts the char value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putChar(
      long offsetBytes,
      char value);

  /**
   * Puts the char array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  void putCharArray(
      long offsetBytes,
      char[] srcArray,
      int srcOffsetChars,
      int lengthChars);

  /**
   * Puts the double value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putDouble(
      long offsetBytes,
      double value);

  /**
   * Puts the double array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  void putDoubleArray(
      long offsetBytes,
      double[] srcArray,
      int srcOffsetDoubles,
      int lengthDoubles);

  /**
   * Puts the float value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putFloat(
      long offsetBytes,
      float value);

  /**
   * Puts the float array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  void putFloatArray(
      long offsetBytes,
      float[] srcArray,
      int srcOffsetFloats,
      int lengthFloats);

  /**
   * Puts the int value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putInt(
      long offsetBytes,
      int value);

  /**
   * Puts the int array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  void putIntArray(
      long offsetBytes,
      int[] srcArray,
      int srcOffsetInts,
      int lengthInts);

  /**
   * Puts the long value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putLong(
      long offsetBytes,
      long value);

  /**
   * Puts the long array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  void putLongArray(
      long offsetBytes,
      long[] srcArray,
      int srcOffsetLongs,
      int lengthLongs);

  /**
   * Puts the short value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putShort(
      long offsetBytes,
      short value);

  /**
   * Puts the short array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  void putShortArray(
      long offsetBytes,
      short[] srcArray,
      int srcOffsetShorts,
      int lengthShorts);

  //OTHER WRITE METHODS

  /**
   * Clears all bytes of this Memory to zero
   */
  void clear();

  /**
   * Clears a portion of this Memory to zero.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param lengthBytes the length in bytes
   */
  void clear(
      long offsetBytes,
      long lengthBytes);

  /**
   * Clears the bits defined by the bitMask
   * @param offsetBytes offset bytes relative to this Memory start.
   * @param bitMask the bits set to one will be cleared
   */
  void clearBits(
      long offsetBytes,
      byte bitMask);

  /**
   * Fills all bytes of this Memory region to the given byte value.
   * @param value the given byte value
   */
  void fill(byte value);

  /**
   * Fills a portion of this Memory region to the given byte value.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param lengthBytes the length in bytes
   * @param value the given byte value
   */
  void fill(
      long offsetBytes,
      long lengthBytes,
      byte value);

  /**
   * Sets the bits defined by the bitMask
   * @param offsetBytes offset bytes relative to this Memory start
   * @param bitMask the bits set to one will be set
   */
  void setBits(
      long offsetBytes,
      byte bitMask);


  //OTHER WRITABLE API METHODS
  /**
   * Returns a copy of the primitive backing array as a byte array.
   * @return a copy of the primitive backing array as a byte array.
   */
  byte[] getArray();

}
