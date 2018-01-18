/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * This exception will be thrown for errors encountered during either the encoding of characters
 * to Utf8 bytes, or the decoding of Utf8 bytes to characters.
 *
 * @author Lee Rhodes
 */
public final class Utf8CodingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public Utf8CodingException(final String message) {
    super(message);
  }

  //Decode
  static Utf8CodingException shortUtf8DecodeByteSequence(final byte leadByte, final long address,
      final long limit, final int required) {
    final String s = "Too few Utf8 decode bytes remaining given the leading byte. "
        + shortSeq(leadByte, address, limit, required);
    return new Utf8CodingException(s);
  }

  static Utf8CodingException illegalUtf8DecodeByteSequence(final byte[] bytes) {
    final String s = "Invalid UTF-8 decode byte sequence: " + badBytes(bytes);
    return new Utf8CodingException(s);
  }

  //Encode
  static Utf8CodingException outOfMemory() {
    final String s = "Out-of-memory with characters remaining to be encoded";
    return new Utf8CodingException(s);
  }

  static Utf8CodingException unpairedSurrogate(final char c) {
    final String s = "Last char to encode is an unpaired surrogate: 0X"
        + Integer.toHexString(c & 0XFFFF);
    return new Utf8CodingException(s);
  }

  static Utf8CodingException shortUtf8EncodeByteLength(final int remaining) {
    final String s = "Too few Memory bytes to encode a surrogate pair: " + remaining;
    return new Utf8CodingException(s);
  }

  static Utf8CodingException illegalSurrogatePair(final char c1, final char c2) {
    final String s = "Illegal Surrogate Pair: Char 1: " + Integer.toHexString(c1 & 0XFFFF)
      + ", Char 2: " + Integer.toHexString(c2 & 0XFFFF);
    return new Utf8CodingException(s);
  }

  private static String shortSeq(final byte leadByte, final long address, final long limit,
      final int required) {
    final String s = "Lead byte: " + Integer.toHexString(leadByte & 0xFF)
      + ", offset: 0X" + Long.toHexString(address)
      + ", limit: 0X" + Long.toHexString(limit)
      + ", required: " + required;
    return s;
  }

  private static String badBytes(final byte[] bytes) {
    final StringBuilder sb = new StringBuilder();
    final int len = bytes.length;
    int i = 0;
    for (; i < (len - 1); i++) {
      sb.append("0X" + Integer.toHexString(bytes[i] & 0XFF)).append(", ");
    }
    sb.append("0X" + Integer.toHexString(bytes[i] & 0XFF));
    return sb.toString();
  }

}
