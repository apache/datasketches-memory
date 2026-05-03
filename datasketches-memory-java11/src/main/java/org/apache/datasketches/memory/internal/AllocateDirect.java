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

package org.apache.datasketches.memory.internal;

import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.lang.ref.Cleaner;

/**
 * Provides access to off-heap memory.
 *
 * @author Lee Rhodes
 */
public class AllocateDirect {
  private static final Cleaner CLEANER = Cleaner.create();
  private final long rawAddress;     //used for freeMemory
  private final long alignedAddress; //data start address
  private final Deallocator deallocator;
  private final Cleaner.Cleanable cleanable;
 
  /**
   * Allocates off-heap memory with a default alignment of 8 bytes.
   * @param capacityBytes must be greater than zero.
   */
  AllocateDirect(final long capacityBytes) {
    this(capacityBytes, Long.BYTES);
  }
  
  /**
   * Allocates off-heap memory with a specified alignment.
   * @param capacityBytes must be greater than or equal 0.
   * @param alignment the desired alignment. It must be a power of 2; e.g., 2, 4 or 8; and greater than 1.
   */
  AllocateDirect(final long capacityBytes, int alignment) {
    if (capacityBytes < 0) { throw new IllegalArgumentException("capacityBytes must be >= 0: " + capacityBytes); }
    long mask = alignment - 1L;
    if (checkAlignment(alignment)) {
      throw new IllegalArgumentException("alignment must be a positive power of 2 and greater than one: " + alignment); }
    try {
      this.rawAddress = unsafe.allocateMemory(capacityBytes + mask);
    } catch (final OutOfMemoryError err) {
      throw new RuntimeException(err);
    }    
    this.alignedAddress = rawAddress & ~mask;
    this.deallocator = new Deallocator(rawAddress);
    this.cleanable = CLEANER.register(this, deallocator);
  }
  
  public long getAddress() {
    return alignedAddress;
  }

  public void close() {
    cleanable.clean();
  }

  public StepBoolean getValid() {
    return deallocator.getValid();
  }

  //Must be static and NOT hold a reference to the outer AllocateDirect class
  private static final class Deallocator implements Runnable {
    //This is the only place the actual native address is kept for use by unsafe.freeMemory();
    private final long addressToFree;
    private final StepBoolean valid = new StepBoolean(true); //only place for this

    Deallocator(final long addressToFree) {
      this.addressToFree = addressToFree;
    }

    StepBoolean getValid() {
      return valid;
    }

    @Override
    public void run() {
      if (valid.change() && addressToFree != 0) {
        unsafe.freeMemory(addressToFree);
      }
    }
  }
  
  /**
   * Returns true if given <i>n</i> is greater than one and a positive power of 2.
   *
   * @param n The input argument.
   * @return true if argument is greater than one and a positive power of 2.
   */
  private static final boolean checkAlignment(final int n) {
    return (n >= 1) && ((n & -n) == n);
  }
}
