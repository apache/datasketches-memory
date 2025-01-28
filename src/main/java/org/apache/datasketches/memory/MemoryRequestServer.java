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

import java.lang.foreign.Arena;
import java.nio.ByteOrder;

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
   * Request new WritableMemory with the given newCapacityBytes.
   * @param newCapacityBytes The capacity being requested.
   * @param alignmentBytes requested segment alignment. Typically 1, 2, 4 or 8.
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @param arena the given arena to manage the new off-heap WritableMemory.
   * If arena is null, the requested WritableMemory will be off-heap.
   * Warning: This class is not thread-safe. Specifying an Arena that allows multiple threads is not recommended.
   * @return new WritableMemory with the requested capacity.
   */
  WritableMemory request(
      long newCapacityBytes,
      long alignmentBytes,
      ByteOrder byteOrder,
      Arena arena);

  /**
   * Request to close the area managing all the related resources, if applicable.
   * Be careful when you request to close the given Arena, you may be closing other resources as well.
   * @param arena the given arena to use to close all its managed resources.
   */
  void requestClose( Arena arena);

}
