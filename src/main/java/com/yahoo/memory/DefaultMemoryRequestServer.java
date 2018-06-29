/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * This is a simple implementation of the MemoryRequestServer that creates space on the Java heap
 * for the requesting application. This capability is only available for direct, off-heap
 * allocated memory.
 *
 * <p>Using this default implementation could be something like the following:
 *
 * <blockquote><pre>
 * class OffHeap {
 *   WritableMemory mem;
 *   MemoryRequestServer memReqSvr = null;
 *
 *   void add(Object something) {
 *
 *     if (outOfSpace) { // determine if out-of-space
 *       long spaceNeeded = ...
 *
 *       //Acquire the MemoryRequestServer from the direct Memory the first time.
 *       //Once acquired, this can be reused if more memory is needed later.
 *       //This is required for the default implementation because it returns memory on heap
 *       // and on-heap memory does not carry a reference to the MemoryRequestServer.
 *       memReqSvr = (memReqSvr == null) ? mem.getMemoryRequestServer() : memReqSvr;
 *
 *       //Request bigger memory
 *       WritableMemory newMem = memReqSvr.request(spaceNeeded);
 *
 *       //Copy your data from the current memory to the new one and resize
 *       moveAndResize(mem, newMem);
 *
 *       //You are done with the old memory, so request close.
 *       //Note that it is up to the owner of the WritableDirectHandle whether or not to
 *       // actually close the resource.
 *       memReqSvr.requestClose(mem, newMem);
 *
 *       mem = newMem; //update your reference to memoty
 *     }
 *
 *     //continue with the add process
 *   }
 * }
 * </pre></blockquote>
 *
 *
 * @author Lee Rhodes
 */
public final class DefaultMemoryRequestServer implements MemoryRequestServer {

  /**
   * {@inheritDoc}
   *
   * <p>By default this allocates new memory requests on the Java heap.
   */
  @Override
  public WritableMemory request(final long capacityBytes) {
    final WritableMemory wmem = WritableMemory.allocate((int)capacityBytes);
    return wmem;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method does nothing in this default implementation because it is application specific.
   * This method must be overridden to explicitly close if desired.
   * Otherwise, the AutoCloseable will eventually close the resource.
   */
  @Override
  public void requestClose(final WritableMemory memToRelease, final WritableMemory newMemory) {
    //do nothing
  }

}
