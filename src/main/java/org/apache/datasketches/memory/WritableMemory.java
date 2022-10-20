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

/**
 * Write API for offset access to a memory resource.
 */
public interface WritableMemory extends Resource, Memory {
    // REGIONS
    /**
     * A writable region is a writable view of this object.
     * This returns a new <i>WritableMemory</i> representing the defined writable region with the
     * given offsetBytes and capacityBytes and in native byte order (which may be different
     * than the current byte order).
     * <ul>
     * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
     * <li>Returned object's capacity = <i>capacityBytes</i></li>
     * </ul>
     *
     * @param offsetBytes the starting offset with respect to this object. It must be &ge; 0.
     * @param capacityBytes the capacity of the returned object in bytes. It must be &ge; 0.
     * @return a new <i>WritableMemory</i> representing the defined writable region.
     */
    default WritableMemory writableRegion(long offsetBytes, long capacityBytes) {
        return writableRegion(offsetBytes, capacityBytes, getByteOrder());
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
     * @param offsetBytes the starting offset with respect to this object. It must be &ge; 0.
     * @param capacityBytes the capacity of the returned object in bytes. It must be &ge; 0.
     * @param byteOrder the given byte order. It must be non-null.
     * @return a new <i>WritableMemory</i> representing the defined writable region.
     */
    WritableMemory writableRegion(long offsetBytes, long capacityBytes, ByteOrder byteOrder);

    // AS WRITABLE BUFFER
    /**
     * Returns a new <i>WritableBuffer</i> with a writable view of this object.
     * 
     * <ul>
     * <li>Returned object's origin = this object's origin</li>
     * <li>Returned object's <i>start</i> = 0</li>
     * <li>Returned object's <i>position</i> = 0</li>
     * <li>Returned object's <i>end</i> = this object's capacity</li>
     * <li>Returned object's <i>capacity</i> = this object's capacity</li>
     * <li>Returned object's <i>start</i>, <i>position</i> and <i>end</i> are mutable</li>
     * </ul>
     * 
     * @return a new <i>WritableBuffer</i> with a view of this WritableMemory
     */
    default WritableBuffer asWritableBuffer() {
        return asWritableBuffer(getByteOrder());
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
     * 
     * @param byteOrder the given byte order
     * @return a new <i>WritableBuffer</i> with a view of this WritableMemory
     */
    WritableBuffer asWritableBuffer(ByteOrder byteOrder);

    // PRIMITIVE putX() and putXArray()
    /**
     * Puts the boolean value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putBoolean(long offsetBytes, boolean value);

    /**
     * Puts the boolean array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetBooleans offset in array units
     * @param lengthBooleans number of array units to transfer
     */
    void putBooleanArray(long offsetBytes, boolean[] srcArray, int srcOffsetBooleans, int lengthBooleans);

    /**
     * Puts the byte value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putByte(long offsetBytes, byte value);

    /**
     * Puts the byte array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetBytes offset in array units
     * @param lengthBytes number of array units to transfer
     */
    void putByteArray(long offsetBytes, byte[] srcArray, int srcOffsetBytes, int lengthBytes);

    /**
     * Puts the char value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putChar(long offsetBytes, char value);

    /**
     * Puts the char array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetChars offset in array units
     * @param lengthChars number of array units to transfer
     */
    void putCharArray(long offsetBytes, char[] srcArray, int srcOffsetChars, int lengthChars);

    /**
     * Encodes characters from the given CharSequence into UTF-8 bytes and puts them into this
     * <i>WritableMemory</i> begining at the given offsetBytes.
     * This is specifically designed to reduce the production of intermediate objects (garbage),
     * thus significantly reducing pressure on the JVM Garbage Collector.
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param src The source CharSequence to be encoded and put into this WritableMemory. It is
     *        the responsibility of the caller to provide sufficient capacity in this
     *        <i>WritableMemory</i> for the encoded Utf8 bytes. Characters outside the ASCII range can
     *        require 2, 3 or 4 bytes per character to encode.
     * @return the number of bytes encoded
     */
    long putCharsToUtf8(long offsetBytes, CharSequence src);

    /**
     * Puts the double value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putDouble(long offsetBytes, double value);

    /**
     * Puts the double array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetDoubles offset in array units
     * @param lengthDoubles number of array units to transfer
     */
    void putDoubleArray(long offsetBytes, double[] srcArray, int srcOffsetDoubles, int lengthDoubles);

    /**
     * Puts the float value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putFloat(long offsetBytes, float value);

    /**
     * Puts the float array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetFloats offset in array units
     * @param lengthFloats number of array units to transfer
     */
    void putFloatArray(long offsetBytes, float[] srcArray, int srcOffsetFloats, int lengthFloats);

    /**
     * Puts the int value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putInt(long offsetBytes, int value);

    /**
     * Puts the int array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetInts offset in array units
     * @param lengthInts number of array units to transfer
     */
    void putIntArray(long offsetBytes, int[] srcArray, int srcOffsetInts, int lengthInts);

    /**
     * Puts the long value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putLong(long offsetBytes, long value);

    /**
     * Puts the long array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetLongs offset in array units
     * @param lengthLongs number of array units to transfer
     */
    void putLongArray(long offsetBytes, long[] srcArray, int srcOffsetLongs, int lengthLongs);

    /**
     * Puts the short value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param value the value to put
     */
    void putShort(long offsetBytes, short value);

    /**
     * Puts the short array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
     * @param srcArray The source array.
     * @param srcOffsetShorts offset in array units
     * @param lengthShorts number of array units to transfer
     */
    void putShortArray(long offsetBytes, short[] srcArray, int srcOffsetShorts, int lengthShorts);

    // Atomic Methods
    /**
     * Atomically adds the given value to the long located at offsetBytes.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param delta the amount to add
     * @return the the previous value
     */
    long getAndAddLong(long offsetBytes, long delta);

    /**
     * Atomically sets the current value at the memory location to the given updated value
     * if and only if the current value {@code ==} the expected value.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful. False return indicates that
     *         the current value at the memory location was not equal to the expected value.
     */
    boolean compareAndSwapLong(long offsetBytes, long expect, long update);

    /**
     * Atomically exchanges the given value with the current value located at offsetBytes.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param newValue new value
     * @return the previous value
     */
    long getAndSetLong(long offsetBytes, long newValue);

    // OTHER WRITE METHODS
    /**
     * Returns the primitive backing array, otherwise null.
     * 
     * @return the primitive backing array, otherwise null.
     */
    Object getArray();

    /**
     * Clears all bytes of this Memory to zero
     */
    void clear();

    /**
     * Clears a portion of this Memory to zero.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param lengthBytes the length in bytes
     */
    void clear(long offsetBytes, long lengthBytes);

    /**
     * Clears the bits defined by the bitMask
     * 
     * @param offsetBytes offset bytes relative to this Memory start.
     * @param bitMask the bits set to one will be cleared
     */
    void clearBits(long offsetBytes, byte bitMask);

    /**
     * Fills all bytes of this Memory region to the given byte value.
     * 
     * @param value the given byte value
     */
    void fill(byte value);

    /**
     * Fills a portion of this Memory region to the given byte value.
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param lengthBytes the length in bytes
     * @param value the given byte value
     */
    void fill(long offsetBytes, long lengthBytes, byte value);

    /**
     * Sets the bits defined by the bitMask
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param bitMask the bits set to one will be set
     */
    void setBits(long offsetBytes, byte bitMask);

    //OTHER WRITABLE API METHODS
    /**
     * WritableMemory enables this for ByteBuffer, Heap and Direct Memory backed resources.
     * Map backed resources will always return null.
     * Gets the MemoryRequestServer object, if set, for the above resources to request additional memory.
     * The user must customize the actions of the MemoryRequestServer by
     * implementing the MemoryRequestServer interface and set using one of these methods:
     * <ul><li>{@link MemoryFactory#allocateDirect(long, ByteOrder, MemoryRequestServer)}</li>
     * <li>{@link MemoryFactory#allocate(int, ByteOrder, MemoryRequestServer)}</li>
     * <li>{@link MemoryFactory#writableWrap(ByteBuffer, ByteOrder, MemoryRequestServer)}</li>
     * </ul>
     * Simple implementation examples include the DefaultMemoryRequestServer in the main tree, as well as
     * the ExampleMemoryRequestServerTest and the use with ByteBuffer documented in the DruidIssue11544Test
     * in the test tree.
     * @return the MemoryRequestServer object or null.
     */
    MemoryRequestServer getMemoryRequestServer();
}
