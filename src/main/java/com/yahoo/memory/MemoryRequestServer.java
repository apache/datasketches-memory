/*
 * Copyright 2015, Yahoo, Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * The MemoryRequestServer is a callback interface provides a means for a WritableMemory object to
 * request more memory. This mechanism is only used with directly allocated native memory.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request new WritableMemory with the given capacity.
   * @param capacityBytes The capacity being requested
   * @return new WritableMemory with the given capacity.
   */
  WritableMemory request(long capacityBytes);

  /**
   * Request to close the AutoCloseable resource.
   * This may be ignored depending on the implementation.
   */
  void requestClose();

}
