/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

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
public class BaseBuffer {
  final ResourceState state;
  final long capacity;
  private long start = 0;
  private long pos = 0;
  private long end;


  BaseBuffer(final ResourceState state) {
    this.state = state;
    capacity = state.getCapacity();
    end = capacity;
    state.putBaseBuffer(this);
  }

  /**
   * Sets start position, current position, and end position
   * @param start the start position in the buffer
   * @param position the current position between the start and end
   * @param end the end position in the buffer
   * @return BaseBuffer
   */
  public final BaseBuffer setStartPositionEnd(final long start, final long position,
        final long end) {
    assertInvariants(start, position, end, capacity);
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
    assertInvariants(start, position, end, capacity);
    pos = position;
    return this;
  }

  /**
   * Increments the current position by the given increment
   * @param increment the given increment
   * @return BaseBuffer
   */
  public BaseBuffer incrementPosition(final long increment) {
    incrementPosition(pos, increment);
    return this;
  }

  void incrementPosition(final long pos, final long increment) {
    assertValid();
    final long newPos = pos + increment;
    assertInvariants(start, newPos, end, capacity);
    this.pos = newPos;
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

  static final void checkInvariants(final long start, final long pos, final long end,
        final long cap) {
    if ((start | pos | end | cap | (pos - start) | (end - pos) | (cap - end) ) < 0L) {
      throw new IllegalArgumentException(
          "Violation of Invariants: "
              + "start: " + start
              + " <= pos: " + pos
              + " <= end: " + end
              + " <= cap: " + cap
              + "; (pos - start): " + (pos - start)
              + ", (end - pos): " + (end - pos)
              + ", (cap - end): " + (cap - end)
      );
    }
  }

  //RESTRICTED READ AND WRITE XXX
  final void assertValid() { //applies to both readable and writable
    assert state.isValid() : "Memory not valid.";
  }

  final void checkValid() {
    if (!state.isValid()) {
      throw new IllegalStateException("Memory not valid.");
    }
  }

  final ResourceState getResourceState() {
    return state;
  }
}
