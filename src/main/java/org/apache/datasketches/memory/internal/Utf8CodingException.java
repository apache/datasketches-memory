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

package org.apache.datasketches.memory;

/**
 * This exception will be thrown for errors encountered during either the encoding of characters
 * to Utf8 bytes, or the decoding of Utf8 bytes to characters.
 *
 * @author Lee Rhodes
 */
public final class Utf8CodingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * A coding exception occured processing UTF_8
   * @param message the error message
   */
  public Utf8CodingException(final String message) {
    super(message);
  }

  //DECODE
  /**
   * Exception for a short UTF_8 Decode Byte Sequence
   * @param leadByte the given lead byte
   * @param address the given address
   * @param limit the given limit
   * @param required what is required
   * @return the exception
   */
  public static Utf8CodingException shortUtf8DecodeByteSequence(final byte leadByte, final long address,
      final long limit, final int required) {
    final String s = "Too few Utf8 decode bytes remaining given the leading byte. "
        + shortSeq(leadByte, address, limit, required);
    return new Utf8CodingException(s);
  }

  /**
   * Exception for an illegal UTF_8 Decode Byte Sequence
   * @param bytes the illegal byte sequence
   * @return the exception.
   */
  public static Utf8CodingException illegalUtf8DecodeByteSequence(final byte[] bytes) {
    final String s = "Invalid UTF-8 decode byte sequence: " + badBytes(bytes);
    return new Utf8CodingException(s);
  }

  //ENCODE
  /**
   * Exception for out-of-memory
   * @return the exception
   */
  public static Utf8CodingException outOfMemory() {
    final String s = "Out-of-memory with characters remaining to be encoded";
    return new Utf8CodingException(s);
  }

  /**
   * Exception for an unpaired surrogate
   * @param c The last char to encode is an unpaired surrogate
   * @return the exception plus the unpaired surrogate character
   */
  public static Utf8CodingException unpairedSurrogate(final char c) {
    final String s = "Last char to encode is an unpaired surrogate: 0X"
        + Integer.toHexString(c & 0XFFFF);
    return new Utf8CodingException(s);
  }

  /**
   * Exception for a short UTF_8 encode byte length
   * @param remaining The surrogate pair that is short
   * @return the exception plus the surrogate pair that is short
   */
  public static Utf8CodingException shortUtf8EncodeByteLength(final int remaining) {
    final String s = "Too few MemoryImpl bytes to encode a surrogate pair: " + remaining;
    return new Utf8CodingException(s);
  }

  /**
   * Exception for an illegal surrogate pair
   * @param c1 the first character of the pair
   * @param c2 the second character of the pair
   * @return the exception plus the illegal pair
   */
  public static Utf8CodingException illegalSurrogatePair(final char c1, final char c2) {
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
