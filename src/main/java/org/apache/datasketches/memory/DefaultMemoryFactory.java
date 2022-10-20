/**
 * Copyright 2022 Yahoo Inc. All rights reserved.
 */
package org.apache.datasketches.memory;

import org.apache.datasketches.memory.internal.BaseWritableMemoryImpl;
import org.apache.datasketches.memory.internal.unsafe.Prim;
import org.apache.datasketches.memory.internal.unsafe.UnsafeUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static org.apache.datasketches.memory.internal.Util.negativeCheck;

/**
 * 
 */
public class DefaultMemoryFactory implements MemoryFactory {
    
    public final static MemoryFactory DEFAULT = DefaultMemoryRequestServer.DEFAULT.getFactory();
    
    private final MemoryRequestServer memoryRequestServer;
    
    public DefaultMemoryFactory(MemoryRequestServer memoryRequestServer) {
        this.memoryRequestServer = memoryRequestServer;
    }

    @Override
    public Memory wrap(ByteBuffer byteBuffer, ByteOrder byteOrder) {
        Objects.requireNonNull(byteBuffer, "byteBuffer must not be null");
        Objects.requireNonNull(byteOrder, "byteOrder must not be null");
        negativeCheck(byteBuffer.capacity(), "byteBuffer");
        return BaseWritableMemoryImpl.wrapByteBuffer(byteBuffer, true, byteOrder, null);
    }

    @Override
    public WritableMemory writableWrap(ByteBuffer byteBuffer, ByteOrder byteOrder, MemoryRequestServer memReqSvr) {
        Objects.requireNonNull(byteBuffer, "byteBuffer must be non-null");
        Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
        negativeCheck(byteBuffer.capacity(), "byteBuffer");
        if (byteBuffer.isReadOnly()) { throw new IllegalArgumentException("byteBuffer must be writable."); }
        return BaseWritableMemoryImpl.wrapByteBuffer(byteBuffer, false, byteOrder, memReqSvr);
      }
    
    @Override
    public MmapHandle map(File file, long fileOffsetBytes, long capacityBytes, ByteOrder byteOrder) {
        Objects.requireNonNull(file, "file must be non-null.");
        Objects.requireNonNull(byteOrder, "byteOrder must be non-null.");
        if (!file.canRead()) { throw new IllegalArgumentException("file must be readable."); }
        negativeCheck(fileOffsetBytes, "fileOffsetBytes");
        negativeCheck(capacityBytes, "capacityBytes");
        return BaseWritableMemoryImpl.wrapMap(file, fileOffsetBytes, capacityBytes, true, byteOrder);
    }

    @Override
    public WritableMmapHandle writableMap(File file, long fileOffsetBytes, long capacityBytes, ByteOrder byteOrder) {
        Objects.requireNonNull(file, "file must be non-null.");
        Objects.requireNonNull(byteOrder, "byteOrder must be non-null.");
        if (!file.canWrite()) { throw new IllegalArgumentException("file must be writable."); }
        negativeCheck(file.length(), "file.length()");
        negativeCheck(fileOffsetBytes, "fileOffsetBytes");
        negativeCheck(capacityBytes, "capacityBytes");
        return BaseWritableMemoryImpl.wrapMap(file, fileOffsetBytes, capacityBytes, false, byteOrder);
    }
    
    @Override
    public WritableHandle allocateDirect(long capacityBytes, ByteOrder byteOrder, MemoryRequestServer memReqSvr) {
        Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
        negativeCheck(capacityBytes, "capacityBytes");
        return BaseWritableMemoryImpl.wrapDirect(capacityBytes, byteOrder, memReqSvr);
    }
    
    @Override
    public Memory wrap(byte[] array, int offsetBytes, int lengthBytes, ByteOrder byteOrder) {
        Objects.requireNonNull(array, "array must be non-null");
        Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
        negativeCheck(offsetBytes, "offsetBytes");
        negativeCheck(lengthBytes, "lengthBytes");
        UnsafeUtil.checkBounds(offsetBytes, lengthBytes, array.length);
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }

    @Override
    public WritableMemory writableWrap(byte[] array, int offsetBytes, int lengthBytes, ByteOrder byteOrder,
            MemoryRequestServer memReqSvr) {
        Objects.requireNonNull(array, "array must be non-null");
        Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
        negativeCheck(offsetBytes, "offsetBytes");
        negativeCheck(lengthBytes, "lengthBytes");
        UnsafeUtil.checkBounds(offsetBytes, lengthBytes, array.length);
        return BaseWritableMemoryImpl.wrapHeapArray(array, offsetBytes, lengthBytes, false, byteOrder, memReqSvr);
      }

    @Override
    public Memory wrap(boolean[] array) {
        Objects.requireNonNull(array, "array must be non-null");
        final long lengthBytes = array.length << Prim.BOOLEAN.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public WritableMemory writableWrap(boolean[] array) {
        Objects.requireNonNull(array, "array must be non-null");
        final long lengthBytes = array.length << Prim.BOOLEAN.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }

    @Override
    public Memory wrap(char[] array) {
        Objects.requireNonNull(array, "array must be non-null");
        final long lengthBytes = array.length << Prim.CHAR.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public WritableMemory writableWrap(char[] array) {
        Objects.requireNonNull(array, "array must be non-null");
        final long lengthBytes = array.length << Prim.CHAR.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }

    @Override
    public Memory wrap(short[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.SHORT.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public WritableMemory writableWrap(short[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.SHORT.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public Memory wrap(int[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.INT.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }

    @Override
    public WritableMemory writableWrap(int[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.INT.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }

    @Override
    public Memory wrap(long[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.LONG.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }

    @Override
    public WritableMemory writableWrap(long[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.LONG.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public Memory wrap(float[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.FLOAT.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public WritableMemory writableWrap(float[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.FLOAT.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }

    @Override
    public Memory wrap(double[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.DOUBLE.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, true, ByteOrder.nativeOrder(), null);
    }
    
    @Override
    public WritableMemory writableWrap(double[] array) {
        Objects.requireNonNull(array, "arr must be non-null");
        final long lengthBytes = array.length << Prim.DOUBLE.shift();
        return BaseWritableMemoryImpl.wrapHeapArray(array, 0L, lengthBytes, false, ByteOrder.nativeOrder(), null);
    }

    @Override
    public MemoryRequestServer getDefaultMemoryRequestServer() {
        return memoryRequestServer;
    }

}
