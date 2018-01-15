/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

public final class Utf8CodingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public Utf8CodingException(final String message) {
    super(message);
  }

  static Utf8CodingException inputBounds(final long address, final long limit) {
    return new Utf8CodingException("Bounds violation: " + badBounds(address, limit));
  }

  static Utf8CodingException inputBytes(final byte[] bytes) {
    return new Utf8CodingException("Invalid UTF-8 input byte sequence: " + badBytes(bytes));
  }

  static String badBounds(long address, long limit) {
    return "0X" + Long.toHexString(address) + " >= 0X" + Long.toHexString(limit);
  }

  static String badBytes(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    int len = bytes.length;
    int i = 0;
    for (; i < (len - 1); i++) {
      sb.append("0X" + Integer.toHexString(bytes[i] & 0XFF)).append(", ");
    }
    sb.append("0X" + Integer.toHexString(bytes[i] & 0XFF));
    return sb.toString();
  }

}
