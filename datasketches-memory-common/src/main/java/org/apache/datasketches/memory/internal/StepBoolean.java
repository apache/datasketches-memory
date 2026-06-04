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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * This is a step boolean or latch function that can change its state only once.
 *
 * @author Lee Rhodes
 */
public final class StepBoolean {
  private static final VarHandle STATE_HANDLE;

  static {
      try {
          STATE_HANDLE = MethodHandles.lookup().findVarHandle(StepBoolean.class, "state", int.class);
      } catch (final ReflectiveOperationException e) { throw new Error(e); }
  }

  private final int initialState;
  private volatile int state; // Still needs to be volatile

  /**
   * Defines the initial state
   * @param initialState the given initial state
   */
  public StepBoolean(final boolean initialState) {
      this.initialState = initialState ? 1 : 0;
      this.state = this.initialState;
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  public boolean get() {
    return state == 1;
  }
  
  /**
   * This changes the state of this step boolean function if it has not yet changed.
   * @return true if this call led to the change of the state; false if the state has already been
   * changed
   */
  public boolean change() {
      final int targetState = 1 - initialState;
      // Transition 'state' from 'initialState' to 'targetState'
      return STATE_HANDLE.compareAndSet(this, initialState, targetState);
  }
  
  /**
   * Return true if the state has changed from the initial state
   * @return true if the state has changed from the initial state
   */
  public boolean hasChanged() {
    return state != initialState;
  }
}
