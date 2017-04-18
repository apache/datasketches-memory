/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * @author Lee Rhodes
 *
 */
public class DefaultMemoryRequest implements MemoryRequest {

  @Override
  public WritableMemory request(final long capacityBytes) {
    final WritableMemory mem = WritableMemory.allocate((int)capacityBytes); //default allocate on heap
    mem.setMemoryRequest(this);
    return mem;
  }

  @Override
  public void requestClose(final WritableMemory memoryToClose, final WritableMemory newMemory) {


  }

}
