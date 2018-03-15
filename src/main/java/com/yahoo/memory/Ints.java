/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/** Equivalent of Guava's Ints. */
final class Ints {

  private Ints() {}

  static int checkedCast(final long v) {
    final int result = (int) v;
    if (result != v) {
      throw new IllegalArgumentException("Out of range: " + v);
    } else {
      return result;
    }
  }
}
