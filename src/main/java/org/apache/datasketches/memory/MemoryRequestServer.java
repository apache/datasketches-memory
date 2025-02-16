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
 * The MemoryRequestServer is a callback interface to provide a means to request more or less memory
 * for heap and off-heap WritableMemory resources that are not file-memory-mapped backed resources.
 *
 * <p>Note: this only works with Java 21.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request a new WritableMemory with the given newCapacityBytes.
   * @param oldWmem the previous WritableMemory to be possibly closed and which provides an associated Arena
   * that may be used for allocating the new WritableMemory.
   * If the arena is null, the requested WritableMemory will be on-heap.
   * @param newCapacityBytes The capacity being requested.
   *
   * @return new WritableMemory with the requested capacity.
   */
  WritableMemory request(WritableMemory oldWmem, long newCapacityBytes);

  /**
   * Request to close the given WritableMemory.  If applicable, it will be closed by its associated Arena.
   * Be careful. Closing the associated Arena may be closing other resources as well.
   * @param wmemToClose the given WritableMemory to close.
   */
  void requestClose(WritableMemory wmemToClose);

}
