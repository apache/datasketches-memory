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
public abstract class BaseBuffer {
  final long capacity;
  private long start = 0;
  private long pos = 0;
  private long end;


  BaseBuffer(final long capacity) {
    this.capacity = capacity;
    end = capacity;
  }

  /**
   * Increments the current position by the given increment.
   * Asserts that the resource is valid and that the positional invariants are not violated,
   * otherwise, if asserts are enabled throws an {@link AssertionError}.
   * @param increment the given increment
   * @return BaseBuffer
   */
  public final BaseBuffer incrementPosition(final long increment) {
    incrementAndAssertPositionForRead(pos, increment);
    return this;
  }

  /**
   * Increments the current position by the given increment.
   * Checks that the resource is valid and that the positional invariants are not violated,
   * otherwise throws an {@link IllegalArgumentException}.
   * @param increment the given increment
   * @return BaseBuffer
   */
  public final BaseBuffer incrementAndCheckPosition(final long increment) {
    incrementAndCheckPositionForRead(pos, increment);
    return this;
  }

  /**
   * Gets the end position
   * @return the end position
   */
  public final long getEnd() {
    return end;
  }

  /**
   * Gets the current position
   * @return the current position
   */
  public final long getPosition() {
    return pos;
  }

  /**
   * Gets start position
   * @return start position
   */
  public final long getStart() {
    return start;
  }

  /**
   * The number of elements remaining between the current position and the end position
   * @return {@code (end - position)}
   */
  public final long getRemaining()  {
    return end - pos;
  }

  /**
   * Returns true if there are elements remaining between the current position and the end position
   * @return {@code (end - position) > 0}
   */
  public final boolean hasRemaining() {
    return (end - pos) > 0;
  }

  /**
   * Resets the current position to the start position,
   * This does not modify any data.
   * @return BaseBuffer
   */
  public final BaseBuffer resetPosition() {
    pos = start;
    return this;
  }

  /**
   * Sets the current position.
   * Asserts that the positional invariants are not violated,
   * otherwise, if asserts are enabled throws an {@link AssertionError}.
   * @param position the given current position.
   * @return BaseBuffer
   */
  public final BaseBuffer setPosition(final long position) {
    assertInvariants(start, position, end, capacity);
    pos = position;
    return this;
  }

  /**
   * Sets the current position.
   * Checks that the positional invariants are not violated,
   * otherwise, throws an {@link IllegalArgumentException}.
   * @param position the given current position.
   * @return BaseBuffer
   */
  public final BaseBuffer setAndCheckPosition(final long position) {
    checkInvariants(start, position, end, capacity);
    pos = position;
    return this;
  }

  /**
   * Sets start position, current position, and end position.
   * Asserts that the positional invariants are not violated,
   * otherwise, if asserts are enabled throws an {@link AssertionError}.
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
   * Sets start position, current position, and end position.
   * Checks that the positional invariants are not violated,
   * otherwise, throws an {@link IllegalArgumentException}.
   * @param start the start position in the buffer
   * @param position the current position between the start and end
   * @param end the end position in the buffer
   * @return BaseBuffer
   */
  public final BaseBuffer setAndCheckStartPositionEnd(final long start, final long position,
      final long end) {
    checkInvariants(start, position, end, capacity);
    this.start = start;
    this.end = end;
    pos = position;
    return this;
  }

  //RESTRICTED XXX
  final void incrementAndAssertPositionForRead(final long position, final long increment) {
    assertValid();
    final long newPos = position + increment;
    assertInvariants(start, newPos, end, capacity);
    pos = newPos;
  }

  final void incrementAndAssertPositionForWrite(final long position, final long increment) {
    assertValid();
    assert !isReadOnly() : "Buffer is read-only.";
    final long newPos = position + increment;
    assertInvariants(start, newPos, end, capacity);
    pos = newPos;
  }

  final void incrementAndCheckPositionForRead(final long pos, final long increment) {
    checkValid();
    final long newPos = pos + increment;
    checkInvariants(start, newPos, end, capacity);
    this.pos = newPos;
  }

  final void incrementAndCheckPositionForWrite(final long pos, final long increment) {
    checkValidForWrite();
    final long newPos = pos + increment;
    checkInvariants(start, newPos, end, capacity);
    this.pos = newPos;
  }

  final void checkValidForWrite() {
    checkValid();
    if (isReadOnly()) {
      throw new ReadOnlyException("Buffer is read-only.");
    }
  }

  /**
   * The invariants equation is: {@code 0 <= start <= position <= end <= capacity}.
   * If this equation is violated and assertions are enabled,
   * an <i>AssertionError</i> will be thrown.
   * @param start the lowest start position
   * @param pos the current position
   * @param end the highest position
   * @param cap the capacity of the backing buffer.
   */
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

  /**
   * The invariants equation is: {@code 0 <= start <= position <= end <= capacity}.
   * If this equation is violated an <i>IllegalArgumentException</i> will be thrown.
   * @param start the lowest start position
   * @param pos the current position
   * @param end the highest position
   * @param cap the capacity of the backing buffer.
   */
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

  abstract void assertValid();

  abstract void checkValid();

  /**
   * Returns true if this or the backing resource is read only
   * @return true if this or the backing resource is read only
   */
  public abstract boolean isReadOnly();
}
