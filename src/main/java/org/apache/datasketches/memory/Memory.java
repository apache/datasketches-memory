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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.internal.WritableMemoryImpl;

/**
 * Defines the read-only API for offset access to a resource.
 *
 * @author Lee Rhodes
 */
public interface Memory extends Resource {

  //BYTE BUFFER

  /**
   * Provides a view of the given <i>ByteBuffer</i> for read-only operations.
   * The view is of the entire ByteBuffer independent of position and limit.
   * The returned <i>WritableMemory</i> will use the native <i>ByteOrder</i>,
   * independent of the ByteOrder of the given ByteBuffer.
   * This does not affect the ByteOrder of data already in the ByteBuffer.
   * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null.
   * @return a new <i>Memory</i> for read-only operations on the given <i>ByteBuffer</i>.
   */
  static Memory wrap(ByteBuffer byteBuffer) {
    return wrap(byteBuffer, ByteOrder.nativeOrder());
  }

  /**
   * Provides a view of the given <i>ByteBuffer</i> for read-only operations.
   * The view is of the entire ByteBuffer independent of position and limit.
   * The returned <i>WritableMemory</i> will use the native <i>ByteOrder</i>,
   * independent of the ByteOrder of the given ByteBuffer.
   * This does not affect the ByteOrder of data already in the ByteBuffer.
   * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null.
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @return a new <i>Memory</i> for read-only operations on the given <i>ByteBuffer</i>.
   */
  static Memory wrap(
      ByteBuffer byteBuffer,
      ByteOrder byteOrder) {
    return WritableMemoryImpl.wrapByteBuffer(byteBuffer, true, byteOrder, null);
  }

  //Duplicates make no sense here

  //MAP
  /**
   * Maps the given file into <i>Memory</i> for read operations
   * Calling this method is equivalent to calling
   * {@link #map(File, long, long, ByteOrder, Arena)
   * map(file, 0, file.length(), scope, ByteOrder.nativeOrder())}.
   * @param file the given file to map. It must be non-null with a non-negative length and readable.
   * @param arena the given arena. It must be non-null.
   * Warning: This class is not thread-safe. Specifying an Arena that allows multiple threads is not recommended.
   * @return <i>Memory</i> for managing the mapped memory.
   * @throws IllegalArgumentException if path is not associated with the default file system.
   * @throws IllegalStateException if scope has been already closed, or if access occurs from a thread other than the thread owning scope.
   * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
   * @throws SecurityException If a security manager is installed and it denies an unspecified permission
   * required by the implementation.
   */
  static Memory map(File file, Arena arena) throws IOException {
    return map(file, 0, file.length(), ByteOrder.nativeOrder(), arena);
  }

  /**
   * Maps the specified portion of the given file into <i>Memory</i> for read operations.
   * @param file the given file to map. It must be non-null,readable and length &ge; 0.
   * @param fileOffsetBytes the position in the given file in bytes. It must not be negative.
   * @param capacityBytes the size of the mapped memory. It must not be negative..
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @param arena the given arena. It must be non-null.
   * Warning: This class is not thread-safe. Specifying an Arena that allows multiple threads is not recommended.
   * @return <i>Memory</i> for managing the mapped memory.
   * @throws IllegalArgumentException if path is not associated with the default file system.
   * @throws IllegalStateException if scope has been already closed, or if access occurs from a thread other than the thread owning scope.
   * @throws IOException if the specified path does not point to an existing file, or if some other I/O error occurs.
   * @throws SecurityException If a security manager is installed and it denies an unspecified permission
   * required by the implementation.
   */
  static Memory map(
      File file,
      long fileOffsetBytes,
      long capacityBytes,
      ByteOrder byteOrder,
      Arena arena) throws IOException {
    return WritableMemoryImpl.wrapMap(file, fileOffsetBytes, capacityBytes, byteOrder, true, arena);
  }

  //NO ALLOCATE DIRECT, makes no sense

  //REGIONS
  /**
   * A region is a read-only view of this object.
   * <ul>
   * <li>Returned object's origin = this object's origin + offsetBytes</li>
   * <li>Returned object's capacity = capacityBytes</li>
   * </ul>
   * @param offsetBytes the starting offset with respect to the origin of this <i>Memory</i>. It must be &ge; 0.
   * @param capacityBytes the capacity of the region in bytes. It must be &ge; 0.
   * @return a new <i>Memory</i> representing the defined region based on the given
   * offsetBytes and capacityBytes.
   */
  default Memory region(
      long offsetBytes,
      long capacityBytes) {
    return region(offsetBytes, capacityBytes, getTypeByteOrder());
  }

  /**
   * A region is a read-only view of this object.
   * <ul>
   * <li>Returned object's origin = this object's origin + <i>offsetBytes</i></li>
   * <li>Returned object's capacity = <i>capacityBytes</i></li>
   * <li>Returned object's byte order = <i>byteOrder</i></li>
   * </ul>
   * @param offsetBytes the starting offset with respect to the origin of this Memory. It must be &ge; 0.
   * @param capacityBytes the capacity of the region in bytes. It must be &ge; 0.
   * @param byteOrder the given byte order. It must be non-null.
   * @return a new <i>Memory</i> representing the defined region based on the given
   * offsetBytes, capacityBytes and byteOrder.
   */
  Memory region(
      long offsetBytes,
      long capacityBytes,
      ByteOrder byteOrder);

  //AS BUFFER
  /**
   * Returns a new <i>Buffer</i> view of this object.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's capacity</li>
   * <li>Returned object's <i>capacity</i> = this object's capacity</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable</li>
   * </ul>
   * @return a new <i>Buffer</i>
   */
  default Buffer asBuffer() {
    return asBuffer(getTypeByteOrder());
  }

  /**
   * Returns a new <i>Buffer</i> view of this object, with the given
   * byte order.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's capacity</li>
   * <li>Returned object's <i>capacity</i> = this object's capacity</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable</li>
   * </ul>
   * @param byteOrder the given byte order.  It must be non-null.
   * @return a new <i>Buffer</i> with the given byteOrder.
   */
  Buffer asBuffer(ByteOrder byteOrder);

  //NO ALLOCATE HEAP BYTE ARRAYS, makes no sense

  //WRAP - ACCESS PRIMITIVE HEAP ARRAYS for readOnly
  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(byte[] array) {
    return wrap(array, 0, array.length, ByteOrder.nativeOrder());
  }

  /**
   * Wraps the given primitive array for read operations with the given byte order.
   * @param array the given primitive array.
   * @param byteOrder the byte order to be used
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(
      byte[] array,
      ByteOrder byteOrder) {
    return wrap(array, 0, array.length, byteOrder);
  }

  /**
   * Wraps the given primitive array for read operations with the given byte order.
   * @param array the given primitive array. It must be non-null.
   * @param offsetBytes the byte offset into the given array
   * @param lengthBytes the number of bytes to include from the given array.
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(
      byte[] array,
      int offsetBytes,
      int lengthBytes,
      ByteOrder byteOrder) {
    final MemorySegment slice = MemorySegment.ofArray(array).asSlice(offsetBytes, lengthBytes).asReadOnly();
    return WritableMemoryImpl.wrapSegment(slice, byteOrder);
  }

  //intentionally removed wrap(boolean[])

  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(char[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array).asReadOnly();
    return WritableMemoryImpl.wrapSegment(seg, ByteOrder.nativeOrder());
  }

  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(short[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array).asReadOnly();
    return WritableMemoryImpl.wrapSegment(seg, ByteOrder.nativeOrder());
  }

  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(int[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array).asReadOnly();
    return WritableMemoryImpl.wrapSegment(seg, ByteOrder.nativeOrder());
  }

  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(long[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array).asReadOnly();
    return WritableMemoryImpl.wrapSegment(seg, ByteOrder.nativeOrder());
  }

  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(float[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array).asReadOnly();
    return WritableMemoryImpl.wrapSegment(seg, ByteOrder.nativeOrder());
  }

  /**
   * Wraps the given primitive array for read operations assuming native byte order.
   * @param array the given primitive array. It must be non-null.
   * @return a new <i>Memory</i> for read operations
   */
  static Memory wrap(double[] array) {
    final MemorySegment seg = MemorySegment.ofArray(array).asReadOnly();
    return WritableMemoryImpl.wrapSegment(seg, ByteOrder.nativeOrder());
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE getX() and getXArray()

  /**
   * Gets the boolean value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the boolean at the given offset
   */
  boolean getBoolean(long offsetBytes);

  //intentionally removed getBooleanArray(...)

  /**
   * Gets the byte value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the byte at the given offset
   */
  byte getByte(long offsetBytes);

  /**
   * Gets the byte array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  void getByteArray(
      long offsetBytes,
      byte[] dstArray,
      int dstOffsetBytes,
      int lengthBytes);

  /**
   * Gets the char value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the char at the given offset
   */
  char getChar(long offsetBytes);

  /**
   * Gets the char array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  void getCharArray(
      long offsetBytes,
      char[] dstArray,
      int dstOffsetChars,
      int lengthChars);

  //Missing getCharsFromUtf8(long offsetBytes, int utf8LengthBytes, Appendable ds)

  //Missing getCharsFromUtf8(long offsetBytes, int utf8LengthBytes, StringBuilder dst)

  /**
   * Gets the double value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the double at the given offset
   */
  double getDouble(long offsetBytes);

  /**
   * Gets the double array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  void getDoubleArray(
      long offsetBytes,
      double[] dstArray,
      int dstOffsetDoubles,
      int lengthDoubles);

  /**
   * Gets the float value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the float at the given offset
   */
  float getFloat(long offsetBytes);

  /**
   * Gets the float array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  void getFloatArray(
      long offsetBytes,
      float[] dstArray,
      int dstOffsetFloats,
      int lengthFloats);

  /**
   * Gets the int value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the int at the given offset
   */
  int getInt(long offsetBytes);

  /**
   * Gets the int array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  void getIntArray(
      long offsetBytes,
      int[] dstArray,
      int dstOffsetInts,
      int lengthInts);

  /**
   * Gets the long value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the long at the given offset
   */
  long getLong(long offsetBytes);

  /**
   * Gets the long array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  void getLongArray(
      long offsetBytes,
      long[] dstArray,
      int dstOffsetLongs,
      int lengthLongs);

  /**
   * Gets the short value at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the short at the given offset
   */
  short getShort(long offsetBytes);

  /**
   * Gets the short array at the given offset
   * @param offsetBytes offset bytes relative to this Memory start
   * @param dstArray The preallocated destination array.
   * @param dstOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  void getShortArray(
      long offsetBytes,
      short[] dstArray,
      int dstOffsetShorts,
      int lengthShorts);

  //SPECIAL PRIMITIVE READ METHODS: compareTo, copyTo, writeTo

  /**
   * Copies bytes from a source range of this Memory to a destination range of the given Memory
   * with the same semantics when copying between overlapping ranges of bytes as method
   * {@link java.lang.System#arraycopy(Object, int, Object, int, int)} has. However, if the source
   * and the destination ranges are exactly the same, this method throws {@link
   * IllegalArgumentException}, because it should never be needed in real-world scenarios and
   * therefore indicates a bug.
   * @param srcOffsetBytes the source offset for this Memory
   * @param destination the destination Memory, which may not be Read-Only.
   * @param dstOffsetBytes the destination offset
   * @param lengthBytes the number of bytes to copy
   */
  void copyTo(
      long srcOffsetBytes,
      WritableMemory destination,
      long dstOffsetBytes,
      long lengthBytes);

  /**
   * Writes bytes from a source range of this Memory to the given {@code WritableByteChannel}.
   * @param offsetBytes the source offset for this Memory
   * @param lengthBytes the number of bytes to copy
   * @param out the destination ByteArrayOutputStream
   * @throws IOException may occur while writing to the ByteArrayOutputStream.
   */
  void writeToByteStream(
      long offsetBytes,
      int lengthBytes,
      ByteArrayOutputStream out) throws IOException;

}
