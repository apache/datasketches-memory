/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * This is a step boolean function that can change its state only once.
 *
 * @author Lee Rhodes
 */
public final class StepBoolean {
  private static final int FALSE = 0;
  private static final int TRUE = 1;
  private static final AtomicIntegerFieldUpdater<StepBoolean> STATE_FIELD_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(StepBoolean.class, "state");

  private final int initialState;
  private volatile int state;

  /**
   * Defines the initial state
   * @param initialState the given initial state
   */
  public StepBoolean(final boolean initialState) {
    this.initialState = initialState ? TRUE : FALSE;
    state = this.initialState;
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  public boolean get() {
    return state == TRUE;
  }

  /**
   * This changes the state of this step boolean function if it has not yet changed.
   * @return true if this call led to the change of the state; false if the state has already been
   * changed
   */
  public boolean change() {
    final int notInitialState = initialState == TRUE ? FALSE : TRUE;
    return STATE_FIELD_UPDATER.compareAndSet(this, initialState, notInitialState);
  }

  /**
   * Return true if the state has changed from the initial state
   * @return true if the state has changed from the initial state
   */
  public boolean hasChanged() {
    return state != initialState;
  }
}
