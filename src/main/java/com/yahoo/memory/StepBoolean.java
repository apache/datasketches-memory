/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * This is a step boolean function that can change its state only once and is thread-safe.
 *
 * @author Lee Rhodes
 */
final class StepBoolean {
  private static final int FALSE = 0;
  private static final int TRUE = 1;
  private static final AtomicIntegerFieldUpdater<StepBoolean> STATE_FIELD_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(StepBoolean.class, "state");

  private final int initialState;
  private volatile int state;

  StepBoolean(final boolean initialState) {
    this.initialState = initialState ? TRUE : FALSE;
    state = this.initialState;
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  boolean get() {
    return state == TRUE;
  }

  /**
   * This changes the state of this step boolean function if it has not yet changed.
   * @return true if this call led to the change of the state; false if the state has already been
   * changed
   */
  boolean change() {
    final int notInitialState = initialState == TRUE ? FALSE : TRUE;
    return STATE_FIELD_UPDATER.compareAndSet(this, initialState, notInitialState);
  }

  /**
   * Return true if the state has changed from the initial state
   * @return true if the state has changed from the initial state
   */
  boolean hasChanged() {
    return state != initialState;
  }
}
