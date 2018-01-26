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
  private final boolean initialState;
  private volatile boolean state;

  StepBoolean(final boolean initialState) {
    this.initialState = initialState;
    state = initialState;
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  boolean get() {
    return state;
  }

  /**
   * This changes the state of this step boolean function if it has not yet changed.
   */
  void change() {
    state = !initialState;
  }

  /**
   * Return true if the state has changed from the initial state
   * @return true if the state has changed from the initial state
   */
  boolean hasChanged() {
    return state == !initialState;
  }
}
