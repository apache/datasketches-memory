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
 * <li>The invariants are 0 <= start <= position <= end <= capacity.</li>
 * <li>It always starts up as (0, 0, capacity, capacity).</li>
 * <li>You set (start, position, end) in one call with {@link #setStartPositionEnd(long, long, long)}</li>
 * <li>Added incrementPosition(long), which is much easier when you know the increment.</li>
 * <li>This approach eliminated a number of methods and checks, and has no unseen side effects,
 * e.g., mark being invalidated.</li>
 * <li>Clearer method naming (IMHO).</li>
 * </ul>
 *
 * @author Lee Rhodes
 */
class BaseBuffer {
  private long start;
  private long pos;
  private long end;
  private long cap;

  BaseBuffer(final ResourceState state) {
    this.cap = state.getCapacity();
    final BaseBuffer baseBuf = state.getBaseBuffer();
    if (baseBuf != null) {
      this.start = baseBuf.getStart();
      this.pos = baseBuf.getPosition();
      this.end = baseBuf.getEnd();
    } else {
      final ByteBuffer byteBuf = state.getByteBuffer();
      if (byteBuf != null) {
        this.pos = byteBuf.position();
        this.end = byteBuf.limit();
      } else {
        this.pos = 0;
        this.end = this.cap;
      }
      this.start = 0;
    }
    state.putBaseBuffer(this);
  }

  /**
   * Sets start, position, and end
   * @param start the start position in the buffer
   * @param position the position bewteen start and end
   * @param end the end position in the buffer
   * @return BaseBuffer
   */
  BaseBuffer setStartPositionEnd(final long start, final long position, final long end) {
    assertInvariants(start, position, end, this.cap);
    this.start = start;
    this.end = end;
    this.pos = position;
    return this;
  }

  /**
   * Gets start
   * @return start
   */
  long getStart() {
    return this.start;
  }

  /**
   * Gets the current position
   * @return the current position
   */
  long getPosition() {
    return this.pos;
  }

  /**
   * Gets end
   * @return end
   */
  long getEnd() {
    return this.end;
  }

  /**
   * Sets the position
   * @param position the given position
   * @return BaseBuffer
   */
  BaseBuffer setPosition(final long position) {
    assertInvariants(this.start, position, this.end, this.cap);
    this.pos = position;
    return this;
  }

  /**
   * Increments the current position by the given increment
   * @param increment the increment
   * @return BaseBuffer
   */
  BaseBuffer incrementPosition(final long increment) {
    assertInvariants(this.start, this.pos + increment, this.end, this.cap);
    this.pos += increment;
    return this;
  }

  /**
   * Resets the position to start,
   * This does not modify any data.
   * @return BaseBuffer
   */
  BaseBuffer resetPosition() {
    this.pos = this.start;
    return this;
  }

  /**
   * The number of elements remaining between the pos and end
   * @return (end - position)
   */
  long getRemaining()  {
    return this.end - this.pos;
  }

  /**
   * Returns true if there are elements remaining between the pos and end
   * @return (end - position) > 0
   */
  boolean hasRemaining() {
    return (this.end - this.pos) > 0;
  }

  static final void assertInvariants(final long start, final long pos, final long end,
      final long cap) {
    assert (start | pos | end | cap | (pos - start) | (end - pos) | (cap - end) ) >= 0L
        : "Violation of Invariants: "
        + "start: " + start
        + " <= pos: " + pos
        + " <= end: " + end
        + " <= cap: " + cap
        + "; (pos - start): " + (pos - start)
        + ", (end - pos): " + (end - pos)
        + ", (cap - end): " + (cap - end);
  }

}
