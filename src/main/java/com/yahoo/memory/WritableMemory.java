/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.Util.nativeOrder;
import static com.yahoo.memory.Util.negativeCheck;
import static com.yahoo.memory.Util.nullCheck;
import static com.yahoo.memory.Util.zeroCheck;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides read and write primitive and primitive array access to any of the four resources
 * mentioned at the package level.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public abstract class WritableMemory extends Memory {

  //Pass-through ctor
  WritableMemory(final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  //BYTE BUFFER
  /**
   * Accesses the given ByteBuffer for write operations. The returned WritableMemory object has
   * the same byte order, as the given ByteBuffer, unless the capacity of the given ByteBuffer is
   * zero, then byte order of the returned WritableMemory object, as well as backing storage and
   * read-only status are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param byteBuf the given ByteBuffer
   * @return a new WritableMemory for write operations on the given ByteBuffer.
   */
  public static WritableMemory wrap(final ByteBuffer byteBuf) {
    return BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, false, byteBuf.order());
  }

  /**
   * Accesses the given ByteBuffer for write operations. The returned WritableMemory object has
   * the given byte order, ignoring the byte order of the given ByteBuffer. If the capacity of
   * the given ByteBuffer is zero the byte order of the returned WritableMemory object
   * (as well as backing storage) is unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param byteBuf the given ByteBuffer, must not be null
   * @param byteOrder the byte order to be used, which may be independent of the byte order
   * state of the given ByteBuffer
   * @return a new WritableMemory for write operations on the given ByteBuffer.
   */
  public static WritableMemory wrap(final ByteBuffer byteBuf, final ByteOrder byteOrder) {
    return BaseWritableMemoryImpl.wrapByteBuffer(byteBuf, false, byteOrder);
  }

  //MAP
  /**
   * Maps the entire given file into native-ordered Memory for write operations
   * (including those &gt; 2GB). Calling this method is equivalent to calling
   * {@link #map(File, long, long, ByteOrder) map(file, 0, file.length(), ByteOrder.nativeOrder())}.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.map(...)</i>.
   * @param file the given file to map
   * @return WritableMapHandle for managing the mapped Memory
   * @throws IOException file not found or a RuntimeException.
   */
  public static WritableMapHandle map(final File file) throws IOException {
    return map(file, 0, file.length(), nativeOrder);
  }

  /**
   * Maps the specified portion of the given file into Memory for write operations
   * (including those &gt; 2GB).
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.map(...)</i>.
   * @param file the given file to map. It may not be null.
   * @param fileOffsetBytes the position in the given file in bytes. It may not be negative.
   * @param capacityBytes the size of the mapped Memory. It may not be negative or zero.
   * @param byteOrder the byte order to be used for the given file. It may not be null.
   * @return WritableMapHandle for managing the mapped Memory
   * @throws IOException file not found or RuntimeException, etc.
   */
  public static WritableMapHandle map(final File file, final long fileOffsetBytes,
      final long capacityBytes, final ByteOrder byteOrder) throws IOException {
    zeroCheck(capacityBytes, "Capacity");
    nullCheck(file, "file is null");
    negativeCheck(fileOffsetBytes, "File offset is negative");
    return BaseWritableMemoryImpl
        .wrapMap(file, fileOffsetBytes, capacityBytes, false, byteOrder);
  }

  //ALLOCATE DIRECT
  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the WritableMemory API. Native byte order is assumed.
   * The allocated memory will be 8-byte aligned, but may not be page aligned.
   * If capacityBytes is zero, byte order, backing storage and read-only status
   * of the WritableMemory object, returned from {@link WritableHandle#get()} are unspecified.
   *
   * <p>The default MemoryRequestServer, which allocates any request for memory onto the heap,
   * will be used.</p>
   *
   * <p><b>NOTE:</b> Native/Direct memory acquired using Unsafe may have garbage in it.
   * It is the responsibility of the using class to clear this memory, if required,
   * and to call <i>close()</i> when done.</p>
   *
   * @param capacityBytes the size of the desired memory in bytes.
   * @return WritableDirectHandle for this off-heap resource
   */
  public static WritableDirectHandle allocateDirect(final long capacityBytes) {
    return allocateDirect(capacityBytes, null);
  }

  /**
   * Allocates and provides access to capacityBytes directly in native (off-heap) memory
   * leveraging the WritableMemory API. The allocated memory will be 8-byte aligned, but may not
   * be page aligned. If capacityBytes is zero, byte order, backing storage and read-only status
   * of the WritableMemory object, returned from {@link WritableHandle#get()} are unspecified.
   *
   * <p><b>NOTE:</b> Native/Direct memory acquired using Unsafe may have garbage in it.
   * It is the responsibility of the using class to clear this memory, if required,
   * and to call <i>close()</i> when done.</p>
   *
   * @param capacityBytes the size of the desired memory in bytes.
   * @param memReqSvr A user-specified MemoryRequestServer.
   * This is a callback mechanism for a user client of direct memory to request more memory.
   * @return WritableHandle for this off-heap resource
   */
  public static WritableDirectHandle allocateDirect(final long capacityBytes,
      final MemoryRequestServer memReqSvr) {
    return BaseWritableMemoryImpl.wrapDirect(capacityBytes, nativeOrder, memReqSvr);
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
   * If the given capacityBytes is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   *
   * @param offsetBytes the starting offset with respect to this object.
   * @param capacityBytes the capacity of the returned object in bytes.
   * @return a new <i>WritableMemory</i> representing the defined writable region.
   */
  public abstract WritableMemory writableRegion(long offsetBytes, long capacityBytes);

  /**
   * A writable region is a writable view of this object.
   * This returns a new <i>WritableMemory</i> representing the defined writable region with the
   * given offsetBytes, capacityBytes and byte order.
   * <ul>
   * <li>Returned object's origin = this objects' origin + <i>offsetBytes</i></li>
   * <li>Returned object's capacity = <i>capacityBytes</i></li>
   * <li>Returned object's byte order = <i>byteOrder</i></li>
   * </ul>
   * If the given capacityBytes is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   *
   * @param offsetBytes the starting offset with respect to this object.
   * @param capacityBytes the capacity of the returned object in bytes.
   * @param byteOrder the given byte order
   * @return a new <i>WritableMemory</i> representing the defined writable region.
   */
  public abstract WritableMemory writableRegion(long offsetBytes, long capacityBytes,
      ByteOrder byteOrder);

  //AS BUFFER
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
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @return a new <i>WritableBuffer</i> with a view of this WritableMemory
   */
  public abstract WritableBuffer asWritableBuffer();

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
   * If this object's capacity is zero, the returned object is effectively immutable and
   * the backing storage and byte order are unspecified.
   * @param byteOrder the given byte order
   * @return a new <i>WritableBuffer</i> with a view of this WritableMemory
   */
  public abstract WritableBuffer asWritableBuffer(ByteOrder byteOrder);

  //ALLOCATE HEAP VIA AUTOMATIC BYTE ARRAY
  /**
   * Creates on-heap WritableMemory with the given capacity and the native byte order. If the given
   * capacityBytes is zero, backing storage, byte order and read-only status of the returned
   * WritableMemory object are unspecified.
   * @param capacityBytes the given capacity in bytes.
   * @return a new WritableMemory for write operations on a new byte array.
   */
  public static WritableMemory allocate(final int capacityBytes) {
    final byte[] arr = new byte[capacityBytes];
    return wrap(arr, nativeOrder);
  }

  /**
   * Creates on-heap WritableMemory with the given capacity and byte order. If the given
   * capacityBytes is zero, backing storage, byte order and read-only status of the returned
   * WritableMemory object are unspecified.
   * @param capacityBytes the given capacity in bytes.
   * @return a new WritableMemory for write operations on a new byte array.
   */
  public static WritableMemory allocate(final int capacityBytes, final ByteOrder byteOrder) {
    final byte[] arr = new byte[capacityBytes];
    return wrap(arr, byteOrder);
  }

  //ACCESS PRIMITIVE HEAP ARRAYS for write
  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final boolean[] arr) {
    final long lengthBytes = arr.length << Prim.BOOLEAN.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final byte[] arr) {
    return WritableMemory.wrap(arr, 0, arr.length, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations with the given byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @param byteOrder the byte order to be used
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final byte[] arr, final ByteOrder byteOrder) {
    return WritableMemory.wrap(arr, 0, arr.length, byteOrder);
  }

  /**
   * Wraps the given primitive array for write operations with the given byte order. If the given
   * lengthBytes is zero, backing storage, byte order and read-only status of the returned
   * WritableMemory object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @param offsetBytes the byte offset into the given array
   * @param lengthBytes the number of bytes to include from the given array
   * @param byteOrder the byte order to be used
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final byte[] arr, final int offsetBytes, final int lengthBytes,
      final ByteOrder byteOrder) {
    UnsafeUtil.checkBounds(offsetBytes, lengthBytes, arr.length);
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, byteOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final char[] arr) {
    final long lengthBytes = arr.length << Prim.CHAR.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final short[] arr) {
    final long lengthBytes = arr.length << Prim.SHORT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final int[] arr) {
    final long lengthBytes = arr.length << Prim.INT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final long[] arr) {
    final long lengthBytes = arr.length << Prim.LONG.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final float[] arr) {
    final long lengthBytes = arr.length << Prim.FLOAT.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }

  /**
   * Wraps the given primitive array for write operations assuming native byte order. If the array
   * size is zero, backing storage, byte order and read-only status of the returned WritableMemory
   * object are unspecified.
   *
   * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
   * <i>WritableMemory.wrap(...)</i>.
   * @param arr the given primitive array.
   * @return a new WritableMemory for write operations on the given primitive array.
   */
  public static WritableMemory wrap(final double[] arr) {
    final long lengthBytes = arr.length << Prim.DOUBLE.shift();
    return BaseWritableMemoryImpl.wrapHeapArray(arr, 0L, lengthBytes, false, nativeOrder);
  }
  //END OF CONSTRUCTOR-TYPE METHODS

  //PRIMITIVE putX() and putXArray()
  /**
   * Puts the boolean value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putBoolean(long offsetBytes, boolean value);

  /**
   * Puts the boolean array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetBooleans offset in array units
   * @param lengthBooleans number of array units to transfer
   */
  public abstract void putBooleanArray(long offsetBytes, boolean[] srcArray, int srcOffsetBooleans,
          int lengthBooleans);

  /**
   * Puts the byte value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putByte(long offsetBytes, byte value);

  /**
   * Puts the byte array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetBytes offset in array units
   * @param lengthBytes number of array units to transfer
   */
  public abstract void putByteArray(long offsetBytes, byte[] srcArray, int srcOffsetBytes,
          int lengthBytes);

  /**
   * Puts the char value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putChar(long offsetBytes, char value);

  /**
   * Puts the char array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetChars offset in array units
   * @param lengthChars number of array units to transfer
   */
  public abstract void putCharArray(long offsetBytes, char[] srcArray, int srcOffsetChars,
          int lengthChars);

  /**
   * Encodes characters from the given CharSequence into UTF-8 bytes and puts them into this
   * <i>WritableMemory</i> begining at the given offsetBytes.
   * This is specifically designed to reduce the production of intermediate objects (garbage),
   * thus significantly reducing pressure on the JVM Garbage Collector.
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param src The source CharSequence to be encoded and put into this WritableMemory. It is
   * the responsibility of the caller to provide sufficient capacity in this
   * <i>WritableMemory</i> for the encoded Utf8 bytes. Characters outside the ASCII range can
   * require 2, 3 or 4 bytes per character to encode.
   * @return the number of bytes encoded
   */
  public abstract long putCharsToUtf8(long offsetBytes, CharSequence src);

  /**
   * Puts the double value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putDouble(long offsetBytes, double value);

  /**
   * Puts the double array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetDoubles offset in array units
   * @param lengthDoubles number of array units to transfer
   */
  public abstract void putDoubleArray(long offsetBytes, double[] srcArray,
          final int srcOffsetDoubles, final int lengthDoubles);

  /**
   * Puts the float value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putFloat(long offsetBytes, float value);

  /**
   * Puts the float array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetFloats offset in array units
   * @param lengthFloats number of array units to transfer
   */
  public abstract void putFloatArray(long offsetBytes, float[] srcArray,
          final int srcOffsetFloats, final int lengthFloats);

  /**
   * Puts the int value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putInt(long offsetBytes, int value);

  /**
   * Puts the int array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetInts offset in array units
   * @param lengthInts number of array units to transfer
   */
  public abstract void putIntArray(long offsetBytes, int[] srcArray,
          final int srcOffsetInts, final int lengthInts);

  /**
   * Puts the long value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putLong(long offsetBytes, long value);

  /**
   * Puts the long array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetLongs offset in array units
   * @param lengthLongs number of array units to transfer
   */
  public abstract void putLongArray(long offsetBytes, long[] srcArray,
          final int srcOffsetLongs, final int lengthLongs);

  /**
   * Puts the short value at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param value the value to put
   */
  public abstract void putShort(long offsetBytes, short value);

  /**
   * Puts the short array at the given offset
   * @param offsetBytes offset bytes relative to this <i>WritableMemory</i> start
   * @param srcArray The source array.
   * @param srcOffsetShorts offset in array units
   * @param lengthShorts number of array units to transfer
   */
  public abstract void putShortArray(long offsetBytes, short[] srcArray,
          final int srcOffsetShorts, final int lengthShorts);

  //Atomic Methods
  /**
   * Atomically adds the given value to the long located at offsetBytes.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param delta the amount to add
   * @return the the previous value
   */
  public abstract long getAndAddLong(long offsetBytes, long delta);

  /**
   * Atomically sets the current value at the memory location to the given updated value
   * if and only if the current value {@code ==} the expected value.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param expect the expected value
   * @param update the new value
   * @return {@code true} if successful. False return indicates that
   * the current value at the memory location was not equal to the expected value.
   */
  public abstract boolean compareAndSwapLong(long offsetBytes, long expect, long update);

  /**
   * Atomically exchanges the given value with the current value located at offsetBytes.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param newValue new value
   * @return the previous value
   */
  public abstract long getAndSetLong(long offsetBytes, long newValue);

  //OTHER WRITE METHODS
  /**
   * Returns the primitive backing array, otherwise null.
   * @return the primitive backing array, otherwise null.
   */
  public abstract Object getArray();

  /**
   * Clears all bytes of this Memory to zero
   */
  public abstract void clear();

  /**
   * Clears a portion of this Memory to zero.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param lengthBytes the length in bytes
   */
  public abstract void clear(long offsetBytes, long lengthBytes);

  /**
   * Clears the bits defined by the bitMask
   * @param offsetBytes offset bytes relative to this Memory start.
   * @param bitMask the bits set to one will be cleared
   */
  public abstract void clearBits(long offsetBytes, byte bitMask);

  /**
   * Fills all bytes of this Memory region to the given byte value.
   * @param value the given byte value
   */
  public abstract void fill(byte value);

  /**
   * Fills a portion of this Memory region to the given byte value.
   * @param offsetBytes offset bytes relative to this Memory start
   * @param lengthBytes the length in bytes
   * @param value the given byte value
   */
  public abstract void fill(long offsetBytes, long lengthBytes, byte value);

  /**
   * Sets the bits defined by the bitMask
   * @param offsetBytes offset bytes relative to this Memory start
   * @param bitMask the bits set to one will be set
   */
  public abstract void setBits(long offsetBytes, byte bitMask);

  //OTHER WRITABLE API METHODS
  /**
   * For Direct Memory only. Other types of backing resources will return null.
   * Gets the MemoryRequestServer object used by dynamic off-heap (Direct) memory objects
   * to request additional memory.
   * Set using {@link WritableMemory#allocateDirect(long, MemoryRequestServer)}.
   * If not explicity set, this returns the {@link DefaultMemoryRequestServer}.
   * @return the MemoryRequestServer object (if direct memory) or null.
   */
  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
  }

}
