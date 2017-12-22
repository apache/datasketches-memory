/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public class Utf8CodingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public Utf8CodingException(final String message) {
    super(message);
  }

  static Utf8CodingException input() {
    return new Utf8CodingException("Invalid input UTF-8");
  }
}
