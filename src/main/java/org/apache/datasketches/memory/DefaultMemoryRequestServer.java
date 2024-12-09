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
 * manage continuous requests for larger memory.
 * This capability is only available for writable, non-file-memory-mapping resources.
 *
 * @author Lee Rhodes
 */
public final class DefaultMemoryRequestServer implements MemoryRequestServer {
  private final boolean offHeap; //create the new memory off-heap; otherwise, on-heap
  private final boolean copyOldToNew; //copy data from old memory to new memory.

  /**
   * Default constructor.
   * Create new memory on-heap and do not copy old contents to new memory.
   */
  public DefaultMemoryRequestServer() {
    this(false, false);
  }

  /**
   * Constructor with parameters
   * @param offHeap if true, the returned new memory will be off heap
   * @param copyOldToNew if true, the data from the current memory will be copied to the new memory,
   * starting at address 0, and through the currentMemory capacity.
   */
  public DefaultMemoryRequestServer(
      final boolean offHeap,
      final boolean copyOldToNew) {
    this.offHeap = offHeap;
    this.copyOldToNew = copyOldToNew;
  }

  @Override
  public WritableMemory request(
      final WritableMemory currentWmem,
      final long newCapacityBytes,
      final Arena arena) {
    final ByteOrder order = currentWmem.getTypeByteOrder();
    final long currentBytes = currentWmem.getCapacity();
    final WritableMemory newWmem;

    if (newCapacityBytes <= currentBytes) {
      throw new IllegalArgumentException("newCapacityBytes must be &gt; currentBytes");
    }

    if (offHeap) {
      newWmem = WritableMemory.allocateDirect(newCapacityBytes, 8, order, this, arena);
    }
    else { //On-heap
      if (newCapacityBytes > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Requested capacity exceeds Integer.MAX_VALUE.");
      }
      newWmem = WritableMemory.allocate((int)newCapacityBytes, order, this);
    }

    if (copyOldToNew) {
      currentWmem.copyTo(0, newWmem, 0, currentBytes);
    }

    return newWmem;
  }

  @Override
  public void requestClose(
      final WritableMemory memToClose,
      final WritableMemory newMemory) {
    //try to make this operation idempotent.
    if (memToClose.isCloseable()) { memToClose.close(); }
  }

}
