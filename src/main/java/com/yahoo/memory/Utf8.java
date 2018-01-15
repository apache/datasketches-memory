package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.io.IOException;

/**
 * Encoding and decoding implementations of putUtf8 and getUtf8.
 * This is specifically designed to reduce the production of intermediate objects (garbage),
 * thus significantly reducing pressure on the JVM Garbage Collector.
 *
 * <p>UTF-8 encoding/decoding is based on
 * https://github.com/google/protobuf/blob/3e944aec9ebdf5043780fba751d604c0a55511f2/
 * java/core/src/main/java/com/google/protobuf/Utf8.java
 *
 * <p>Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
// NOTE: If you touch this class you need to enable the exhaustive Utf8Test.testThreeBytes()
//  method.
final class Utf8 {

  static final void getUtf8(final long offsetBytes, final Appendable dst, final int utf8Length,
      final ResourceState state) throws IOException, Utf8CodingException {
    assert state.isValid();

    //Why not use UnsafeUtil.assertBounds() like all other methods?
    checkBounds(offsetBytes, utf8Length, state.getCapacity());

    long address = state.getCumBaseOffset() + offsetBytes;
    final long addressLimit = address + utf8Length;
    final Object unsafeObj = state.getUnsafeObject();

    // Optimize for 100% ASCII (Hotspot loves small simple top-level loops like this).
    // This simple loop stops when we encounter a byte >= 0x80 (i.e. non-ASCII).
    while (address < addressLimit) {
      final byte b = unsafe.getByte(unsafeObj, address);
      if (!DecodeUtil.isOneByte(b)) {
        break;
      }
      address++;
      dst.append((char) b);
    }

    while (address < addressLimit) {
      final byte byte1 = unsafe.getByte(unsafeObj, address++);
      if (DecodeUtil.isOneByte(byte1)) {
        dst.append((char) byte1);
        // It's common for there to be multiple ASCII characters in a run mixed in, so add an
        // extra optimized loop to take care of these runs.
        while (address < addressLimit) {
          final byte b = unsafe.getByte(unsafeObj, address);
          if (!DecodeUtil.isOneByte(b)) {
            break;
          }
          address++;
          dst.append((char) b);
        }
      }
      else if (DecodeUtil.isTwoBytes(byte1)) {
        if (address >= addressLimit) {
          throw Utf8CodingException.inputBounds(address, addressLimit);
        }
        DecodeUtil.handleTwoBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            dst);
      }
      else if (DecodeUtil.isThreeBytes(byte1)) {
        if (address >= (addressLimit - 1)) {
          throw Utf8CodingException.inputBounds(address + 1, addressLimit);
        }
        DecodeUtil.handleThreeBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            /* byte3 */ unsafe.getByte(unsafeObj, address++),
            dst);
      }
      else {
        if (address >= (addressLimit - 2)) {
          throw Utf8CodingException.inputBounds(address + 2, addressLimit);
        }
        DecodeUtil.handleFourBytes(
            byte1,
            /* byte2 */ unsafe.getByte(unsafeObj, address++),
            /* byte3 */ unsafe.getByte(unsafeObj, address++),
            /* byte4 */ unsafe.getByte(unsafeObj, address++),
            dst);
      }
    }
  }

  static long putUtf8(final long offsetBytes, final CharSequence src, final ResourceState state) {
    assert state.isValid();
    final Object unsafeObj = state.getUnsafeObject();
    final long cumBaseOffset = state.getCumBaseOffset();

    long j = cumBaseOffset + offsetBytes; //absolute byte index in memory
    int i = 0; //src character index
    final long byteLimit = cumBaseOffset + state.getCapacity(); //absolute memory byte index limit

    final int utf16Length = src.length();
    //Quickly dispatch an ANCII sequence
    for (char c; (i < utf16Length) && ((i + j) < byteLimit) && ((c = src.charAt(i)) < 0x80); i++) {
      unsafe.putByte(unsafeObj, j + i, (byte) c);
    }
    if (i == utf16Length) {
      return (j + utf16Length) - cumBaseOffset; //done, return next relative byte index in memory
    }
    j += i;
    for (char c; i < utf16Length; i++) {
      c = src.charAt(i);
      if ((c < 0x80) && (j < byteLimit)) { //ASCII
        unsafe.putByte(unsafeObj, j++, (byte) c);
      }
      //c MUST BE >= 0x80 || j >= byteLimit (memory has 0 capacity)
      else if ((c < 0x800) && (j <= (byteLimit - 2))) { // 11 bits, two UTF-8 bytes.
        //We have target space for at least 2 bytes.
        //This is for almost all Latin-script alphabets plus Greek, Cyrillic, Hebrew, Arabic, etc..
        unsafe.putByte(unsafeObj, j++, (byte) ((0xF << 6) | (c >>> 6)));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & c)));
      }
      //c > 0x800 || j > byteLimit - 2
      else if (((c < Character.MIN_SURROGATE) || (Character.MAX_SURROGATE < c))
          && (j <= (byteLimit - 3))) { //Not a surrogate, AND we have target space for &ge; 3 bytes
        // Maximum single-char code point is 0xFFFF, 16 bits, three UTF-8 bytes for the rest of
        // the BMP.
        unsafe.putByte(unsafeObj, j++, (byte) ((0xF << 5) | (c >>> 12)));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & (c >>> 6))));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & c)));
      }
      //c is a surrogate || j > byteLimit - 3
      else if (j <= (byteLimit - 4)) { //We know we have target space for 4 bytes. Thus,
        //c and c+1 better be a surrogate pair.
        // Minimum code point represented by a surrogate pair is 0x10000, 17 bits,
        // four UTF-8 bytes, code planes 1 - 16.
        final char low;
        if (((i + 1) == utf16Length) //src too small for low surrogate pair; src bounds violation.
            // OR not a surrogate pair
            || !Character.isSurrogatePair(c, (low = src.charAt(++i)))) {
          throw new UnpairedSurrogateException((i - 1), utf16Length);
        }
        final int codePoint = Character.toCodePoint(c, low);
        unsafe.putByte(unsafeObj, j++, (byte) ((0xF << 4) | (codePoint >>> 18)));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & (codePoint >>> 12))));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & (codePoint >>> 6))));
        unsafe.putByte(unsafeObj, j++, (byte) (0x80 | (0x3F & codePoint)));
      }
      else { //We end up here if we have fewer than 4 bytes left in the target,
        // We must throw an exception, the question is which one.
        // If we are surrogates and we're not a surrogate pair, always throw an
        // UnpairedSurrogateException instead of an ArrayOutOfBoundsException.
        if (((Character.MIN_SURROGATE <= c) && (c <= Character.MAX_SURROGATE))
            && (((i + 1) == utf16Length) //src too small for low surrogate; bounds violation.
                || !Character.isSurrogatePair(c, src.charAt(i + 1)))) {
          throw new UnpairedSurrogateException(i, utf16Length);
        }
        final long localOffsetBytes = j - cumBaseOffset;
        throw new IllegalArgumentException(
           "Insufficient Memory. Failed writing character \"" + c
             + "\" at Memory byte offset " + localOffsetBytes);
      }
    }
    final long localOffsetBytes = j - cumBaseOffset;
    return localOffsetBytes;
  }

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
    private static boolean isOneByte(final byte b) {
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
     * All bytes must be &lt; 0xE0.
     *
     * @param b the byte being tested
     * @return true if this is the start of a two-byte UTF-8 encoding.
     */
    private static boolean isTwoBytes(final byte b) {
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
    private static boolean isThreeBytes(final byte b) {
      return b < (byte) 0xF0;
    }

    /*
     * Note that if three-byte UTF-8 coding has been excluded and if the current byte is
     * &ge; 0XF0, it must be four-byte UTF-8 encoding.
     * This is for the less common CJKV characters, historic scripts, math symbols, emoji, etc.
     *
     * <p>Code Plane1 1 through 16, Code Point range U+10000 to U+10FFFF.
     *
     * <p>Bit Patterns:
     * <ul><li>Byte 1: '11110xxx'</li>
     * <li>Byte 2: '10xxxxxx'</li>
     * <li>Byte 3: '10xxxxxx'</li>
     * <li>Byte 4: '10xxxxxx'</li>
     * </ul>
     */

    private static void handleTwoBytes(final byte byte1, final byte byte2, final Appendable dst)
        throws IOException, Utf8CodingException {
      // Simultaneously checks for illegal trailing-byte in leading position (<= '11000000') and
      // overlong 2-byte, '11000001'.
      if ((byte1 < (byte) 0xC2)
          || isNotTrailingByte(byte2)) {
        byte[] out = new byte[] {byte1, byte2};
        throw Utf8CodingException.inputBytes(out);
      }
      dst.append((char) (((byte1 & 0x1F) << 6) | trailingByteValue(byte2)));
    }

    private static void handleThreeBytes(final byte byte1, final byte byte2, final byte byte3,
        final Appendable dst) throws IOException, Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // overlong? 5 most significant bits must not all be zero
          || ((byte1 == (byte) 0xE0) && (byte2 < (byte) 0xA0))
          // check for illegal surrogate codepoints
          || ((byte1 == (byte) 0xED) && (byte2 >= (byte) 0xA0))
          || isNotTrailingByte(byte3)) {
        byte[] out = new byte[] {byte1, byte2, byte3};
        throw Utf8CodingException.inputBytes(out);
      }
      dst.append((char)
          (((byte1 & 0x0F) << 12) | (trailingByteValue(byte2) << 6) | trailingByteValue(byte3)));
    }

    private static void handleFourBytes(
        final byte byte1, final byte byte2, final byte byte3, final byte byte4,
        final Appendable dst) throws IOException, Utf8CodingException {
      if (isNotTrailingByte(byte2)
          // Check that 1 <= plane <= 16.  Tricky optimized form of:
          //   valid 4-byte leading byte?
          // if (byte1 > (byte) 0xF4 ||
          //   overlong? 4 most significant bits must not all be zero
          //     byte1 == (byte) 0xF0 && byte2 < (byte) 0x90 ||
          //   codepoint larger than the highest code point (U+10FFFF)?
          //     byte1 == (byte) 0xF4 && byte2 > (byte) 0x8F)
          || ((((byte1 << 28) + (byte2 - (byte) 0x90)) >> 30) != 0)
          || isNotTrailingByte(byte3)
          || isNotTrailingByte(byte4)) {
        byte[] out = new byte[] {byte1, byte2, byte3, byte4};
        throw Utf8CodingException.inputBytes(out);
      }
      final int codepoint = ((byte1 & 0x07) << 18)
                      | (trailingByteValue(byte2) << 12)
                      | (trailingByteValue(byte3) << 6)
                      | trailingByteValue(byte4);
      dst.append(DecodeUtil.highSurrogate(codepoint));
      dst.append(DecodeUtil.lowSurrogate(codepoint));
    }

    /**
     * Returns whether the byte is not a valid continuation of the form '10XXXXXX'.
     */
    private static boolean isNotTrailingByte(final byte b) {
      return b > (byte) 0xBF;
    }

    /**
     * Returns the actual value of the trailing byte (removes the prefix '10') for composition.
     */
    private static int trailingByteValue(final byte b) {
      return b & 0x3F;
    }

    private static char highSurrogate(final int codePoint) {
      return (char) ((Character.MIN_HIGH_SURROGATE
                      - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10))
                     + (codePoint >>> 10));
    }

    private static char lowSurrogate(final int codePoint) {
      return (char) (Character.MIN_LOW_SURROGATE + (codePoint & 0x3ff));
    }
  }

  static class UnpairedSurrogateException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    UnpairedSurrogateException(final int index, final int length) {
      //if index + 1 == length, src too small for low surrogate; bounds violation.
      // else not a surrogate pair.
      super("Unpaired surrogate at character index " + index + " of " + length);
    }
  }

}
