/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * This is a step boolean function that can change its state only once and is thread-safe.
 *
 * @author Lee Rhodes
 */
final class StepBoolean {
  private volatile boolean state;

  StepBoolean(final boolean initialState) {
    this.state = initialState;
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  boolean get() {
    return this.state;
  }

  void set(boolean state)
  {
    this.state = state;
  }
}
