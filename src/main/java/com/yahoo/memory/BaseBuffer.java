/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;

/**
 * A new positional API. This is different from and simpler than Java Buffer positional approach.
 * <ul><li>All based on longs instead of ints.</li>
 * <li>Eliminated "mark". Rarely used and confusing with its silent side effects.</li>
 * <li>The invariants are 0 <= low <= pos <= high <= cap.</li>
 * <li>It always starts up as (0, 0, cap, cap).</li>
 * <li>You set (low, pos, cap) in one call with {@link #setLowPosHigh(long, long, long)}</li>
 * <li>Added incPos(long), which is much easier when you know the increment.</li>
 * <li>This approach eliminated a number of methods and checks, and has no unseen side effects,
 * e.g., mark being invalidated.</li>
 * <li>Clearer method naming (IMHO).</li>
 * </ul>
 *
 * @author Lee Rhodes
 */
class BaseBuffer {
  private long low;
  private long pos;
  private long high;
  private long cap;

  BaseBuffer(final ResourceState state) {
    this.cap = state.getCapacity();
    final BaseBuffer baseBuf = state.getBaseBuffer();
    if (baseBuf != null) {
      this.low = baseBuf.getLow();
      this.pos = baseBuf.getPos();
      this.high = baseBuf.getHigh();
    } else {
      final ByteBuffer byteBuf = state.getByteBuffer();
      if (byteBuf != null) {
        this.pos = byteBuf.position();
        this.high = byteBuf.limit();
      } else {
        this.pos = 0;
        this.high = this.cap;
      }
      this.low = 0;
    }
    state.putBaseBuffer(this);
  }

  /**
   * Sets low, position, and high
   * @param low the low
   * @param pos the position bewteen low and high
   * @param high the high
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
   * Gets low
   * @return low
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
   * Gets high
   * @return high
   */
  long getHigh() {
    return this.high;
  }

  /**
   * Gets cap
   * @return cap
   */
  long getCap() {
    return this.cap;
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
   * Resets the pos to low,
   * This does not modify any data.
   * @return BaseBuffer
   */
  BaseBuffer resetPos() {
    this.pos = this.low;
    return this;
  }

  /**
   * The number of elements remaining between the pos and high
   * @return (high - pos)
   */
  long getRemaining()  {
    return this.high - this.pos;
  }

  /**
   * Returns true if there are elements remaining between the pos and high
   * @return (high - pos) > 0
   */
  boolean hasRemaining() {
    return (this.high - this.pos) > 0;
  }

  static final void assertInvariants(final long low, final long pos, final long high,
      final long cap) {
    assert (low | pos | high | cap | (pos - low) | (high - pos) | (cap - high) ) >= 0L
        : "Violation of Invariants: "
        + "low: " + low
        + " <= pos: " + pos
        + " <= high: " + high
        + " <= cap: " + cap
        + "; (pos - low): " + (pos - low)
        + ", (high - pos): " + (high - pos)
        + ", (cap - high): " + (cap - high);
  }

}
