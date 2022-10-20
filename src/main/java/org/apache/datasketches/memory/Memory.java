/**
 * Copyright 2022 Yahoo Inc. All rights reserved.
 */
package org.apache.datasketches.memory;

import org.apache.datasketches.memory.internal.Utf8CodingException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

/**
 * Read API for offset access to a memory resource.
 */
public interface Memory extends Resource {
    /**
     * A region is a readable view subset of this object.
     * <ul>
     * <li>Returned object's origin = this object's origin + offsetBytes</li>
     * <li>Returned object's capacity = capacityBytes</li>
     * </ul>
     * 
     * @param offsetBytes the starting offset with respect to the origin of this <i>Memory</i>. It must be &ge; 0.
     * @param capacityBytes the capacity of the region in bytes. It must be &ge; 0.
     * @return a new <i>Memory</i> representing the defined region based on the given
     *         offsetBytes and capacityBytes.
     */
    default Memory region(long offsetBytes, long capacityBytes) {
        return region(offsetBytes, capacityBytes, ByteOrder.nativeOrder());
    }

    /**
     * A region is a read view of this object.
     * <ul>
     * <li>Returned object's origin = this object's origin + <i>offsetBytes</i></li>
     * <li>Returned object's capacity = <i>capacityBytes</i></li>
     * <li>Returned object's byte order = <i>byteOrder</i></li>
     * </ul>
     * 
     * @param offsetBytes the starting offset with respect to the origin of this Memory. It must be &ge; 0.
     * @param capacityBytes the capacity of the region in bytes. It must be &ge; 0.
     * @param byteOrder the given byte order. It must be non-null.
     * @return a new <i>Memory</i> representing the defined region based on the given
     *         offsetBytes, capacityBytes and byteOrder.
     */
    Memory region(long offsetBytes, long capacityBytes, ByteOrder byteOrder);

    // AS BUFFER
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
     * 
     * @return a new <i>Buffer</i>
     */
    default Buffer asBuffer() {
        return asBuffer(ByteOrder.nativeOrder());
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
     * 
     * @param byteOrder the given byte order
     * @return a new <i>Buffer</i> with the given byteOrder.
     */
    Buffer asBuffer(ByteOrder byteOrder);

    // PRIMITIVE getX() and getXArray()
    /**
     * Gets the boolean value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the boolean at the given offset
     */
    boolean getBoolean(long offsetBytes);

    /**
     * Gets the boolean array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetBooleans offset in array units
     * @param lengthBooleans number of array units to transfer
     */
    void getBooleanArray(long offsetBytes, boolean[] dstArray, int dstOffsetBooleans, int lengthBooleans);

    /**
     * Gets the byte value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the byte at the given offset
     */
    byte getByte(long offsetBytes);

    /**
     * Gets the byte array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetBytes offset in array units
     * @param lengthBytes number of array units to transfer
     */
    void getByteArray(long offsetBytes, byte[] dstArray, int dstOffsetBytes, int lengthBytes);

    /**
     * Gets the char value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the char at the given offset
     */
    char getChar(long offsetBytes);

    /**
     * Gets the char array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetChars offset in array units
     * @param lengthChars number of array units to transfer
     */
    void getCharArray(long offsetBytes, char[] dstArray, int dstOffsetChars, int lengthChars);

    /**
     * Gets UTF-8 encoded bytes from this Memory, starting at offsetBytes to a length of
     * utf8LengthBytes, decodes them into characters and appends them to the given Appendable.
     * This is specifically designed to reduce the production of intermediate objects (garbage),
     * thus significantly reducing pressure on the JVM Garbage Collector.
     * 
     * @param offsetBytes offset bytes relative to the Memory start
     * @param utf8LengthBytes the number of encoded UTF-8 bytes to decode. It is assumed that the
     *        caller has the correct number of utf8 bytes required to decode the number of characters
     *        to be appended to dst. Characters outside the ASCII range can require 2, 3 or 4 bytes per
     *        character to decode.
     * @param dst the destination Appendable to append the decoded characters to.
     * @return the number of characters decoded
     * @throws IOException if dst.append() throws IOException
     * @throws Utf8CodingException in case of malformed or illegal UTF-8 input
     */
    int getCharsFromUtf8(long offsetBytes, int utf8LengthBytes, Appendable dst) throws IOException, Utf8CodingException;

    /**
     * Gets UTF-8 encoded bytes from this Memory, starting at offsetBytes to a length of
     * utf8LengthBytes, decodes them into characters and appends them to the given StringBuilder.
     * This method does *not* reset the length of the destination StringBuilder before appending
     * characters to it.
     * This is specifically designed to reduce the production of intermediate objects (garbage),
     * thus significantly reducing pressure on the JVM Garbage Collector.
     * 
     * @param offsetBytes offset bytes relative to the Memory start
     * @param utf8LengthBytes the number of encoded UTF-8 bytes to decode. It is assumed that the
     *        caller has the correct number of utf8 bytes required to decode the number of characters
     *        to be appended to dst. Characters outside the ASCII range can require 2, 3 or 4 bytes per
     *        character to decode.
     * @param dst the destination StringBuilder to append decoded characters to.
     * @return the number of characters decoded.
     * @throws Utf8CodingException in case of malformed or illegal UTF-8 input
     */
    int getCharsFromUtf8(long offsetBytes, int utf8LengthBytes, StringBuilder dst) throws Utf8CodingException;

    /**
     * Gets the double value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the double at the given offset
     */
    double getDouble(long offsetBytes);

    /**
     * Gets the double array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetDoubles offset in array units
     * @param lengthDoubles number of array units to transfer
     */
    void getDoubleArray(long offsetBytes, double[] dstArray, int dstOffsetDoubles, int lengthDoubles);

    /**
     * Gets the float value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the float at the given offset
     */
    float getFloat(long offsetBytes);

    /**
     * Gets the float array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetFloats offset in array units
     * @param lengthFloats number of array units to transfer
     */
    void getFloatArray(long offsetBytes, float[] dstArray, int dstOffsetFloats, int lengthFloats);

    /**
     * Gets the int value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the int at the given offset
     */
    int getInt(long offsetBytes);

    /**
     * Gets the int array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetInts offset in array units
     * @param lengthInts number of array units to transfer
     */
    void getIntArray(long offsetBytes, int[] dstArray, int dstOffsetInts, int lengthInts);

    /**
     * Gets the long value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the long at the given offset
     */
    long getLong(long offsetBytes);

    /**
     * Gets the long array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetLongs offset in array units
     * @param lengthLongs number of array units to transfer
     */
    void getLongArray(long offsetBytes, long[] dstArray, int dstOffsetLongs, int lengthLongs);

    /**
     * Gets the short value at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @return the short at the given offset
     */
    short getShort(long offsetBytes);

    /**
     * Gets the short array at the given offset
     * 
     * @param offsetBytes offset bytes relative to this Memory start
     * @param dstArray The preallocated destination array.
     * @param dstOffsetShorts offset in array units
     * @param lengthShorts number of array units to transfer
     */
    void getShortArray(long offsetBytes, short[] dstArray, int dstOffsetShorts, int lengthShorts);

    // SPECIAL PRIMITIVE READ METHODS: compareTo, copyTo, writeTo
    /**
     * Compares the bytes of this Memory to <i>that</i> Memory.
     * Returns <i>(this &lt; that) ? (some negative value) : (this &gt; that) ? (some positive value)
     * : 0;</i>.
     * If all bytes are equal up to the shorter of the two lengths, the shorter length is considered
     * to be less than the other.
     * 
     * @param thisOffsetBytes the starting offset for <i>this Memory</i>
     * @param thisLengthBytes the length of the region to compare from <i>this Memory</i>
     * @param that the other Memory to compare with
     * @param thatOffsetBytes the starting offset for <i>that Memory</i>
     * @param thatLengthBytes the length of the region to compare from <i>that Memory</i>
     * @return <i>(this &lt; that) ? (some negative value) : (this &gt; that) ? (some positive value)
     *         : 0;</i>
     */
    int compareTo(long thisOffsetBytes, long thisLengthBytes, Memory that, long thatOffsetBytes, long thatLengthBytes);

    /**
     * Copies bytes from a source range of this Memory to a destination range of the given Memory
     * with the same semantics when copying between overlapping ranges of bytes as method
     * {@link java.lang.System#arraycopy(Object, int, Object, int, int)} has. However, if the source
     * and the destination ranges are exactly the same, this method throws {@link
     * IllegalArgumentException}, because it should never be needed in real-world scenarios and
     * therefore indicates a bug.
     * 
     * @param srcOffsetBytes the source offset for this Memory
     * @param destination the destination Memory, which may not be Read-Only.
     * @param dstOffsetBytes the destination offset
     * @param lengthBytes the number of bytes to copy
     */
    void copyTo(long srcOffsetBytes, WritableMemory destination, long dstOffsetBytes, long lengthBytes);

    /**
     * Writes bytes from a source range of this Memory to the given {@code WritableByteChannel}.
     * 
     * @param offsetBytes the source offset for this Memory
     * @param lengthBytes the number of bytes to copy
     * @param out the destination WritableByteChannel
     * @throws IOException may occur while writing to the WritableByteChannel
     */
    void writeTo(long offsetBytes, long lengthBytes, WritableByteChannel out) throws IOException;

}
