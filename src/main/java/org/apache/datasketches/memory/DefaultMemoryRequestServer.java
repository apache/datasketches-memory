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
 * This example MemoryRequestServer is simple but demonstrates one of many ways to
 * manage continuous requests for larger or smaller memory.
 * This capability is only available for writable, non-file-memory-mapping resources.
 *
 * @author Lee Rhodes
 */
public final class DefaultMemoryRequestServer implements MemoryRequestServer {

  /**
   * Default constructor.
   */
  public DefaultMemoryRequestServer() {
  }

  @Override
  public WritableMemory request(
      final long newCapacityBytes,
      final long alignmentBytes,
      final ByteOrder byteOrder,
      final Arena arena) {
    final WritableMemory newWmem;

    if (arena != null) {
      newWmem = WritableMemory.allocateDirect(newCapacityBytes, alignmentBytes, byteOrder, this, arena);
    }
    else { //On-heap
      if (newCapacityBytes > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Requested capacity exceeds Integer.MAX_VALUE.");
      }
      newWmem = WritableMemory.allocate((int)newCapacityBytes, byteOrder, this);
    }

    return newWmem;
  }

  @Override
  public void requestClose(final Arena arena) {
    if (arena.scope().isAlive()) { arena.close(); }
  }

}
