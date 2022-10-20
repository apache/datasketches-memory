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

/** Equivalent of Guava's Ints. */
public final class Ints {

  private Ints() {}

  /**
   * Checks if a cast of a long to an int is within the range of an int
   * @param v the given long
   * @return returns the cast int, or throws an exception that the long was out-of-range of an int.
   */
  public static int checkedCast(final long v) {
    final int result = (int) v;
    if (result != v) {
      throw new IllegalArgumentException("Out of range: " + v);
    }
    return result;
  }
}
