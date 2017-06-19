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
 * <li>The invariants are {@code 0 <= start <= position <= end <= capacity}.</li>
 * <li>It always starts up as (0, 0, capacity, capacity).</li>
 * <li>You set (start, position, end) in one call with
 * {@link #setStartPositionEnd(long, long, long)}</li>
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
  private final long cap;

  BaseBuffer(final ResourceState state) {
    cap = state.getCapacity();
    final BaseBuffer baseBuf = state.getBaseBuffer();
    if (baseBuf != null) { //BaseBuffer valid, comes from Buffer with valid state
      start = 0;
      pos = 0;
      end = cap;
      assertInvariants(start, pos, end, cap);
    } else { //BaseBuffer null, comes from WritableMemory asBuffer()
      start = 0;
      final ByteBuffer byteBuf = state.getByteBuffer();
      if (byteBuf != null) {
        pos = byteBuf.position();
        end = byteBuf.limit();
        assertInvariants(start, pos, end, cap);
      } else {
        pos = 0;
        end = cap;
        assertInvariants(start, pos, end, cap);
      }
    }
    assertInvariants(start, pos, end, cap);
    state.putBaseBuffer(this);
  }

  /**
   * Sets start position, current position, and end position
   * @param start the start position in the buffer
   * @param position the current position between the start and end
   * @param end the end position in the buffer
   * @return BaseBuffer
   */
  public final BaseBuffer setStartPositionEnd(final long start, final long position, final long end) {
    assertInvariants(start, position, end, cap);
    this.start = start;
    this.end = end;
    pos = position;
    return this;
  }

  /**
   * Gets start position
   * @return start position
   */
  public long getStart() {
    return start;
  }

  /**
   * Gets the current position
   * @return the current position
   */
  public long getPosition() {
    return pos;
  }

  /**
   * Gets the end position
   * @return the end position
   */
  public long getEnd() {
    return end;
  }

  /**
   * Sets the current position
   * @param position the given current position
   * @return BaseBuffer
   */
  public BaseBuffer setPosition(final long position) {
    assertInvariants(start, position, end, cap);
    pos = position;
    return this;
  }

  /**
   * Increments the current position by the given increment
   * @param increment the given increment
   * @return BaseBuffer
   */
  public BaseBuffer incrementPosition(final long increment) {
    assertInvariants(start, pos + increment, end, cap);
    pos += increment;
    return this;
  }

  /**
   * Resets the current position to the start position,
   * This does not modify any data.
   * @return BaseBuffer
   */
  public BaseBuffer resetPosition() {
    pos = start;
    return this;
  }

  /**
   * The number of elements remaining between the current position and the end position
   * @return {@code (end - position)}
   */
  public long getRemaining()  {
    return end - pos;
  }

  /**
   * Returns true if there are elements remaining between the current position and the end position
   * @return {@code (end - position) > 0}
   */
  public boolean hasRemaining() {
    return (end - pos) > 0;
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
