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
