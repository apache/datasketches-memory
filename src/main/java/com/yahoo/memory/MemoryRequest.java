/*
 * Copyright 2015, Yahoo, Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * The MemoryRequest is a callback interface provides a means for a WritableMemory object to
 * request more memory from the WritableMemoryDirectHandler. This mechanism is only used with directly
 * allocated native memory.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequest {

  /**
   * Request new WritableMemory with the given capacity.
   * @param capacityBytes The capacity being requested
   * @return new WritableMemory with the given capacity.
   */
  WritableMemory request(long capacityBytes);

  /**
   * Request close of the AutoCloseable resource.
   */
  void requestClose();

}
