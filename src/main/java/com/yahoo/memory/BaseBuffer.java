/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

class BaseBuffer {
  private long pos;
  private long low;
  private long high;
  private long cap;

  BaseBuffer(final long capacity) {
    assert capacity > 0;
    this.cap = capacity;
    this.low = 0;
    this.pos = 0;
    this.high = capacity;
  }

  /**
   * Sets the low bound, position, and the high bound
   * @param low the low bound
   * @param pos the position bewteen the low bound and the high bound
   * @param high the high bound
   * @return BaseBuffer
   */
  BaseBuffer setLowPosHigh(final long low, final long pos, final long high) {
    assertInvariants(low, pos, high, this.cap);
    this.low = low;
    this.high = high;
    this.pos = pos;
    return this;
  }

  /**
   * Gets the low bound
   * @return the low bound
   */
  long getLow() {
    return this.low;
  }

  /**
   * Gets the current position
   * @return the current position
   */
  long getPos() {
    return this.pos;
  }

  /**
   * Gets the high bound
   * @return high
   */
  long getHigh() {
    return this.high;
  }

  /**
   * Sets the position
   * @param pos the given position
   * @return BaseBuffer
   */
  BaseBuffer setPos(final long pos) {
    assertInvariants(this.low, pos, this.high, this.cap);
    this.pos = pos;
    return this;
  }

  /**
   * Increments the current pos by inc
   * @param inc the increment
   * @return BaseBuffer
   */
  BaseBuffer incPos(final long inc) {
    assertInvariants(this.low, this.pos + inc, this.high, this.cap);
    this.pos += inc;
    return this;
  }

  /**
   * Resets the pos to the low bound,
   * This does not modify any data.
   * @return BaseBuffer
   */
  BaseBuffer resetPos() {
    this.pos = this.low;
    return this;
  }

  /**
   * The number of elements remaining between the pos and the upperBound //remaining
   * @return (upperBound - pos)
   */
  long getRemaining()  {
    return this.high - this.pos;
  }

  /**
   * Returns true if there are elements remaining between the pos and the upperBound //hasRemaining
   * @return (upperBound - pos) > 0
   */
  boolean hasRemaining() {
    return (this.high - this.pos) > 0;
  }

  static final void assertInvariants(final long low, final long pos, final long high,
      final long capacity) {
    assert (low | pos | high | capacity | (pos - low) | (high - pos) | (capacity - high) ) >= 0L
        : "Violation of Invariants: "
        + "low: " + low
        + ", pos: " + pos
        + ", high: " + high
        + ", cap: " + capacity
        + ", (pos - low): " + (pos - low)
        + ", (high - pos): " + (high - pos)
        + " ,(cap - high): " + (capacity - high);
  }

}
