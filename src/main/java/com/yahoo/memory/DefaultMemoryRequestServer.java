/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * @author Lee Rhodes
 */
final class DefaultMemoryRequestServer implements MemoryRequestServer {

  @Override
  //By default this allocates new memory requests on the Java heap.
  public WritableMemory request(final long capacityBytes) {
    final WritableMemory wmem = WritableMemory.allocate((int)capacityBytes);
    return wmem;
  }

  @Override
  public void requestClose(final WritableMemory memToRelease, final WritableMemory newMemory) {
    //If memToRelease is direct, this must be overridden to explicitly close.
    //Otherwise, the AutoCloseable will eventually close the resource.
  }

}
