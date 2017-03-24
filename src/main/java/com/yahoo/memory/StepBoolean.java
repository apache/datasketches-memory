/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a step boolean function that can change its state only once and is thread-safe.
 *
 * @author Lee Rhodes
 */
final class StepBoolean {
  private final boolean initial;
  private AtomicBoolean state = new AtomicBoolean(false);

  StepBoolean(final boolean initialState) {
    this.initial = initialState;
    this.state.set(initialState);
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  boolean get() {
    return this.state.get();
  }

  /**
   * This changes the state of this step boolean function if it has not yet changed.
   * If the state has already changed this does nothing and returns false.
   * @return true if the state changed due to this operation
   */
  boolean change() {
    return this.state.compareAndSet(this.initial, !this.initial);
  }

  /**
   * Return true if the state has changed from the initial state
   * @return true if the state has changed from the initial state
   */
  boolean hasChanged() {
    return !change();
  }
}
