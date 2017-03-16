/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is a step boolean function that can change its state only once and is thread-safe.
 *
 * @author Lee Rhodes
 */
final class StepBoolean {
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private volatile boolean state;
  private final boolean initial;

  StepBoolean(final boolean initialValue) {
    this.initial = initialValue;
    this.state = initialValue;
  }

  /**
   * Gets the current state.
   * @return the current state.
   */
  boolean get() {
    try {
      lock.readLock().lock();
      return state;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * This changes the state of this step boolean function if it has not yet changed.
   * If the state has already changed this does nothing and returns false.
   * @return true if the state changed due to this operation
   */
  boolean change() {
    try {
      lock.writeLock().lock();
      if (state == initial) {
        this.state = !initial;
        return true;
      }
      return false;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Return true if the state has changed from the initial state
   * @return true if the state has changed from the initial state
   */
  boolean hasChanged() {
    try {
      lock.readLock().lock();
      return state != initial;
    } finally {
      lock.readLock().unlock();
    }
  }
}
