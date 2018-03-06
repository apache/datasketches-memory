/*
 * Copyright 2015, Yahoo, Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * The MemoryRequestServer is a callback interface to provide a means for a WritableMemory
 * object to request more memory. This mechanism is primarily used with directly allocated
 * native memory.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request new WritableMemory with the given capacity via a WritableDirectHandle.
   * @param capacityBytes The capacity being requested
   * @return new WritableDirectHandle with the reference to new WritableMemory of the
   * given capacity.
   */
  WritableMemory request(long capacityBytes);

  /**
   * Request to release (perhaps to close) the backing memory for the given WritableMemory.
   * This may be ignored depending on the implementation.
   * @param memToRelease the memory to release
   */
  void release(final WritableMemory memToRelease);

}
