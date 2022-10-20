/**
 * Copyright 2022 Yahoo Inc. All rights reserved.
 */
package org.apache.datasketches.memory;

import org.apache.datasketches.memory.internal.BaseWritableBufferImpl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static org.apache.datasketches.memory.internal.Util.negativeCheck;

/**
 * 
 */
public class DefaultBufferFactory implements BufferFactory {
    
    public static final BufferFactory DEFAULT = new DefaultBufferFactory(new DefaultMemoryRequestServer());
    
    private final MemoryRequestServer memoryRequestServer;
    
    public DefaultBufferFactory(MemoryRequestServer memoryRequestServer) {
        this.memoryRequestServer = memoryRequestServer;
    }
    
    @Override
    public Buffer wrap(ByteBuffer byteBuffer, ByteOrder byteOrder) {
      Objects.requireNonNull(byteBuffer, "byteBuffer must not be null");
      Objects.requireNonNull(byteOrder, "byteOrder must not be null");
      negativeCheck(byteBuffer.capacity(), "byteBuffer");
      return BaseWritableBufferImpl.wrapByteBuffer(byteBuffer, true, byteOrder, null);
    }

    @Override
    public WritableBuffer writableWrap(ByteBuffer byteBuf, ByteOrder byteOrder, MemoryRequestServer memReqSvr) {
      Objects.requireNonNull(byteBuf, "ByteBuffer 'byteBuf' must not be null");
      Objects.requireNonNull(byteOrder, "ByteOrder 'byteOrder' must not be null");
      negativeCheck(byteBuf.capacity(), "byteBuf.capacity");
      if (byteBuf.isReadOnly()) {
        throw new IllegalArgumentException("Cannot create a WritableBuffer from a ReadOnly ByteBuffer.");
      }
      return BaseWritableBufferImpl.wrapByteBuffer(byteBuf, false, byteOrder, memReqSvr);
    }

    @Override
    public MemoryRequestServer getDefaultMemoryRequestServer() {
        return memoryRequestServer;
    }

}
