/**
 * Copyright 2022 Yahoo Inc. All rights reserved.
 */
package org.apache.datasketches.memory;

import org.apache.datasketches.memory.internal.Util;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Factory interface for creating various Memory objects
 */
public interface MemoryFactory {
    
    MemoryRequestServer getDefaultMemoryRequestServer();
    
    //BYTE BUFFER

    /**
     * Accesses the given <i>ByteBuffer</i> for read-only operations. The returned <i>Memory</i> object has
     * the same byte order, as the given <i>ByteBuffer</i>.
     * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null and with capacity &ge; 0.
     * @return a new <i>Memory</i> for read-only operations on the given <i>ByteBuffer</i>.
     */
    default Memory wrap(ByteBuffer byteBuffer) {
      return wrap(byteBuffer, byteBuffer.order());
    }

    /**
     * Accesses the given <i>ByteBuffer</i> for write operations. The returned <i>WritableMemory</i> object has
     * the same byte order, as the given <i>ByteBuffer</i>.
     * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null, with capacity &ge; 0, and writable.
     * @return a new <i>WritableMemory</i> for write operations on the given <i>ByteBuffer</i>.
     */
    default WritableMemory writableWrap(ByteBuffer byteBuffer) {
      return writableWrap(byteBuffer, byteBuffer.order());
    }

    /**
     * Accesses the given <i>ByteBuffer</i> for read-only operations. The returned <i>Memory</i> object has
     * the given byte order, ignoring the byte order of the given <i>ByteBuffer</i> for future reads and writes.
     * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null and with capacity &ge; 0.
     * @param byteOrder the byte order to be used.  It must be non-null.
     * @return a new <i>Memory</i> for read-only operations on the given <i>ByteBuffer</i>.
     */
    Memory wrap(ByteBuffer byteBuffer, ByteOrder byteOrder);

    /**
     * Accesses the given <i>ByteBuffer</i> for write operations. The returned <i>WritableMemory</i> object has
     * the given byte order, ignoring the byte order of the given <i>ByteBuffer</i> for future writes and following reads.
     * However, this does not change the byte order of data already in the <i>ByteBuffer</i>.
     * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null, with capacity &ge; 0, and writable.
     * @param byteOrder the byte order to be used. It must be non-null.
     * @return a new <i>WritableMemory</i> for write operations on the given <i>ByteBuffer</i>.
     */
    default WritableMemory writableWrap(ByteBuffer byteBuffer, ByteOrder byteOrder) {
        return writableWrap(byteBuffer, byteOrder, getDefaultMemoryRequestServer());
    }
    
    /**
     * Accesses the given <i>ByteBuffer</i> for write operations. The returned <i>WritableMemory</i> object has
     * the given byte order, ignoring the byte order of the given <i>ByteBuffer</i> for future reads and writes.
     * However, this does not change the byte order of data already in the <i>ByteBuffer</i>.
     * @param byteBuffer the given <i>ByteBuffer</i>. It must be non-null, with capacity &ge; 0, and writable.
     * @param byteOrder the byte order to be used. It must be non-null.
     * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which may be null.
     * This is a callback mechanism for a user client to request a larger <i>WritableMemory</i>.
     * @return a new <i>WritableMemory</i> for write operations on the given <i>ByteBuffer</i>.
     */
    WritableMemory writableWrap(ByteBuffer byteBuffer, ByteOrder byteOrder, MemoryRequestServer memReqSvr);

    //MAP
    /**
     * Maps the entire given file into native-ordered <i>Memory</i> for read operations
     * Calling this method is equivalent to calling
     * {@link #map(File, long, long, ByteOrder) map(file, 0, file.length(), ByteOrder.nativeOrder())}.
     * @param file the given file to map. It must be non-null, length &ge; 0, and readable.
     * @return <i>MmapHandle</i> for managing the mapped memory.
     * Please read Javadocs for {@link Handle}.
     */
    default MmapHandle map(File file) {
      return map(file, 0, file.length(), ByteOrder.nativeOrder());
    }

    /**
     * Maps the entire given file into native-ordered WritableMemory for write operations
     * Calling this method is equivalent to calling
     * {@link #writableMap(File, long, long, ByteOrder) writableMap(file, 0, file.length(), ByteOrder.nativeOrder())}.
     * @param file the given file to map. It must be non-null, with length &ge; 0, and writable.
     * @return WritableMmapHandle for managing the mapped Memory.
     * Please read Javadocs for {@link Handle}.
     */
    default WritableMmapHandle writableMap(File file) {
      return writableMap(file, 0, file.length(), ByteOrder.nativeOrder());
    }
    
    /**
     * Maps the specified portion of the given file into <i>Memory</i> for read operations.
     * @param file the given file to map. It must be non-null and readable.
     * @param fileOffsetBytes the position in the given file in bytes. It must not be negative.
     * @param capacityBytes the size of the mapped memory. It must not be negative.
     * @param byteOrder the byte order to be used for the mapped memory. It must be non-null.
     * @return <i>MmapHandle</i> for managing the mapped memory.
     * Please read Javadocs for {@link Handle}.
     */
     MmapHandle map(File file, long fileOffsetBytes, long capacityBytes, ByteOrder byteOrder);

     /**
      * Maps the specified portion of the given file into Memory for write operations.
      *
      * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
      * <i>WritableMemory.map(...)</i>.
      * @param file the given file to map. It must be non-null, writable and length &ge; 0.
      * @param fileOffsetBytes the position in the given file in bytes. It must not be negative.
      * @param capacityBytes the size of the mapped Memory. It must not be negative.
      * @param byteOrder the byte order to be used for the given file. It must be non-null.
      * @return WritableMapHandle for managing the mapped Memory.
      * Please read Javadocs for {@link Handle}.
      */
     WritableMmapHandle writableMap(File file, long fileOffsetBytes, long capacityBytes, ByteOrder byteOrder) ;
     
     //ALLOCATE HEAP VIA AUTOMATIC BYTE ARRAY
     /**
      * Creates on-heap WritableMemory with the given capacity and the native byte order.
      * @param capacityBytes the given capacity in bytes. It must be &ge; 0.
      * @return a new WritableMemory for write operations on a new byte array.
      */
     default WritableMemory allocate(int capacityBytes) {
       return allocate(capacityBytes, ByteOrder.nativeOrder());
     }

     /**
      * Creates on-heap WritableMemory with the given capacity and the given byte order.
      * @param capacityBytes the given capacity in bytes. It must be &ge; 0.
      * @param byteOrder the given byte order to allocate new Memory object with. It must be non-null.
      * @return a new WritableMemory for write operations on a new byte array.
      */
     default WritableMemory allocate(int capacityBytes, ByteOrder byteOrder) {
       return allocate(capacityBytes, byteOrder, getDefaultMemoryRequestServer());
     }

     /**
      * Creates on-heap WritableMemory with the given capacity and the given byte order.
      * @param capacityBytes the given capacity in bytes. It must be &ge; 0.
      * @param byteOrder the given byte order to allocate new Memory object with. It must be non-null.
      * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which may be null.
      * This is a callback mechanism for a user client to request a larger <i>WritableMemory</i>.
      * @return a new WritableMemory for write operations on a new byte array.
      */
     default WritableMemory allocate(int capacityBytes, ByteOrder byteOrder, MemoryRequestServer memReqSvr) {
         final byte[] arr = new byte[capacityBytes];
         Util.negativeCheck(capacityBytes, "capacityBytes");
         return writableWrap(arr, 0, capacityBytes, byteOrder, memReqSvr);
     }

     //ALLOCATE DIRECT
     /**
      * Allocates and provides access to capacityBytes directly in native (off-heap) memory.
      * Native byte order is assumed.
      * The allocated memory will be 8-byte aligned, but may not be page aligned.
      *
      * <p><b>NOTE:</b> Native/Direct memory acquired may have garbage in it.
      * It is the responsibility of the using application to clear this memory, if required,
      * and to call <i>close()</i> when done.</p>
      *
      * @param capacityBytes the size of the desired memory in bytes. It must be &ge; 0.
      * @return WritableHandle for this off-heap resource.
      * Please read Javadocs for {@link Handle}.
      */
     default WritableHandle allocateDirect(long capacityBytes) {
       return allocateDirect(capacityBytes, ByteOrder.nativeOrder(), getDefaultMemoryRequestServer());
     }

     /**
      * Allocates and provides access to capacityBytes directly in native (off-heap) memory.
      * The allocated memory will be 8-byte aligned, but may not be page aligned.
      *
      * <p><b>NOTE:</b> Native/Direct memory acquired may have garbage in it.
      * It is the responsibility of the using application to clear this memory, if required,
      * and to call <i>close()</i> when done.</p>
      *
      * @param capacityBytes the size of the desired memory in bytes. It must be &ge; 0.
      * @param byteOrder the given byte order. It must be non-null.
      * @param memReqSvr A user-specified MemoryRequestServer, which may be null.
      * This is a callback mechanism for a user client of direct memory to request more memory.
      * @return WritableHandle for this off-heap resource.
      * Please read Javadocs for {@link Handle}.
      */
     WritableHandle allocateDirect(long capacityBytes, ByteOrder byteOrder, MemoryRequestServer memReqSvr);
     
    //ACCESS PRIMITIVE HEAP ARRAYS
    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    default Memory wrap(byte[] array) {
      Objects.requireNonNull(array, "array must be non-null");
      return wrap(array, 0, array.length, ByteOrder.nativeOrder());
    }

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     *
     * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
     * <i>WritableMemory.wrap(...)</i>.
     * @param array the given primitive array. It must be non-null.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    default WritableMemory writableWrap(byte[] array) {
      return writableWrap(array, 0, array.length, ByteOrder.nativeOrder());
    }

    /**
     * Wraps the given primitive array for read operations with the given byte order.
     * @param array the given primitive array.
     * @param byteOrder the byte order to be used
     * @return a new <i>Memory</i> for read operations
     */
    default Memory wrap(byte[] array, ByteOrder byteOrder) {
      return wrap(array, 0, array.length, byteOrder);
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
    default WritableMemory writableWrap(byte[] array, ByteOrder byteOrder) {
      return writableWrap(array, 0, array.length, byteOrder, getDefaultMemoryRequestServer());
    }

    /**
     * Wraps the given primitive array for read operations with the given byte order.
     * @param array the given primitive array.
     * @param offsetBytes the byte offset into the given array
     * @param lengthBytes the number of bytes to include from the given array
     * @param byteOrder the byte order to be used
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(byte[] array, int offsetBytes, int lengthBytes, ByteOrder byteOrder);

    /**
     * Wraps the given primitive array for write operations with the given byte order.
     *
     * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
     * <i>WritableMemory.wrap(...)</i>.
     * @param array the given primitive array. It must be non-null.
     * @param offsetBytes the byte offset into the given array. It must be &ge; 0.
     * @param lengthBytes the number of bytes to include from the given array. It must be &ge; 0.
     * @param byteOrder the byte order to be used. It must be non-null.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    default WritableMemory writableWrap(byte[] array, int offsetBytes, int lengthBytes, ByteOrder byteOrder) {
      return writableWrap(array, offsetBytes, lengthBytes, byteOrder, getDefaultMemoryRequestServer());
    }

    /**
     * Wraps the given primitive array for write operations with the given byte order. If the given
     * lengthBytes is zero, backing storage, byte order and read-only status of the returned
     * WritableMemory object are unspecified.
     *
     * <p><b>Note:</b> Always qualify this method with the class name, e.g.,
     * <i>WritableMemory.wrap(...)</i>.
     * @param array the given primitive array. It must be non-null.
     * @param offsetBytes the byte offset into the given array. It must be &ge; 0.
     * @param lengthBytes the number of bytes to include from the given array. It must be &ge; 0.
     * @param byteOrder the byte order to be used. It must be non-null.
     * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which may be null.
     * This is a callback mechanism for a user client to request a larger <i>WritableMemory</i>.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(byte[] array, int offsetBytes, int lengthBytes, ByteOrder byteOrder, 
            MemoryRequestServer memReqSvr);

    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(boolean[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array. It must be non-null.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(boolean[] array);

    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(char[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(char[] array);

    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(short[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(short[] array);

    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(int[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(int[] array);
    
    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(long[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(long[] array);

    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(float[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(float[] array);
    
    /**
     * Wraps the given primitive array for read operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new <i>Memory</i> for read operations
     */
    Memory wrap(double[] array);

    /**
     * Wraps the given primitive array for write operations assuming native byte order.
     * @param array the given primitive array.
     * @return a new WritableMemory for write operations on the given primitive array.
     */
    WritableMemory writableWrap(double[] array);
}
