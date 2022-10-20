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

import java.nio.ByteOrder;

/**
 * Defines the read API for relative positional access to a resource.
 */
public interface Buffer extends Resource {
    /**
     * Increments the current position by the given increment.
     * Asserts that the resource is valid and that the positional invariants are not violated,
     * otherwise, if asserts are enabled throws an {@link AssertionError}.
     * @param increment the given increment
     * @return Buffer
     */
    Buffer incrementPosition(long increment);

    /**
     * Increments the current position by the given increment.
     * Checks that the resource is valid and that the positional invariants are not violated,
     * otherwise throws an {@link IllegalArgumentException}.
     * @param increment the given increment
     * @return Buffer
     */
    Buffer incrementAndCheckPosition(final long increment);

    /**
     * Gets the end position
     * @return the end position
     */
    long getEnd();

    /**
     * Gets the current position
     * @return the current position
     */
    long getPosition();

    /**
     * Gets start position
     * @return start position
     */
    long getStart();

    /**
     * The number of elements remaining between the current position and the end position
     * @return {@code (end - position)}
     */
    long getRemaining();

    /**
     * Returns true if there are elements remaining between the current position and the end position
     * @return {@code (end - position) > 0}
     */
    boolean hasRemaining();

    /**
     * Resets the current position to the start position,
     * This does not modify any data.
     * @return Buffer
     */
    Buffer resetPosition();

    /**
     * Sets the current position.
     * Asserts that the positional invariants are not violated,
     * otherwise, if asserts are enabled throws an {@link AssertionError}.
     * @param position the given current position.
     * @return Buffer
     */
    Buffer setPosition(long position);

    /**
     * Sets the current position.
     * Checks that the positional invariants are not violated,
     * otherwise, throws an {@link IllegalArgumentException}.
     * @param position the given current position.
     * @return Buffer
     */
    Buffer setAndCheckPosition(long position);

    /**
     * Sets start position, current position, and end position.
     * Asserts that the positional invariants are not violated,
     * otherwise, if asserts are enabled throws an {@link AssertionError}.
     * @param start the start position in the buffer
     * @param position the current position between the start and end
     * @param end the end position in the buffer
     * @return Buffer
     */
    Buffer setStartPositionEnd(long start, long position, long end);

    /**
     * Sets start position, current position, and end position.
     * Checks that the positional invariants are not violated,
     * otherwise, throws an {@link IllegalArgumentException}.
     * @param start the start position in the buffer
     * @param position the current position between the start and end
     * @param end the end position in the buffer
     * @return Buffer
     */
    Buffer setAndCheckStartPositionEnd(long start, long position, long end);

    // DUPLICATES
    /**
     * Returns a read-only duplicate view of this Buffer with the same but independent values of
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
     * 
     * @return a read-only duplicate view of this Buffer with the same but independent values of
     *         <i>start</i>, <i>position</i> and <i>end</i>.
     */
    Buffer duplicate();

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
     * 
     * @param byteOrder the given <i>ByteOrder</i>.
     * @return a read-only duplicate view of this Buffer with the same but independent values of
     *         <i>start</i>, <i>position</i> and <i>end</i>.
     */
    Buffer duplicate(ByteOrder byteOrder);

    // NO MAP

    // REGIONS
    /**
     * A region is a read-only view of this object.
     * <ul>
     * <li>Returned object's origin = this object's <i>position</i></li>
     * <li>Returned object's <i>start</i> = 0</li>
     * <li>Returned object's <i>position</i> = 0</li>
     * <li>Returned object's <i>end</i> = this object's (<i>end</i> - <i>position</i>)</li>
     * <li>Returned object's <i>capacity</i> = this object's (<i>end</i> - <i>position</i>)</li>
     * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable and
     * independent of this object's <i>start</i>, <i>position</i> and <i>end</i></li>
     * </ul>
     * 
     * @return a new <i>Buffer</i> representing the defined region based on the current
     *         <i>position</i> and <i>end</i>.
     */
    Buffer region();

    /**
     * A region is a read-only view of this object.
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
     *         based on the current <i>position</i>, <i>end</i> and byteOrder.
     */
    Buffer region(long offsetBytes, long capacityBytes, ByteOrder byteOrder);

    // AS MEMORY
    /**
     * Convert this Buffer to a Memory in native byte order (which may be different than the 
     * current buffer's byte order. The current <i>start</i>, <i>position</i> and <i>end</i>
     * are ignored. 
     * 
     * @return Memory
     */
    default Memory asMemory() {
        return asMemory(getByteOrder());
    }

    /**
     * Convert this Buffer to a Memory with the given byte order.
     * The current <i>start</i>, <i>position</i> and <i>end</i> are ignored.
     * 
     * @param byteOrder the given byte order.
     * @return Memory
     */
    Memory asMemory(ByteOrder byteOrder);

    // NO ACCESS PRIMITIVE HEAP ARRAYS for readOnly

    // PRIMITIVE getX() and getXArray()
    /**
     * Gets the boolean value at the current position.
     * Increments the position by 1.
     * 
     * @return the boolean at the current position
     */
    boolean getBoolean();

    /**
     * Gets the boolean value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the boolean at the given offset
     */
    boolean getBoolean(long offsetBytes);

    /**
     * Gets the boolean array at the current position.
     * Increments the position by <i>lengthBooleans - dstOffsetBooleans</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetBooleans offset in array units
     * @param lengthBooleans number of array units to transfer
     */
    void getBooleanArray(boolean[] dstArray, int dstOffsetBooleans, int lengthBooleans);

    /**
     * Gets the byte value at the current position.
     * Increments the position by <i>Byte.BYTES</i>.
     * 
     * @return the byte at the current position
     */
    byte getByte();

    /**
     * Gets the byte value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the byte at the given offset
     */
    byte getByte(long offsetBytes);

    /**
     * Gets the byte array at the current position.
     * Increments the position by <i>Byte.BYTES * (lengthBytes - dstOffsetBytes)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetBytes offset in array units
     * @param lengthBytes number of array units to transfer
     */
    void getByteArray(byte[] dstArray, int dstOffsetBytes, int lengthBytes);

    /**
     * Gets the char value at the current position.
     * Increments the position by <i>Character.BYTES</i>.
     * 
     * @return the char at the current position
     */
    char getChar();

    /**
     * Gets the char value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the char at the given offset
     */
    char getChar(long offsetBytes);

    /**
     * Gets the char array at the current position.
     * Increments the position by <i>Character.BYTES * (lengthChars - dstOffsetChars)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetChars offset in array units
     * @param lengthChars number of array units to transfer
     */
    void getCharArray(char[] dstArray, int dstOffsetChars, int lengthChars);

    /**
     * Gets the double value at the current position.
     * Increments the position by <i>Double.BYTES</i>.
     * 
     * @return the double at the current position
     */
    double getDouble();

    /**
     * Gets the double value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the double at the given offset
     */
    double getDouble(long offsetBytes);

    /**
     * Gets the double array at the current position.
     * Increments the position by <i>Double.BYTES * (lengthDoubles - dstOffsetDoubles)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetDoubles offset in array units
     * @param lengthDoubles number of array units to transfer
     */
    void getDoubleArray(double[] dstArray, int dstOffsetDoubles, int lengthDoubles);

    /**
     * Gets the float value at the current position.
     * Increments the position by <i>Float.BYTES</i>.
     * 
     * @return the float at the current position
     */
    float getFloat();

    /**
     * Gets the float value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the float at the given offset
     */
    float getFloat(long offsetBytes);

    /**
     * Gets the float array at the current position.
     * Increments the position by <i>Float.BYTES * (lengthFloats - dstOffsetFloats)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetFloats offset in array units
     * @param lengthFloats number of array units to transfer
     */
    void getFloatArray(float[] dstArray, int dstOffsetFloats, int lengthFloats);

    /**
     * Gets the int value at the current position.
     * Increments the position by <i>Integer.BYTES</i>.
     * 
     * @return the int at the current position
     */
    int getInt();

    /**
     * Gets the int value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the int at the given offset
     */
    int getInt(long offsetBytes);

    /**
     * Gets the int array at the current position.
     * Increments the position by <i>Integer.BYTES * (lengthInts - dstOffsetInts)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetInts offset in array units
     * @param lengthInts number of array units to transfer
     */
    void getIntArray(int[] dstArray, int dstOffsetInts, int lengthInts);

    /**
     * Gets the long value at the current position.
     * Increments the position by <i>Long.BYTES</i>.
     * 
     * @return the long at the current position
     */
    long getLong();

    /**
     * Gets the long value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the long at the given offset
     */
    long getLong(long offsetBytes);

    /**
     * Gets the long array at the current position.
     * Increments the position by <i>Long.BYTES * (lengthLongs - dstOffsetLongs)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetLongs offset in array units
     * @param lengthLongs number of array units to transfer
     */
    void getLongArray(long[] dstArray, int dstOffsetLongs, int lengthLongs);

    /**
     * Gets the short value at the current position.
     * Increments the position by <i>Short.BYTES</i>.
     * 
     * @return the short at the current position
     */
    short getShort();

    /**
     * Gets the short value at the given offset.
     * This does not change the position.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the short at the given offset
     */
    short getShort(long offsetBytes);

    /**
     * Gets the short array at the current position.
     * Increments the position by <i>Short.BYTES * (lengthShorts - dstOffsetShorts)</i>.
     * 
     * @param dstArray The preallocated destination array.
     * @param dstOffsetShorts offset in array units
     * @param lengthShorts number of array units to transfer
     */
    void getShortArray(short[] dstArray, int dstOffsetShorts, int lengthShorts);

    // SPECIAL PRIMITIVE READ METHODS: compareTo
    /**
     * Compares the bytes of this Buffer to <i>that</i> Buffer.
     * This uses absolute offsets not the start, position and end.
     * Returns <i>(this &lt; that) ? (some negative value) : (this &gt; that) ? (some positive value)
     * : 0;</i>.
     * If all bytes are equal up to the shorter of the two lengths, the shorter length is
     * considered to be less than the other.
     * 
     * @param thisOffsetBytes the starting offset for <i>this Buffer</i>
     * @param thisLengthBytes the length of the region to compare from <i>this Buffer</i>
     * @param that the other Buffer to compare with
     * @param thatOffsetBytes the starting offset for <i>that Buffer</i>
     * @param thatLengthBytes the length of the region to compare from <i>that Buffer</i>
     * @return <i>(this &lt; that) ? (some negative value) : (this &gt; that) ? (some positive value)
     *         : 0;</i>
     */
    int compareTo(long thisOffsetBytes, long thisLengthBytes, Buffer that, long thatOffsetBytes, long thatLengthBytes);


}
