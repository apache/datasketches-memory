/*
 * Copyright 2015, Yahoo, Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * The MemoryRequestServer is a callback interface to provide a means for a direct (off-heap),
 * dynamic WritableMemory object to request more memory from the owner of the
 * {@link WritableDirectHandle}. Refer to {@link DefaultMemoryRequestServer} for how this can be
 * used.
 *
 * @author Lee Rhodes
 */
public interface MemoryRequestServer {

  /**
   * Request new WritableMemory with the given capacity.
   * @param capacityBytes The capacity being requested.
   * @return new WritableMemory with the given capacity.
   */
  WritableMemory request(long capacityBytes);

  /**
   * Request close the AutoCloseable resource.
   * This may be ignored depending on the application implementation.
   * @param memToClose the relevant WritbleMemory to be considered for closing.
   * @param newMemory the newly allocated WritableMemory. This is returned from the client
   * for the convenience of the resource owner. It is optional and may be null.
   */
  void requestClose(final WritableMemory memToClose, WritableMemory newMemory);

}
