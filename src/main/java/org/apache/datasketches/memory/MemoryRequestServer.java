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

import jdk.incubator.foreign.ResourceScope;

/**
 * The MemoryRequestServer is a callback interface to provide a means to request more memory
 * for heap and off-heap WritableMemory resources that are not file-memory-mapped backed resources.
 *
 * <p>Note: this only works with Java 17.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request new WritableMemory with the given newCapacityBytes. The current WritableMemory can be used to
   * determine the byte order of the returned WritableMemory and other properties. A new confined ResourceScope is
   * assigned.
   * @param currentWritableMemory the current writableMemory of the client. It must be non-null.
   * @param newCapacityBytes The capacity being requested. It must be &gt; the capacity of the currentWritableMemory.
   * @return new WritableMemory with the requested capacity.
   */
  default WritableMemory request(WritableMemory currentWritableMemory, long newCapacityBytes) {
    return request(currentWritableMemory, newCapacityBytes, ResourceScope.newConfinedScope());
  }

  /**
   * Request new WritableMemory with the given newCapacityBytes. The current Writable Memory can be used to
   * determine the byte order of the returned WritableMemory and other properties.
   * @param currentWritableMemory the current writableMemory of the client. It must be non-null.
   * @param newCapacityBytes The capacity being requested. It must be &gt; the capacity of the currentWritableMemory.
   * @param scope the ResourceScope to be used for the newly allocated memory.
   * @return new WritableMemory with the requested capacity.
   */
  WritableMemory request(WritableMemory currentWritableMemory, long newCapacityBytes, ResourceScope scope);

  /**
   * Request to close the resource, if applicable.
   *
   * @param memToClose the relevant WritableMemory to be considered for closing. It must be non-null.
   */
  default void requestClose(WritableMemory memToClose) {
    requestClose(memToClose, null);
  }

  /**
   * Request to close the resource, if applicable.
   *
   * @param memToClose the relevant WritableMemory to be considered for closing. It must be non-null.
   * @param newMemory the newly allocated WritableMemory.
   * The newMemory reference is returned from the client for the convenience of the system that
   * owns the responsibility of memory allocation. It may be null.
   */
  void requestClose(WritableMemory memToClose, WritableMemory newMemory);

}
