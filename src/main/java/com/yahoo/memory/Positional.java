/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public interface Positional {

  /**
   * @return the current relOffset
   */
  long getRelOffset(); //get position

  /**
   * Sets the relOffset
   * @param relOff the given relOffset
   * @return Positional
   */
  Positional setRelOffset(long relOff); //set position

  /**
   * @return upperBound
   */
  long getUpperBound(); //get limit

  /**
   * Sets the upperBound
   * @param ub the given upperBound
   * @return Positional
   */
  Positional setUpperBound(long ub); //set limit

  /**
   * Sets the tag = relOffset
   * @return Positional
   */
  Positional setTagToRelOffset(); //set mark = position

  /**
   * Sets the relOffset = tag
   * @return Positional
   */
  Positional setRelOffsetToTag(); //reset

  /**
   * Sets the relOffset to zero, the upperBound to the capacity and the tag to invalid.
   * This does not modify any data.
   * @return Positional
   */
  Positional reset(); //clear

  /**
   * Sets the upperBound to the current relOffset and then sets the relOffset to zero
   * @return Positional
   */
  Positional exchange(); //flip

  /**
   * Sets the relOffset to zero and the tag to invalid
   * @return Positional
   */
  Positional setRelOffsetToZero(); //rewind

  /**
   * The number of elements remaining between the relOffset and the upperBound
   * @return (upperBound - relOffset)
   */
  long getRemaining(); //remaining

  /**
   * Returns true if there are elements remaining between the relOffset and the upperBound
   * @return (upperBound - pos) > 0
   */
  boolean hasRemaining(); //hasRemaining

}
