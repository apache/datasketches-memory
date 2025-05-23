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

import org.apache.datasketches.memory.internal.WritableBufferImpl;

/**
 * Defines the read-only API for relative positional access to a resource.
 *
 * @author Lee Rhodes
 */
public interface Buffer extends Positional, Resource {

  //BYTE BUFFER
  /**
   * Provides a view of the given <i>ByteBuffer</i> for read-only operations.
   * The view is of the entire ByteBuffer independent of position and limit.
   * However, the returned <i>WritableBuffer</i> will have a position and end set to the
   * ByteBuffer's position and limit, respectively.
   * The returned WritableBuffer will use the native <i>ByteOrder</i>,
   * ignoring the ByteOrder of the given ByteBuffer.
   * This does not affect the ByteOrder of data already in the ByteBuffer.
   * @param byteBuffer the given ByteBuffer, must not be null.
   * @return a new <i>Buffer</i> for read-only operations on the given ByteBuffer.
   */
  static Buffer wrap(ByteBuffer byteBuffer) {
    return wrap(byteBuffer, ByteOrder.nativeOrder());
  }

  /**
   * Provides a view of the given <i>ByteBuffer</i> for read-only operations.
   * The view is of the entire ByteBuffer independent of position and limit.
   * However, the returned <i>WritableBuffer</i> will have a position and end set to the
   * ByteBuffer's position and limit, respectively.
   * The returned WritableBuffer will use the native <i>ByteOrder</i>,
   * ignoring the ByteOrder of the given ByteBuffer.
   * This does not affect the ByteOrder of data already in the ByteBuffer.
   * @param byteBuffer the given ByteBuffer, must not be null
   * @param byteOrder the ByteOrder to be used. It must be non-null.
   * @return a new <i>Buffer</i> for read-only operations on the given ByteBuffer.
   */
  static Buffer wrap(
      ByteBuffer byteBuffer,
      ByteOrder byteOrder) {
    return WritableBufferImpl.wrapByteBuffer(byteBuffer, true, byteOrder, null);
  }

  //DUPLICATES
  /**
   * Returns a read-only duplicate view of this Buffer with the same byte order,
   * but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = this object's <i>start</i></li>
   * <li>Returned object's <i>position</i> = this object's <i>position</i></li>
   * <li>Returned object's <i>end</i> = this object's <i>end</i></li>
   * <li>Returned object's <i>capacity</i> = this object' <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * @return a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  default Buffer duplicate() {
    return duplicate(getTypeByteOrder());
  }

  /**
   * Returns a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>, but with the specified byteOrder.
   * <ul>
   * <li>Returned object's origin = this object's origin</li>
   * <li>Returned object's <i>start</i> = this object's <i>start</i></li>
   * <li>Returned object's <i>position</i> = this object's <i>position</i></li>
   * <li>Returned object's <i>end</i> = this object's <i>end</i></li>
   * <li>Returned object's <i>capacity</i> = this object' <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * @param byteOrder the given <i>ByteOrder</i>.
   * @return a read-only duplicate view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  Buffer duplicate(ByteOrder byteOrder);

  //NO MAP use Memory
  //NO ALLOCATE DIRECT, makes no sense

  //REGIONS
  /**
   * A region is a read-only view of this object from <i>position</i> to <i>end</i>
   * and with the same byte order.
   * <ul>
   * <li>Returned object's origin = this object's <i>position</i></li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's (<i>end</i> - <i>position</i>)</li>
   * <li>Returned object's <i>capacity</i> = this object's (<i>end</i> - <i>position</i>)</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * @return a new <i>Buffer</i> representing the defined region based on the current
   * <i>position</i> and <i>end</i>.
   */
  default Buffer region() {
    return region(getPosition(), getEnd() - getPosition(), getTypeByteOrder());
  }

  /**
   * A region is a read-only view of this object from offsetBytes and with a length of
   * capacityBytes and with the given byte order.
   * <ul>
   * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = <i>capacityBytes</i></li>
   * <li>Returned object's <i>capacity</i> = <i>capacityBytes</i></li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * <li>Returned object's byte order = <i>byteOrder</i></li>
   * </ul>
   *
   * @param offsetBytes the starting offset with respect to the origin of this <i>WritableBuffer</i>
   * @param capacityBytes the <i>capacity</i> of the returned region in bytes
   * @param byteOrder the given byte order
   * @return a new <i>Buffer</i> representing the defined writable region
   * based on the current <i>position</i>, <i>end</i> and byteOrder.
   */
  Buffer region(
      long offsetBytes,
      long capacityBytes,
      ByteOrder byteOrder);

  //AS MEMORY
  /**
   * Convert this Buffer to a Memory. The current <i>start</i>, <i>position</i> and <i>end</i>
   * are ignored.
   * @return Memory
   */
  default Memory asMemory() {
    return asMemory(getTypeByteOrder());
  }

  /**
   * Convert this Buffer to a Memory with the given byte order.
   * The current <i>start</i>, <i>position</i> and <i>end</i> are ignored.
   * @param byteOrder the given byte order.
   * @return Memory
   */
  Memory asMemory(ByteOrder byteOrder);

  //NO ALLOCATE HEAP BYTE ARRAYS, makes no sense. Use WritableMemory
  //NO WRAP - ACCESS PRIMITIVE HEAP ARRAYS for readOnly.
  //  Because of the ambiguity of positional API with the multibyte primitives. Use Memory instead.
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE getX() and getXArray()

  /**
   * Gets the boolean value at the current position.
   * Increments the position by <i>Byte.BYTES</i>.
   * @return the boolean at the current position
   */
  boolean getBoolean();

  /**
   * Gets the boolean value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the boolean at the given offset
   */
  boolean getBoolean(long offsetBytes);

  //intentionally removed getBooleanArray(...)

  /**
   * Gets the byte value at the current position.
   * Increments the position by <i>Byte.BYTES</i>.
   * @return the byte at the current position
   */
  byte getByte();

  /**
   * Gets the byte value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the byte at the given offset
   */
  byte getByte(long offsetBytes);

  /**
   * Gets the byte array at the current position.
   * Increments the position by <i>Byte.BYTES * (lengthBytes - dstOffsetBytes)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  void getByteArray(
      byte[] dstArray,
      int dstOffsetBytes,
      int lengthBytes);

  /**
   * Gets the char value at the current position.
   * Increments the position by <i>Character.BYTES</i>.
   * @return the char at the current position
   */
  char getChar();

  /**
   * Gets the char value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the char at the given offset
   */
  char getChar(long offsetBytes);

  /**
   * Gets the char array at the current position.
   * Increments the position by <i>Character.BYTES * (lengthChars - dstOffsetChars)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  void getCharArray(
      char[] dstArray,
      int dstOffsetChars,
      int lengthChars);

  /**
   * Gets the double value at the current position.
   * Increments the position by <i>Double.BYTES</i>.
   * @return the double at the current position
   */
  double getDouble();

  /**
   * Gets the double value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the double at the given offset
   */
  double getDouble(long offsetBytes);

  /**
   * Gets the double array at the current position.
   * Increments the position by <i>Double.BYTES * (lengthDoubles - dstOffsetDoubles)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  void getDoubleArray(
      double[] dstArray,
      int dstOffsetDoubles,
      int lengthDoubles);

  /**
   * Gets the float value at the current position.
   * Increments the position by <i>Float.BYTES</i>.
   * @return the float at the current position
   */
  float getFloat();

  /**
   * Gets the float value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the float at the given offset
   */
  float getFloat(long offsetBytes);

  /**
   * Gets the float array at the current position.
   * Increments the position by <i>Float.BYTES * (lengthFloats - dstOffsetFloats)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  void getFloatArray(
      float[] dstArray,
      int dstOffsetFloats,
      int lengthFloats);

  /**
   * Gets the int value at the current position.
   * Increments the position by <i>Integer.BYTES</i>.
   * @return the int at the current position
   */
  int getInt();

  /**
   * Gets the int value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the int at the given offset
   */
  int getInt(long offsetBytes);

  /**
   * Gets the int array at the current position.
   * Increments the position by <i>Integer.BYTES * (lengthInts - dstOffsetInts)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  void getIntArray(
      int[] dstArray,
      int dstOffsetInts,
      int lengthInts);

  /**
   * Gets the long value at the current position.
   * Increments the position by <i>Long.BYTES</i>.
   * @return the long at the current position
   */
  long getLong();

  /**
   * Gets the long value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the long at the given offset
   */
  long getLong(long offsetBytes);

  /**
   * Gets the long array at the current position.
   * Increments the position by <i>Long.BYTES * (lengthLongs - dstOffsetLongs)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  void getLongArray(
      long[] dstArray,
      int dstOffsetLongs,
      int lengthLongs);

  /**
   * Gets the short value at the current position.
   * Increments the position by <i>Short.BYTES</i>.
   * @return the short at the current position
   */
  short getShort();

  /**
   * Gets the short value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this Memory start
   * @return the short at the given offset
   */
  short getShort(long offsetBytes);

  /**
   * Gets the short array at the current position.
   * Increments the position by <i>Short.BYTES * (lengthShorts - dstOffsetShorts)</i>.
   * @param dstArray The preallocated destination array.
   * @param dstOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  void getShortArray(
      short[] dstArray,
      int dstOffsetShorts,
      int lengthShorts);

  //SPECIAL PRIMITIVE READ METHODS:none.

}
