/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * Gets a WritableMemory for a map resource
 * @author Lee Rhodes
 */
//Defines combo of WritableMemory with WritableMap resource
public interface WritableMemoryMapHandler extends WritableMap {

  /**
   * Gets a WritableMemory for a map resource
   * @return a WritableMemory for a map resource
   */
  WritableMemory get();

}
