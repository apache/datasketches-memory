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
 * Factory interface for creating various Buffer objects
 */
public interface BufferFactory {
    MemoryRequestServer getDefaultMemoryRequestServer();
    
    //BYTE BUFFER
    /**
     * Accesses the given ByteBuffer for read-only operations. The returned Buffer object has the
     * same byte order, as the given ByteBuffer.
     * @param byteBuffer the given ByteBuffer, must not be null.
     * @return a new Buffer for read-only operations on the given ByteBuffer.
     */
    default Buffer wrap(ByteBuffer byteBuffer) {
      return wrap(byteBuffer, byteBuffer.order());
    }

    /**
     * Accesses the given <i>ByteBuffer</i> for write operations. The returned <i>WritableBuffer</i> object has
     * the same byte order, as the given <i>ByteBuffer</i>.
     * @param byteBuf the given ByteBuffer. It must be non-null and with capacity &ge; 0.
     * @return a new <i>WritableBuffer</i> for write operations on the given <i>ByteBuffer</i>.
     */
    default WritableBuffer writableWrap(ByteBuffer byteBuf) {
      return writableWrap(byteBuf, byteBuf.order(), getDefaultMemoryRequestServer());
    }

    /**
     * Accesses the given ByteBuffer for read-only operations. The returned Buffer object has
     * the given byte order, ignoring the byte order of the given ByteBuffer.
     * @param byteBuffer the given ByteBuffer, must not be null
     * @param byteOrder the byte order to be used, which may be independent of the byte order
     * state of the given ByteBuffer
     * @return a new Buffer for read-only operations on the given ByteBuffer.
     */
    Buffer wrap(ByteBuffer byteBuffer, ByteOrder byteOrder);

    /**
     * Accesses the given <i>ByteBuffer</i> for write operations. The returned <i>WritableBuffer</i> object has
     * the given byte order, ignoring the byte order of the given <i>ByteBuffer</i> for future writes and following reads.
     * However, this does not change the byte order of data already in the <i>ByteBuffer</i>.
     * @param byteBuf the given ByteBuffer. It must be non-null and with capacity &ge; 0.
     * @param byteOrder the byte order to be used.
     * @param memReqSvr A user-specified <i>MemoryRequestServer</i>, which must not be null.
     * This is a callback mechanism for a user client to request a larger <i>WritableBuffer</i>.
     * @return a new <i>WritableBuffer</i> for write operations on the given <i>ByteBuffer</i>.
     */
    WritableBuffer writableWrap(ByteBuffer byteBuf, ByteOrder byteOrder, MemoryRequestServer memReqSvr);
}
