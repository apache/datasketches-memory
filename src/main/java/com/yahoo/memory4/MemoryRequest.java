/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

/**
 * The MemoryRequest is a callback interface that is accessible from the WritableMemory interface and
 * provides a means for a WritableMemory object to request more memory from the calling class and to
 * free memory that is no longer needed.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequest {

  /**
   * Request new WritableMemory with the given capacity.
   * @param capacityBytes The capacity being requested
   * @return new WritableMemory with the given capacity. If this request is refused it will be null.
   */
  WritableMemory request(long capacityBytes);

  /**
   * Request for allocate and copy.
   *
   * <p>Request to allocate new WritableMemory with the capacityBytes; copy the contents of origMem
   * from zero to copyToBytes.</p>
   *
   * @param origMem The original WritableMemory, a portion, starting at zero, which will be copied
   * to the newly allocated WritableMemory. This reference must not be null.
   * This origMem must not modified in any way, and may be reused or freed by the implementation.
   * The requesting application may NOT assume anything about the origMem.
   *
   * @param copyToBytes the upper limit of the region to be copied from origMem to the newly
   * allocated WritableMemory. The upper region of the new WritableMemory may or may not be cleared
   * depending on the implementation.
   *
   * @param capacityBytes the desired new capacity of the newly allocated WritableMemory in bytes.
   * @return The new WritableMemory with the given capacity. If this request is refused it will be
   * null.
   */
  WritableMemory request(WritableMemory origMem, long copyToBytes, long capacityBytes);

  /**
   * Request to close the given <i>mem</i>.
   * @param mem The <i>WritableMemory</i> to be closed
   */
  void closeRequest(WritableMemory mem);

  /**
   * Request to close the given <i>memToClose</i>. This callback also provides a reference to the
   * <i>newMem</i> that was just created. This makes it easer for the owner of the allocations to
   * link together the <i>memToClose</i> to the <i>newMem</i>, if desired.
   *
   * @param memToClose the WritableMemory to be closed.
   *
   * @param newMem the owner of the allocations may link together the <i>memToClose</i> to the
   * <i>newMem</i>, if desired.
   */
  void closeRequest(WritableMemory memToClose, WritableMemory newMem);
}
