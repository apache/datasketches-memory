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

public interface WritableBuffer {

  //BYTE BUFFER
  /**
   * Accesses the given ByteBuffer for write operations. The returned WritableBuffer object has
   * the same byte order, as the given ByteBuffer, unless the capacity of the given ByteBuffer is
   * zero, then byte order of the returned WritableBuffer object, as well as backing storage and
   * read-only status are unspecified.
   * @param byteBuf the given ByteBuffer, must not be null.
   * @return a new WritableBuffer for write operations on the given ByteBuffer.
   */
  static WritableBuffer writableWrap(ByteBuffer byteBuf) {
    return WritableBufferImpl.writableWrap(byteBuf, byteBuf.order());
  }

  /**
   * Accesses the given ByteBuffer for write operations. The returned WritableBuffer object has
   * the given byte order, ignoring the byte order of the given ByteBuffer. If the capacity of
   * the given ByteBuffer is zero the byte order of the returned WritableBuffer object
   * (as well as backing storage) is unspecified.
   * @param byteBuf the given ByteBuffer, must not be null
   * @param byteOrder the byte order to be used, which may be independent of the byte order
   * state of the given ByteBuffer
   * @return a new WritableBuffer for write operations on the given ByteBuffer.
   */
  static WritableBuffer writableWrap(ByteBuffer byteBuf, ByteOrder byteOrder) {
    return WritableBufferImpl.writableWrap(byteBuf, byteOrder);
  }  
  
  //DUPLICATES
  /**
   * Returns a duplicate writable view of this Buffer with the same but independent values of
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
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @return a duplicate writable view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  WritableBuffer writableDuplicate();

  /**
   * Returns a duplicate writable view of this Buffer with the same but independent values of
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
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @param byteOrder the given <i>ByteOrder</i>.
   * @return a duplicate writable view of this Buffer with the same but independent values of
   * <i>start</i>, <i>position</i> and <i>end</i>.
   */
  WritableBuffer writableDuplicate(ByteOrder byteOrder);


  //REGIONS
  /**
   * A writable region is a writable view of this object.
   * <ul>
   * <li>Returned object's origin = this object's <i>position</i></li>
   * <li>Returned object's <i>start</i> = 0</li>
   * <li>Returned object's <i>position</i> = 0</li>
   * <li>Returned object's <i>end</i> = this object's (<i>end</i> - <i>position</i>)</li>
   * <li>Returned object's <i>capacity</i> = this object's (<i>end</i> - <i>position</i>)</li>
   * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
   * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
   * </ul>
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @return a new <i>WritableBuffer</i> representing the defined writable region.
   */
  WritableBuffer writableRegion();

  /**
   * A writable region is a writable view of this object.
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
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   *
   * <p><b>Note: </b><i>asWritableMemory()</i> and <i>asMemory()</i>
   * will return the originating <i>Memory</i> byte order.</p>
   * @param offsetBytes the starting offset with respect to the origin of this <i>WritableBuffer</i>
   * @param capacityBytes the <i>capacity</i> of the returned region in bytes
   * @param byteOrder the given byte order
   * @return a new <i>WritableBuffer</i> representing the defined writable region
   * with the given offsetBytes, capacityBytes and byte order.
   */
  WritableBuffer writableRegion(long offsetBytes, long capacityBytes,
      ByteOrder byteOrder);

  //AS MEMORY
  /**
   * Convert this WritableBuffer to a WritableMemory.
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @return WritableMemory
   */
  WritableMemory asWritableMemory();

  //PRIMITIVE putX() and putXArray()
  /**
   * Puts the boolean value at the current position.
   * Increments the position by 1.
   * @param value the value to put
   */
  void putBoolean(boolean value);

  /**
   * Puts the boolean value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start.
   * @param value the value to put
   */
  void putBoolean(long offsetBytes, boolean value);

  /**
   * Puts the boolean array at the current position.
   * Increments the position by <i>lengthBooleans - srcOffsetBooleans</i>.
   * @param srcArray The source array.
   * @param srcOffsetBooleans offset in array units
   * @param lengthBooleans number of array units to transfer
   */
  void putBooleanArray(boolean[] srcArray, int srcOffsetBooleans,
      int lengthBooleans);

  /**
   * Puts the byte value at the current position.
   * Increments the position by <i>Byte.BYTES</i>.
   * @param value the value to put
   */
  void putByte(byte value);

  /**
   * Puts the byte value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putByte(long offsetBytes, byte value);

  /**
   * Puts the byte array at the current position.
   * Increments the position by <i>Byte.BYTES * (lengthBytes - srcOffsetBytes)</i>.
   * @param srcArray The source array.
   * @param srcOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  void putByteArray(byte[] srcArray, int srcOffsetBytes, int lengthBytes);

  /**
   * Puts the char value at the current position.
   * Increments the position by <i>Character.BYTES</i>.
   * @param value the value to put
   */
  void putChar(char value);

  /**
   * Puts the char value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putChar(long offsetBytes, char value);

  /**
   * Puts the char array at the current position.
   * Increments the position by <i>Character.BYTES * (lengthChars - srcOffsetChars)</i>.
   * @param srcArray The source array.
   * @param srcOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  void putCharArray(char[] srcArray, int srcOffsetChars, int lengthChars);

  /**
   * Puts the double value at the current position.
   * Increments the position by <i>Double.BYTES</i>.
   * @param value the value to put
   */
  void putDouble(double value);

  /**
   * Puts the double value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putDouble(long offsetBytes, double value);

  /**
   * Puts the double array at the current position.
   * Increments the position by <i>Double.BYTES * (lengthDoubles - srcOffsetDoubles)</i>.
   * @param srcArray The source array.
   * @param srcOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  void putDoubleArray(double[] srcArray, int srcOffsetDoubles, int lengthDoubles);

  /**
   * Puts the float value at the current position.
   * Increments the position by <i>Float.BYTES</i>.
   * @param value the value to put
   */
  void putFloat(float value);

  /**
   * Puts the float value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putFloat(long offsetBytes, float value);

  /**
   * Puts the float array at the current position.
   * Increments the position by <i>Float.BYTES * (lengthFloats - srcOffsetFloats)</i>.
   * @param srcArray The source array.
   * @param srcOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  void putFloatArray(float[] srcArray, int srcOffsetFloats, int lengthFloats);

  /**
   * Puts the int value at the current position.
   * Increments the position by <i>Integer.BYTES</i>.
   * @param value the value to put
   */
  void putInt(int value);

  /**
   * Puts the int value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putInt(long offsetBytes, int value);

  /**
   * Puts the int array at the current position.
   * Increments the position by <i>Integer.BYTES * (lengthInts - srcOffsetInts)</i>.
   * @param srcArray The source array.
   * @param srcOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  void putIntArray(int[] srcArray, int srcOffsetInts, int lengthInts);

  /**
   * Puts the long value at the current position.
   * Increments the position by <i>Long.BYTES</i>.
   * @param value the value to put
   */
  void putLong(long value);

  /**
   * Puts the long value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putLong(long offsetBytes, long value);

  /**
   * Puts the long array at the current position.
   * Increments the position by <i>Long.BYTES * (lengthLongs - srcOffsetLongs)</i>.
   * @param srcArray The source array.
   * @param srcOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  void putLongArray(long[] srcArray, int srcOffsetLongs, int lengthLongs);

  /**
   * Puts the short value at the current position.
   * Increments the position by <i>Short.BYTES</i>.
   * @param value the value to put
   */
  void putShort(short value);

  /**
   * Puts the short value at the given offset.
   * This does not change the position.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  void putShort(long offsetBytes, short value);

  /**
   * Puts the short array at the current position.
   * Increments the position by <i>Short.BYTES * (lengthShorts - srcOffsetShorts)</i>.
   * @param srcArray The source array.
   * @param srcOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  void putShortArray(short[] srcArray, int srcOffsetShorts, int lengthShorts);

  //OTHER WRITE METHODS
  /**
   * Returns the primitive backing array, otherwise null.
   * @return the primitive backing array, otherwise null.
   */
  Object getArray();

  /**
   * Clears all bytes of this Buffer from position to end to zero. The position will be set to end.
   */
  void clear();

  /**
   * Fills this Buffer from position to end with the given byte value.
   * The position will be set to <i>end</i>.
   * @param value the given byte value
   */
  void fill(byte value);

  //OTHER WRITABLE API METHODS
  /**
   * For Direct Memory only. Other types of backing resources will return null.
   * Gets the MemoryRequestServer object used by dynamic off-heap (Direct) memory objects
   * to request additional memory.
   * Set using {@link WritableMemory#allocateDirect(long, MemoryRequestServer)}.
   * If not explicity set, this returns the {@link DefaultMemoryRequestServer}.
   * @return the MemoryRequestServer object (if direct memory) or null.
   */
  public MemoryRequestServer getMemoryRequestServer();

}
