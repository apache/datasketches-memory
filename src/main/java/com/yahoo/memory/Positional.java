/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public interface Positional {

  /**
   * Sets the limit to the current position and then sets the position to zero
   * @return WritableBuffer
   */
  WritableBuffer flip();

  /**
   * Returns true if there are elements remaining between the position and the limit
   * @return (limit - pos) > 0
   */
  boolean hasRemaining();

  /**
   * @return limit
   */
  long limit();

  /**
   * Sets the limit
   * @param lim the given limit
   */
  void limit(long lim);

  /**
   * Sets the mark to the current position
   */
  void mark();

  /**
   * @return the current position
   */
  long position();

  /**
   * Sets the postion
   * @param pos the given position
   */
  void position(long pos);

  /**
   * The number of elements remaining between the position and the limit
   * @return (limit - position)
   */
  long remaining();

  /**
   * Sets the position to the mark
   */
  void reset();

  /**
   * Sets the position to zero and the mark to invalid
   */
  void rewind();

  /**
   * Sets the position to zero, the limit to the capacity and the mark to invalid.
   * This does not modify any data.
   */
  void clearPositions();

}
