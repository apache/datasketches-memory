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

import static java.lang.Character.isSurrogate;
import static java.lang.Character.isSurrogatePair;
import static java.lang.Character.toCodePoint;
import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.Utf8CodingException;
import org.apache.datasketches.memory.WritableMemory;

/**
 * Encoding and decoding implementations of {@link WritableMemory#putCharsToUtf8} and
 * {@link Memory#getCharsFromUtf8}.
 *
 * <p>This is specifically designed to reduce the production of intermediate objects (garbage),
 * thus significantly reducing pressure on the JVM Garbage Collector.
 *
 * <p>UTF-8 encoding/decoding is adapted from
 * https://github.com/protocolbuffers/protobuf/blob/master/java/core/src/main/java/com/google/protobuf/Utf8.java
 *
 * <p>Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 * See LICENSE.
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
@SuppressWarnings("restriction")
final class Utf8 {

  private Utf8() { }

  //Decode
  static final int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final Appendable dst, final long cumBaseOffset, final Object unsafeObj)
          throws IOException, Utf8CodingException {

    if ((dst instanceof CharBuffer) && ((CharBuffer) dst).hasArray()) {
      return getCharBufferCharsFromUtf8(offsetBytes, ((CharBuffer) dst), utf8LengthBytes,
          cumBaseOffset, unsafeObj);
    }

    //Decode Direct CharBuffers and all other Appendables

    final long address = cumBaseOffset + offsetBytes;

    // Optimize for 100% ASCII (Hotspot loves small simple top-level loops like this).
    // This simple loop stops when we encounter a byte >= 0x80 (i.e. non-ASCII).
    // Need to keep this loop int-indexed, because it's faster for Hotspot JIT, it doesn't insert
    // savepoint polls on each iteration.
    int i = 0;
    for (; i < utf8LengthBytes; i++) {
      final byte b = unsafe.getByte(unsafeObj, address + i);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      dst.append((char) b);
    }
    if (i == utf8LengthBytes) {
      return i;
    }
    return getNonAsciiCharsFromUtf8(dst, address + i, address + utf8LengthBytes, unsafeObj,
        cumBaseOffset) + i;
  }

  /*
   * Optimize for heap CharBuffer manually, because Hotspot JIT doesn't itself unfold this
   * abstraction well (doesn't hoist array bound checks, etc.)
   */
  private static int getCharBufferCharsFromUtf8(final long offsetBytes, final CharBuffer cbuf,
        final int utf8LengthBytes, final long cumBaseOffset, final Object unsafeObj) {
    final char[] carr = cbuf.array();
    final int startCpos = cbuf.position() + cbuf.arrayOffset();
    int cpos = startCpos;
    final int clim = cbuf.arrayOffset() + cbuf.limit();
    final long address = cumBaseOffset + offsetBytes;
    int i = 0; //byte index

    // Optimize for 100% ASCII (Hotspot loves small simple top-level loops like this).
    // This simple loop stops when we encounter a byte >= 0x80 (i.e. non-ASCII).
    final int cbufNoCheckLimit = Math.min(utf8LengthBytes, clim - cpos);
    // Need to keep this loop int-indexed, because it's faster for Hotspot JIT, it doesn't insert
    // savepoint polls on each iteration.
    for (; i < cbufNoCheckLimit; i++) {
      final byte b = unsafe.getByte(unsafeObj, address + i);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      // Not checking CharBuffer bounds!
      carr[cpos++] = (char) b;
    }

    for (; i < utf8LengthBytes; i++) {
      final byte b = unsafe.getByte(unsafeObj, address + i);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      checkCharBufferPos(cbuf, cpos, clim);
      carr[cpos++] = (char) b;
    }
    if (i == utf8LengthBytes) {
      cbuf.position(cpos - cbuf.arrayOffset());
      return cpos - startCpos;
    }

    return getCharBufferNonAsciiCharsFromUtf8(cbuf, carr, cpos, clim, address + i,
        address + utf8LengthBytes, unsafeObj, cumBaseOffset) - cbuf.arrayOffset();
  }

  private static int getCharBufferNonAsciiCharsFromUtf8(final CharBuffer cbuf, final char[] carr,
      int cpos, final int clim, long address, final long addressLimit, final Object unsafeObj,
      final long cumBaseOffset) {

    while (address < addressLimit) {
      final byte byte1 = unsafe.getByte(unsafeObj, address++);
      if (DecodeUtil.isOneByte(byte1)) {
        checkCharBufferPos(cbuf, cpos, clim);
        carr[cpos++] = (char) byte1;
        // It's common for there to be multiple ASCII characters in a run mixed in, so add an
        // extra optimized loop to take care of these runs.
        while (address < addressLimit) {
          final byte b = unsafe.getByte(unsafeObj, address);
          if (!DecodeUtil.isOneByte(b)) {
            break;
          }
          address++;
          checkCharBufferPos(cbuf, cpos, clim);
          carr[cpos++] = (char) b;
        }
      }
      else if (DecodeUtil.isTwoBytes(byte1)) {
        if (address >= addressLimit) {
          cbuf.position(cpos - cbuf.arrayOffset());
          final long off = address - cumBaseOffset;
          final long limit = addressLimit - cumBaseOffset;
          throw Utf8CodingException.shortUtf8DecodeByteSequence(byte1, off, limit, 2);
        }
        checkCharBufferPos(cbuf, cpos, clim);
        DecodeUtil.handleTwoBytesCharBuffer(
          byte1,
          /* byte2 */ unsafe.getByte(unsafeObj, address++),
          cbuf, carr, cpos);
        cpos++;
      }
      else if (DecodeUtil.isThreeBytes(byte1)) {
        if (address >= (addressLimit - 1)) {
          cbuf.position(cpos - cbuf.arrayOffset());
          final long off = address - cumBaseOffset;
          final long limit = addressLimit - cumBaseOffset;
          throw Utf8CodingException.shortUtf8DecodeByteSequence(byte1, off, limit, 3);
        }
        checkCharBufferPos(cbuf, cpos, clim);
        DecodeUtil.handleThreeBytesCharBuffer(
          byte1,
          /* byte2 */ unsafe.getByte(unsafeObj, address++),
          /* byte3 */ unsafe.getByte(unsafeObj, address++),
          cbuf, carr, cpos);
        cpos++;
      }
      else {
        if (address >= (addressLimit - 2)) {
          cbuf.position(cpos - cbuf.arrayOffset());
          final long off = address - cumBaseOffset;
          final long limit = addressLimit - cumBaseOffset;
          throw Utf8CodingException.shortUtf8DecodeByteSequence(byte1, off, limit, 4);
        }
        if (cpos >= (clim - 1)) {
          cbuf.position(cpos - cbuf.arrayOffset());
          throw new BufferOverflowException();
        }
        DecodeUtil.handleFourBytesCharBuffer(
          byte1,
          /* byte2 */ unsafe.getByte(unsafeObj, address++),
          /* byte3 */ unsafe.getByte(unsafeObj, address++),
          /* byte4 */ unsafe.getByte(unsafeObj, address++),
          cbuf, carr, cpos);
        cpos += 2;
      }
    }
    cbuf.position(cpos - cbuf.arrayOffset());
    return cpos;
  }

  //Decodes into Appendable destination
  //returns num of chars decoded
  private static int getNonAsciiCharsFromUtf8(final Appendable dst, long address,
      final long addressLimit, final Object unsafeObj, final long cumBaseOffset)
          throws IOException {
    int chars = 0;
    while (address < addressLimit) {
      final byte byte1 = unsafe.getByte(unsafeObj, address++);
      if (DecodeUtil.isOneByte(byte1)) {
        dst.append((char) byte1);
        chars++;
        // It's common for there to be multiple ASCII characters in a run mixed in, so add an
        // extra optimized loop to take care of these runs.
        while (address < addressLimit) {
          final byte b = unsafe.getByte(unsafeObj, address);
          if (!DecodeUtil.isOneByte(b)) {
            break;
          }
          address++;
          dst.append((char) b);
          chars++;
        }
      }
      else if (DecodeUtil.isTwoBytes(byte1)) {
        if (address >= addressLimit) {
          final long off = address - cumBaseOffset;
          final long limit = addressLimit - cumBaseOffset;
          throw Utf8CodingException.shortUtf8DecodeByteSequence(byte1, off, limit, 2);
        }
        DecodeUtil.handleTwoBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            dst);
        chars++;
      }
      else if (DecodeUtil.isThreeBytes(byte1)) {
        if (address >= (addressLimit - 1)) {
          final long off = address - cumBaseOffset;
          final long limit = addressLimit - cumBaseOffset;
          throw Utf8CodingException.shortUtf8DecodeByteSequence(byte1, off, limit, 3);
        }
        DecodeUtil.handleThreeBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            /* byte3 */ unsafe.getByte(unsafeObj, address++),
            dst);
        chars++;
      }
      else {
        if (address >= (addressLimit - 2)) {
          final long off = address - cumBaseOffset;
          final long limit = addressLimit - cumBaseOffset;
          throw Utf8CodingException.shortUtf8DecodeByteSequence(byte1, off, limit, 4);
        }
        DecodeUtil.handleFourBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            /* byte3 */ unsafe.getByte(unsafeObj, address++),
            /* byte4 */ unsafe.getByte(unsafeObj, address++),
            dst);
        chars += 2;
      }
    }
    return chars;
  }

  private static void checkCharBufferPos(final CharBuffer cbuf, final int cpos, final int clim) {
    if (cpos == clim) {
      cbuf.position(cpos - cbuf.arrayOffset());
      throw new BufferOverflowException();
    }
  }

  /******************/
  //Encode
  static long putCharsToUtf8(final long offsetBytes, final CharSequence src,
        final long capacityBytes, final long cumBaseOffset, final Object unsafeObj) {


    int cIdx = 0; //src character index
    long bIdx = cumBaseOffset + offsetBytes; //byte index
    long bCnt = 0; //bytes inserted

    final long byteLimit = cumBaseOffset + capacityBytes; //unsafe index limit

    final int utf16Length = src.length();
    //Quickly dispatch an ASCII sequence
    for (char c;
        (cIdx < utf16Length) && ((cIdx + bIdx) < byteLimit) && ((c = src.charAt(cIdx)) < 0x80);
        cIdx++, bCnt++) {
      unsafe.putByte(unsafeObj, bIdx + cIdx, (byte) c);
    }
    //encountered a non-ascii character
    if (cIdx == utf16Length) { //done.
      // next relative byte index in memory is (bIdx + utf16Length) - cumBaseOffset.
      return bCnt;
    }
    bIdx += cIdx; //bytes == characters for ascii

    for (char c; cIdx < utf16Length; cIdx++) { //process the remaining characters
      c = src.charAt(cIdx);

      if ((c < 0x80) && (bIdx < byteLimit)) {
        //Encode ASCII, 0 through 0x007F.
        unsafe.putByte(unsafeObj, bIdx++, (byte) c);
        bCnt++;
      }

      else
      //c MUST BE >= 0x0080 || j >= byteLimit

      if ((c < 0x800) && (bIdx < (byteLimit - 1))) {
        //Encode 0x80 through 0x7FF.
        //This is for almost all Latin-script alphabets plus Greek, Cyrillic, Hebrew, Arabic, etc.
        //We must have target space for at least 2 Utf8 bytes.
        unsafe.putByte(unsafeObj, bIdx++, (byte) ((0xF << 6) | (c >>> 6)));
        unsafe.putByte(unsafeObj, bIdx++, (byte) (0x80 | (0x3F & c)));
        bCnt += 2;
      }

      else
      //c > 0x800 || j >= byteLimit - 1 || j >= byteLimit

      if ( !isSurrogate(c) && (bIdx < (byteLimit - 2)) ) {
        //Encode the remainder of the BMP that are not surrogates:
        //  0x0800 thru 0xD7FF; 0xE000 thru 0xFFFF, the max single-char code point
        //We must have target space for at least 3 Utf8 bytes.
        unsafe.putByte(unsafeObj, bIdx++, (byte) ((0xF << 5) | (c >>> 12)));
        unsafe.putByte(unsafeObj, bIdx++, (byte) (0x80 | (0x3F & (c >>> 6))));
        unsafe.putByte(unsafeObj, bIdx++, (byte) (0x80 | (0x3F & c)));
        bCnt += 3;
      }

      else {
        //c is a surrogate || j >= byteLimit - 2 || j >= byteLimit - 1 || j >= byteLimit

        //At this point we are either:
        // 1) Attempting to encode Code Points outside the BMP.
        //
        //    The only way to properly encode code points outside the BMP into Utf8 bytes is to use
        //    High/Low pairs of surrogate characters. Therefore, we must have at least 2 source
        //    characters remaining, at least 4 bytes of memory space remaining, and the next 2
        //    characters must be a valid surrogate pair.
        //
        // 2) There is insufficient MemoryImpl space to encode the current character from one of the
        //    ifs above.
        //
        // We proceed assuming (1). If the following test fails, we move to an exception.

        final char low;
        if ( (cIdx <= (utf16Length - 2))
            && (bIdx <= (byteLimit - 4))
            && isSurrogatePair(c, low = src.charAt(cIdx + 1)) ) { //we are good
          cIdx++; //skip over low surrogate
          final int codePoint = toCodePoint(c, low);
          unsafe.putByte(unsafeObj, bIdx++, (byte) ((0xF << 4) | (codePoint >>> 18)));
          unsafe.putByte(unsafeObj, bIdx++, (byte) (0x80 | (0x3F & (codePoint >>> 12))));
          unsafe.putByte(unsafeObj, bIdx++, (byte) (0x80 | (0x3F & (codePoint >>> 6))));
          unsafe.putByte(unsafeObj, bIdx++, (byte) (0x80 | (0x3F & codePoint)));
          bCnt += 4;
        }

        else {
          //We are going to throw an exception. So we have time to figure out
          // what was wrong and hopefully throw an intelligent message!

          //check the BMP code point cases and their required memory limits
          if (   ((c < 0X0080) && (bIdx >= byteLimit))
              || ((c < 0x0800) && (bIdx >= (byteLimit - 1)))
              || ((c < 0xFFFF) && (bIdx >= (byteLimit - 2))) ) {
            throw Utf8CodingException.outOfMemory();
          }

          if (cIdx > (utf16Length - 2)) { //the last char is an unpaired surrogate
            throw Utf8CodingException.unpairedSurrogate(c);
          }

          if (bIdx > (byteLimit - 4)) {
            //4 MemoryImpl bytes required to encode a surrogate pair.
            final int remaining = (int) ((bIdx - byteLimit) + 4L);
            throw Utf8CodingException.shortUtf8EncodeByteLength(remaining);
          }

          if (!isSurrogatePair(c, src.charAt(cIdx + 1)) ) {
            //Not a surrogate pair.
            throw Utf8CodingException.illegalSurrogatePair(c, src.charAt(cIdx + 1));
          }

          //This should not happen :)
          throw new IllegalArgumentException("Unknown Utf8 encoding exception");
        }
      }
    }
    //final long localOffsetBytes = bIdx - cumBaseOffset;
    return bCnt;
  }

  /*****************/
  /**
   * Utility methods for decoding UTF-8 bytes into {@link String}. Callers are responsible for
   * extracting bytes (possibly using Unsafe methods), and checking remaining bytes. All other
   * UTF-8 validity checks and codepoint conversions happen in this class.
   *
   * @see <a href="https://en.wikipedia.org/wiki/UTF-8">Wikipedia: UTF-8</a>
   */
  private static class DecodeUtil {

    /**
     * Returns whether this is a single-byte UTF-8 encoding.
     * This is for ASCII.
     *
     * <p>Code Plane 0, Code Point range U+0000 to U+007F.
     *
     * <p>Bit Patterns:
     * <ul><li>Byte 1: '0xxxxxxx'<li>
     * </ul>
     * @param b the byte being tested
     * @return true if this is a single-byte UTF-8 encoding, i.e., b is &ge; 0.
     */
    static boolean isOneByte(final byte b) {
      return b >= 0;
    }

    /**
     * Returns whether this is the start of a two-byte UTF-8 encoding. One-byte encoding must
     * already be excluded.
     * This is for almost all Latin-script alphabets plus Greek, Cyrillic, Hebrew, Arabic, etc.
     *
     * <p>Code Plane 0, Code Point range U+0080 to U+07FF.
     *
     * <p>Bit Patterns:
     * <ul><li>Byte 1: '110xxxxx'</li>
     * <li>Byte 2: '10xxxxxx'</li>
     * </ul>
     *
     * <p>All bytes must be &lt; 0xE0.
     *
     * @param b the byte being tested
     * @return true if this is the start of a two-byte UTF-8 encoding.
     */
    static boolean isTwoBytes(final byte b) {
      return b < (byte) 0xE0;
    }

    /**
     * Returns whether this is the start of a three-byte UTF-8 encoding. Two-byte encoding must
     * already be excluded.
     * This is for the rest of the BMP, which includes most common Chinese, Japanese and Korean
     * characters.
     *
     * <p>Code Plane 0, Code Point range U+0800 to U+FFFF.
     *
     * <p>Bit Patterns:
     * <ul><li>Byte 1: '1110xxxx'</li>
     * <li>Byte 2: '10xxxxxx'</li>
     * <li>Byte 3: '10xxxxxx'</li>
     * </ul>
     * All bytes must be less than 0xF0.
     *
     * @param b the byte being tested
     * @return true if this is the start of a three-byte UTF-8 encoding, i.e., b &ge; 0XF0.
     */
    static boolean isThreeBytes(final byte b) {
      return b < (byte) 0xF0;
    }

    /*
     * Note that if three-byte UTF-8 coding has been excluded and if the current byte is
     * &ge; 0XF0, it must be the start of a four-byte UTF-8 encoding.
     * This is for the less common CJKV characters, historic scripts, math symbols, emoji, etc.
     *
     * <p>Code Plane 1 through 16, Code Point range U+10000 to U+10FFFF.
     *
     * <p>Bit Patterns:
     * <ul><li>Byte 1: '11110xxx'</li>
     * <li>Byte 2: '10xxxxxx'</li>
     * <li>Byte 3: '10xxxxxx'</li>
     * <li>Byte 4: '10xxxxxx'</li>
     * </ul>
     */

    static void handleTwoBytes(
        final byte byte1, final byte byte2,
        final Appendable dst)
        throws IOException, Utf8CodingException {
      // Simultaneously checks for illegal trailing-byte in leading position (<= '11000000') and
      // overlong 2-byte, '11000001'.
      if ((byte1 < (byte) 0xC2)
          || isNotTrailingByte(byte2)) {
        final byte[] out = new byte[] {byte1, byte2};
        throw Utf8CodingException.illegalUtf8DecodeByteSequence(out);
      }
      dst.append((char) (((byte1 & 0x1F) << 6) | trailingByteValue(byte2)));
    }

    static void handleTwoBytesCharBuffer(
        final byte byte1, final byte byte2,
        final CharBuffer cb, final char[] ca, final int cp)
        throws Utf8CodingException {
      // Simultaneously checks for illegal trailing-byte in leading position (<= '11000000') and
      // overlong 2-byte, '11000001'.
      if ((byte1 < (byte) 0xC2)
          || isNotTrailingByte(byte2)) {
        final byte[] out = new byte[] {byte1, byte2};
        cb.position(cp - cb.arrayOffset());
        throw Utf8CodingException.illegalUtf8DecodeByteSequence(out);
      }
      ca[cp] = (char) (((byte1 & 0x1F) << 6) | trailingByteValue(byte2));
    }

    static void handleThreeBytes(
        final byte byte1, final byte byte2, final byte byte3,
        final Appendable dst)
        throws IOException, Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // overlong? 5 most significant bits must not all be zero
          || ((byte1 == (byte) 0xE0) && (byte2 < (byte) 0xA0))
          // check for illegal surrogate codepoints
          || ((byte1 == (byte) 0xED) && (byte2 >= (byte) 0xA0))
          || isNotTrailingByte(byte3)) {
        final byte[] out = new byte[] {byte1, byte2, byte3};
        throw Utf8CodingException.illegalUtf8DecodeByteSequence(out);
      }
      dst.append((char)
          (((byte1 & 0x0F) << 12) | (trailingByteValue(byte2) << 6) | trailingByteValue(byte3)));
    }

    static void handleThreeBytesCharBuffer(
        final byte byte1, final byte byte2, final byte byte3,
        final CharBuffer cb, final char[] ca, final int cp)
        throws Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // overlong? 5 most significant bits must not all be zero
          || ((byte1 == (byte) 0xE0) && (byte2 < (byte) 0xA0))
          // check for illegal surrogate codepoints
          || ((byte1 == (byte) 0xED) && (byte2 >= (byte) 0xA0))
          || isNotTrailingByte(byte3)) {
        cb.position(cp - cb.arrayOffset());
        final byte[] out = new byte[] {byte1, byte2, byte3};
        throw Utf8CodingException.illegalUtf8DecodeByteSequence(out);
      }
      ca[cp] = (char)
              (((byte1 & 0x0F) << 12) | (trailingByteValue(byte2) << 6) | trailingByteValue(byte3));
    }

    static void handleFourBytes(
        final byte byte1, final byte byte2, final byte byte3, final byte byte4,
        final Appendable dst)
        throws IOException, Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // Check that 1 <= plane <= 16. Tricky optimized form of:
          //   valid 4-byte leading byte?
          // if (byte1 > (byte) 0xF4 ||
          //   overlong? 4 most significant bits must not all be zero
          //     byte1 == (byte) 0xF0 && byte2 < (byte) 0x90 ||
          //   codepoint larger than the highest code point (U+10FFFF)?
          //     byte1 == (byte) 0xF4 && byte2 > (byte) 0x8F)
          || ((((byte1 << 28) + (byte2 - (byte) 0x90)) >> 30) != 0)
          || isNotTrailingByte(byte3)
          || isNotTrailingByte(byte4)) {
        final byte[] out = new byte[] { byte1, byte2, byte3, byte4 };
        throw Utf8CodingException.illegalUtf8DecodeByteSequence(out);
      }
      final int codepoint = ((byte1 & 0x07) << 18)
          | (trailingByteValue(byte2) << 12)
          | (trailingByteValue(byte3) << 6)
          | trailingByteValue(byte4);
      dst.append(DecodeUtil.highSurrogate(codepoint));
      dst.append(DecodeUtil.lowSurrogate(codepoint));
    }

    static void handleFourBytesCharBuffer(
        final byte byte1, final byte byte2, final byte byte3, final byte byte4,
        final CharBuffer cb, final char[] ca, final int cp)
        throws Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // Check that 1 <= plane <= 16. Tricky optimized form of:
          //   valid 4-byte leading byte?
          // if (byte1 > (byte) 0xF4 ||
          //   overlong? 4 most significant bits must not all be zero
          //     byte1 == (byte) 0xF0 && byte2 < (byte) 0x90 ||
          //   codepoint larger than the highest code point (U+10FFFF)?
          //     byte1 == (byte) 0xF4 && byte2 > (byte) 0x8F)
          || ((((byte1 << 28) + (byte2 - (byte) 0x90)) >> 30) != 0)
          || isNotTrailingByte(byte3)
          || isNotTrailingByte(byte4)) {
        cb.position(cp - cb.arrayOffset());
        final byte[] out = new byte[] { byte1, byte2, byte3, byte4 };
        throw Utf8CodingException.illegalUtf8DecodeByteSequence(out);
      }
      final int codepoint = ((byte1 & 0x07) << 18)
          | (trailingByteValue(byte2) << 12)
          | (trailingByteValue(byte3) << 6)
          | trailingByteValue(byte4);
      ca[cp] = DecodeUtil.highSurrogate(codepoint);
      ca[cp + 1] = DecodeUtil.lowSurrogate(codepoint);
    }

    /*
     * Returns whether the byte is not a valid continuation of the form '10XXXXXX'.
     */
    private static boolean isNotTrailingByte(final byte b) {
      return b > (byte) 0xBF;
    }

    /*
     * Returns the actual value of the trailing byte (removes the prefix '10') for composition.
     */
    private static int trailingByteValue(final byte b) {
      return b & 0x3F;
    }

    private static char highSurrogate(final int codePoint) {
      return (char)
          ((Character.MIN_HIGH_SURROGATE
          - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10))
          + (codePoint >>> 10));
    }

    private static char lowSurrogate(final int codePoint) {
      return (char) (Character.MIN_LOW_SURROGATE + (codePoint & 0x3ff));
    }
  }

}
