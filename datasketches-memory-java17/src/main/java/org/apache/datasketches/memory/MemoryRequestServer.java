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

/**
 * The MemoryRequestServer is a callback interface to provide a means for direct (off-heap), heap and ByteBuffer
 * backed resources to request more memory.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request new WritableMemory with the given capacity. The current Writable Memory will be used to
   * determine the byte order of the returned WritableMemory and other checks.
   * @param currentWritableMemory the current writableMemory of the client. It must be non-null.
   * @param capacityBytes The capacity being requested. It must be &ge; 0.
   *
   * @return new WritableMemory with the given capacity.
   */
  WritableMemory request(WritableMemory currentWritableMemory, long capacityBytes);

  /**
   * Request close the AutoCloseable resource. This only applies to resources allocated using
   * WritableMemory.allocateDirect(...).
   * This may be ignored depending on the application implementation.
   * @param memToClose the relevant WritbleMemory to be considered for closing. It must be non-null.
   * @param newMemory the newly allocated WritableMemory. It must be non-null.
   * This is returned from the client to facilitate tracking for the convenience of the resource owner.
   */
  void requestClose(final WritableMemory memToClose, WritableMemory newMemory);

}
