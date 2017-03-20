/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a Memory for a map resource
 * @author Lee Rhodes
 */
//Defines combo of Memory with Map resource.
public interface MemoryMapHandler extends Map {

  /**
   * Gets a Memory for a map resource
   * @return a Memory for a map resource
   */
  Memory get();

}
