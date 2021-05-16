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

import org.apache.datasketches.memory.internal.WritableMemory;

/**
 * The MemoryRequestServer is a callback interface to provide a means for a direct (off-heap),
 * dynamic WritableMemory object to request more memory from the owner of the
 * {@link WritableDirectHandle}. Refer to {@link DefaultMemoryRequestServer} for how this can be
 * used.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request new WritableMemory with the given capacity.
   * @param capacityBytes The capacity being requested.
   * @return new WritableMemory with the given capacity.
   */
  WritableMemory request(long capacityBytes);

  /**
   * Request close the AutoCloseable resource.
   * This may be ignored depending on the application implementation.
   * @param memToClose the relevant WritbleMemory to be considered for closing.
   * @param newMemory the newly allocated WritableMemory. This is returned from the client
   * for the convenience of the resource owner. It is optional and may be null.
   */
  void requestClose(final WritableMemory memToClose, WritableMemory newMemory);

}
