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

import org.apache.datasketches.memory.BaseBuffer;
import org.apache.datasketches.memory.ReadOnlyException;

/**
 * A new positional API. This is different from and simpler than Java BufferImpl positional approach.
 * <ul><li>All based on longs instead of ints.</li>
 * <li>Eliminated "mark". Rarely used and confusing with its silent side effects.</li>
 * <li>The invariants are {@code 0 <= start <= position <= end <= capacity}.</li>
 * <li>It always starts up as (0, 0, capacity, capacity).</li>
 * <li>You set (start, position, end) in one call with
 * {@link #setStartPositionEnd(long, long, long)}</li>
 * <li>Position can be set directly or indirectly when using the positional get/put methods.
 * <li>Added incrementPosition(long), which is much easier when you know the increment.</li>
 * <li>This approach eliminated a number of methods and checks, and has no unseen side effects,
 * e.g., mark being invalidated.</li>
 * <li>Clearer method naming (IMHO).</li>
 * </ul>
 *
 * @author Lee Rhodes
 */
public abstract class BaseBufferImpl extends BaseStateImpl implements BaseBuffer {
  private long capacity;
  private long start = 0;
  private long pos = 0;
  private long end;

  //Pass-through ctor
  BaseBufferImpl(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
    capacity = end = capacityBytes;
  }

  @Override
  public final BaseBufferImpl incrementPosition(final long increment) {
    incrementAndAssertPositionForRead(pos, increment);
    return this;
  }

  @Override
  public final BaseBufferImpl incrementAndCheckPosition(final long increment) {
    incrementAndCheckPositionForRead(pos, increment);
    return this;
  }

  @Override
  public final long getEnd() {
    return end;
  }

  @Override
  public final long getPosition() {
    return pos;
  }

  @Override
  public final long getStart() {
    return start;
  }

  @Override
  public final long getRemaining()  {
    return end - pos;
  }

  @Override
  public final boolean hasRemaining() {
    return (end - pos) > 0;
  }

  @Override
  public final BaseBufferImpl resetPosition() {
    pos = start;
    return this;
  }

  @Override
  public final BaseBufferImpl setPosition(final long position) {
    assertInvariants(start, position, end, capacity);
    pos = position;
    return this;
  }

  @Override
  public final BaseBufferImpl setAndCheckPosition(final long position) {
    checkInvariants(start, position, end, capacity);
    pos = position;
    return this;
  }

  @Override
  public final BaseBufferImpl setStartPositionEnd(final long start, final long position,
      final long end) {
    assertInvariants(start, position, end, capacity);
    this.start = start;
    this.end = end;
    pos = position;
    return this;
  }

  @Override
  public final BaseBufferImpl setAndCheckStartPositionEnd(final long start, final long position,
      final long end) {
    checkInvariants(start, position, end, capacity);
    this.start = start;
    this.end = end;
    pos = position;
    return this;
  }

  //RESTRICTED
  //Position checks are only used for Buffers
  //asserts are used for primitives, not used at runtime
  final void incrementAndAssertPositionForRead(final long position, final long increment) {
    assertValid();
    final long newPos = position + increment;
    assertInvariants(start, newPos, end, capacity);
    pos = newPos;
  }

  final void incrementAndAssertPositionForWrite(final long position, final long increment) {
    assertValid();
    assert !isReadOnly() : "BufferImpl is read-only.";
    final long newPos = position + increment;
    assertInvariants(start, newPos, end, capacity);
    pos = newPos;
  }

  //checks are used for arrays and apply at runtime
  final void incrementAndCheckPositionForRead(final long position, final long increment) {
    checkValid();
    final long newPos = position + increment;
    checkInvariants(start, newPos, end, capacity);
    pos = newPos;
  }

  final void incrementAndCheckPositionForWrite(final long position, final long increment) {
    checkValidForWrite();
    final long newPos = position + increment;
    checkInvariants(start, newPos, end, capacity);
    pos = newPos;
  }

  final void checkValidForWrite() {
    checkValid();
    if (isReadOnly()) {
      throw new ReadOnlyException("BufferImpl is read-only.");
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

}
