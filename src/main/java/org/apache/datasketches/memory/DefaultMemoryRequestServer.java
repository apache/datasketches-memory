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
 * <p>The operation of this implementation is controlled by three conditions:</p>
 * <ul>
 * <li><b><i>origOffHeap:</i></b> If <i>true</i>, the originally allocated WritableMemory is off-heap.</li>
 *
 * <li><b><i>oneArena:</i></b> If <i>true</i>, all subsequent off-heap allocations will use the same Arena
 * obtained from the original off-heap WritableMemory. Otherwise, subsequent off-heap allocations will
 * use a new confined Arena created by this implementation.</li>
 *
 * <li><b><i>offHeap:</i></b> If <i>true</i>, all subsequent allocations will be off-heap.
 * If the originally allocated WritableMemory is on-heap, this variable is ignored.</li>
 * </ul>
 *
 * <p>These three variables work together as follows:</p>
 *
 * <ul>
 * <li>If the original WritableMemory is on-heap, all subsequent allocations will also be on-heap.</li>
 *
 * <li>If <i>origOffHeap</i> = <i>true</i>, <i>oneArena</i> = <i>true</i>, and <i>offHeap</i> = <i>true</i>,
 * all subsequent allocations will also be off-heap and associated with the original Arena.
 * It is the responsibility of the user to close the original Arena using a Try-With-Resource block, or directly.</li>
 *
 * <li>If the original WritableMemory is off-heap, <i>oneArena</i> is true, and <i>offHeap</i> is false,
 * all subsequent allocations will be on-heap.
 * It is the responsibility of the user to close the original Arena using a Try-With-Resource block, or directly.</li>
 *
 * <li>If the original WritableMemory is off-heap, <i>oneArena</i> is false, and <i>offHeap</i> is true,
 * all subsequent allocations will also be off-heap and associated with a new confined Arena assigned by this implementation.
 * It is the responsibility of the user to close the original Arena using a Try-With-Resource block, or directly,
 * and close the last returned new WritableMemory directly.</li>
 * </ul>
 *
 * <p>In summary:</p>
 *
 * <table> <caption><b>Configuration Options</b></caption>
 * <tr><th>Original Off-Heap</th> <th>OneArena</th> <th>OffHeap</th> <th>Subsequent Allocations</th></tr>
 * <tr><td>false</td>             <td>N/A</td>      <td>N/A</td>     <td>All on-heap</td></tr>
 * <tr><td>true</td>              <td>N/A</td>     <td>false</td>    <td>All on-heap</td></tr>
 * <tr><td>true</td>              <td>true</td>     <td>true</td>    <td>All off-heap in original Arena</td></tr>
 * <tr><td>true</td>              <td>false</td>    <td>true</td>    <td>All off-heap in separate Arenas</td></tr>
 * </table>
 *
 * @author Lee Rhodes
 */
//@SuppressWarnings("resource") //can't use TWRs here
public final class DefaultMemoryRequestServer implements MemoryRequestServer {
  private final long alignmentBytes;
  private final ByteOrder byteOrder;
  private final boolean oneArena;
  private final boolean offHeap;

  /**
   * Default constructor.
   */
  public DefaultMemoryRequestServer() {
    alignmentBytes = 8;
    byteOrder = ByteOrder.nativeOrder();
    oneArena = false;
    offHeap = false;
  }

  /**
   * Optional constructor 1.
   * @param oneArena if true, the original arena will be used for all requested allocations.
   * @param offHeap if true, new allocations will be off-heap.
   */
  public DefaultMemoryRequestServer(
      final boolean oneArena,
      final boolean offHeap) {
    this.alignmentBytes = 8;
    this.byteOrder = ByteOrder.nativeOrder();
    this.oneArena = oneArena;
    this.offHeap = offHeap;
  }

  /**
   * Optional constructor 2.
   * @param alignmentBytes requested segment alignment for all allocations. Typically 1, 2, 4 or 8.
   * @param byteOrder the given <i>ByteOrder</i>.  It must be non-null.
   * @param oneArena if true, the same arena will be used for all requested allocations.
   * @param offHeap if true, new allocations will be off-heap.
   */
  public DefaultMemoryRequestServer(
      final long alignmentBytes,
      final ByteOrder byteOrder,
      final boolean oneArena,
      final boolean offHeap) {
    this.alignmentBytes = alignmentBytes;
    this.byteOrder = byteOrder;
    this.oneArena = oneArena;
    this.offHeap = offHeap;
  }

  @Override
  public WritableMemory request(
      final WritableMemory oldWmem,
      final long newCapacityBytes) {

    //On-heap
    if (oldWmem.getArena() == null || !offHeap) {
      if (newCapacityBytes > Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Requested capacity exceeds Integer.MAX_VALUE.");
      }
      return WritableMemory.allocate((int)newCapacityBytes, byteOrder, this);
    }

    //Acquire Arena
    final Arena arena = (oneArena) ? oldWmem.getArena() : Arena.ofConfined();
    return WritableMemory.allocateDirect(newCapacityBytes, alignmentBytes, byteOrder, this, arena);
  }

  @Override
  public void requestClose(final WritableMemory wmemToClose) {
    final Arena arena = wmemToClose.getArena();
    if (oneArena || arena == null || !arena.scope().isAlive()) { return; } //can't close
    arena.close();
  }

}
