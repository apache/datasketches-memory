/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.protobuf.ByteString;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.CharBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapted version of
 * https://github.com/google/protobuf/blob/3e944aec9ebdf5043780fba751d604c0a55511f2/
 * java/core/src/test/java/com/google/protobuf/DecodeUtf8Test.java
 *
 * Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 */
public class Utf8Test {

  @Test
  public void testRoundTripAllValidChars() throws IOException { //the non-surrogate chars
    for (int i = Character.MIN_CODE_POINT; i < Character.MAX_CODE_POINT; i++) {
      if ((i < Character.MIN_SURROGATE) || (i > Character.MAX_SURROGATE)) {
        String str = new String(Character.toChars(i));
        assertRoundTrips(str);
      }
    }
  }

  @Test
  public void testPutInvalidChars() { //The surrogates must be a pair, thus invalid alone
    WritableMemory mem = WritableMemory.allocate(10);
    WritableMemory emptyMem = WritableMemory.allocate(0);
    for (int c = Character.MIN_SURROGATE; c <= Character.MAX_SURROGATE; c++) {
      assertSurrogate(mem, (char) c);
      assertSurrogate(emptyMem, (char) c);
    }
  }

  private static void assertSurrogate(WritableMemory mem, char c) {
    try {
      mem.putCharsToUtf8(0, new String(new char[] {c}));
      fail();
    } catch (Utf8CodingException e) {
      // Expected.
    }
  }

  @Test
  public void testPutInvaidSurrogatePairs() {
    WritableMemory mem = WritableMemory.allocate(4);
    StringBuilder sb = new StringBuilder();
    sb.append(Character.MIN_HIGH_SURROGATE);
    sb.append(Character.MAX_HIGH_SURROGATE);
    try {
      mem.putCharsToUtf8(0, sb);
    } catch (Utf8CodingException e) {
      //Expected;
    }
  }

  @Test
  public void testPutHighBMP() {
    WritableMemory mem = WritableMemory.allocate(2);
    StringBuilder sb = new StringBuilder();
    sb.append("\uE000");
    try {
      mem.putCharsToUtf8(0, sb);
    } catch (Utf8CodingException e) {
      //Expected;
    }
  }

  @Test
  public void testPutExtendedAscii() {
    WritableMemory mem = WritableMemory.allocate(1);
    StringBuilder sb = new StringBuilder();
    sb.append("\u07FF");
    try {
      mem.putCharsToUtf8(0, sb);
    } catch (Utf8CodingException e) {
      //Expected;
    }
  }

  @Test
  public void testPutOneAsciiToEmpty() {
    WritableMemory mem = WritableMemory.allocate(0);
    StringBuilder sb = new StringBuilder();
    sb.append("a");
    try {
      mem.putCharsToUtf8(0, sb);
    } catch (Utf8CodingException e) {
      //Expected;
    }
  }

  @Test
  public void testPutValidSurrogatePair() {
    WritableMemory mem = WritableMemory.allocate(4);
    StringBuilder sb = new StringBuilder();
    sb.append(Character.MIN_HIGH_SURROGATE);
    sb.append(Character.MIN_LOW_SURROGATE);
    mem.putCharsToUtf8(0, sb);
  }

  // Test all 1, 2, 3 invalid byte combinations. Valid ones would have been covered above.

  @Test
  public void testOneByte() {
    int valid = 0;
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      ByteString bs = ByteString.copyFrom(new byte[] {(byte) i });
      if (!bs.isValidUtf8()) { //from -128 to -1
        assertInvalid(bs.toByteArray());
      } else {
        valid++; //from 0 to 127
      }
    }
    assertEquals(IsValidUtf8TestUtil.EXPECTED_ONE_BYTE_ROUNDTRIPPABLE_COUNT, valid);
  }

  @Test
  public void testTwoBytes() {
    int valid = 0;
    for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
      for (int j = Byte.MIN_VALUE; j <= Byte.MAX_VALUE; j++) {
        ByteString bs = ByteString.copyFrom(new byte[]{(byte) i, (byte) j});
        if (!bs.isValidUtf8()) {
          assertInvalid(bs.toByteArray());
        } else {
          valid++;
        }
      }
    }
    assertEquals(IsValidUtf8TestUtil.EXPECTED_TWO_BYTE_ROUNDTRIPPABLE_COUNT, valid);
  }

  //@Test  //This test is very long, but should be enabled with any changes to Utf8 class.
  public void testThreeBytes() {
    // Travis' OOM killer doesn't like this test
    if (System.getenv("TRAVIS") == null) {
      int count = 0;
      int valid = 0;
      for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
        for (int j = Byte.MIN_VALUE; j <= Byte.MAX_VALUE; j++) {
          for (int k = Byte.MIN_VALUE; k <= Byte.MAX_VALUE; k++) {
            byte[] bytes = new byte[]{(byte) i, (byte) j, (byte) k};
            ByteString bs = ByteString.copyFrom(bytes);
            if (!bs.isValidUtf8()) {
              assertInvalid(bytes);
            } else {
              valid++;
            }
            count++;
            if ((count % 1000000L) == 0) {
              println("Processed " + (count / 1000000L) + " million characters");
            }
          }
        }
      }
      assertEquals(IsValidUtf8TestUtil.EXPECTED_THREE_BYTE_ROUNDTRIPPABLE_COUNT, valid);
    }
  }

  /**
   * Tests that round tripping of a sample of four byte permutations work.
   */
  @Test
  public void testInvalid_4BytesSamples() {
    // Bad trailing bytes
    assertInvalid(0xF0, 0xA4, 0xAD, 0x7F);
    assertInvalid(0xF0, 0xA4, 0xAD, 0xC0);

    // Special cases for byte2
    assertInvalid(0xF0, 0x8F, 0xAD, 0xA2);
    assertInvalid(0xF4, 0x90, 0xAD, 0xA2);
  }

  @Test
  public void testRealStrings() throws IOException {
    // English
    assertRoundTrips("The quick brown fox jumps over the lazy dog");
    // German
    assertRoundTrips("Quizdeltagerne spiste jordb\u00e6r med fl\u00f8de, mens cirkusklovnen");
    // Japanese
    assertRoundTrips(
        "\u3044\u308d\u306f\u306b\u307b\u3078\u3068\u3061\u308a\u306c\u308b\u3092");
    // Hebrew
    assertRoundTrips(
        "\u05d3\u05d2 \u05e1\u05e7\u05e8\u05df \u05e9\u05d8 \u05d1\u05d9\u05dd "
        + "\u05de\u05d0\u05d5\u05db\u05d6\u05d1 \u05d5\u05dc\u05e4\u05ea\u05e2"
        + " \u05de\u05e6\u05d0 \u05dc\u05d5 \u05d7\u05d1\u05e8\u05d4 "
        + "\u05d0\u05d9\u05da \u05d4\u05e7\u05dc\u05d9\u05d8\u05d4");
    // Thai
    assertRoundTrips(
        " \u0e08\u0e07\u0e1d\u0e48\u0e32\u0e1f\u0e31\u0e19\u0e1e\u0e31\u0e12"
        + "\u0e19\u0e32\u0e27\u0e34\u0e0a\u0e32\u0e01\u0e32\u0e23");
    // Chinese
    assertRoundTrips(
        "\u8fd4\u56de\u94fe\u4e2d\u7684\u4e0b\u4e00\u4e2a\u4ee3\u7406\u9879\u9009\u62e9\u5668");
    // Chinese with 4-byte chars
    assertRoundTrips("\uD841\uDF0E\uD841\uDF31\uD841\uDF79\uD843\uDC53\uD843\uDC78"
                     + "\uD843\uDC96\uD843\uDCCF\uD843\uDCD5\uD843\uDD15\uD843\uDD7C\uD843\uDD7F"
                     + "\uD843\uDE0E\uD843\uDE0F\uD843\uDE77\uD843\uDE9D\uD843\uDEA2");
    // Mixed
    assertRoundTrips(
        "The quick brown \u3044\u308d\u306f\u306b\u307b\u3078\u8fd4\u56de\u94fe"
        + "\u4e2d\u7684\u4e0b\u4e00");
  }

  @Test
  public void testOverlong() {
    assertInvalid(0xc0, 0xaf);
    assertInvalid(0xe0, 0x80, 0xaf);
    assertInvalid(0xf0, 0x80, 0x80, 0xaf);

    // Max overlong
    assertInvalid(0xc1, 0xbf);
    assertInvalid(0xe0, 0x9f, 0xbf);
    assertInvalid(0xf0 ,0x8f, 0xbf, 0xbf);

    // null overlong
    assertInvalid(0xc0, 0x80);
    assertInvalid(0xe0, 0x80, 0x80);
    assertInvalid(0xf0, 0x80, 0x80, 0x80);
  }

  @Test
  public void testIllegalCodepoints() {
    // Single surrogate
    assertInvalid(0xed, 0xa0, 0x80);
    assertInvalid(0xed, 0xad, 0xbf);
    assertInvalid(0xed, 0xae, 0x80);
    assertInvalid(0xed, 0xaf, 0xbf);
    assertInvalid(0xed, 0xb0, 0x80);
    assertInvalid(0xed, 0xbe, 0x80);
    assertInvalid(0xed, 0xbf, 0xbf);

    // Paired surrogates
    assertInvalid(0xed, 0xa0, 0x80, 0xed, 0xb0, 0x80);
    assertInvalid(0xed, 0xa0, 0x80, 0xed, 0xbf, 0xbf);
    assertInvalid(0xed, 0xad, 0xbf, 0xed, 0xb0, 0x80);
    assertInvalid(0xed, 0xad, 0xbf, 0xed, 0xbf, 0xbf);
    assertInvalid(0xed, 0xae, 0x80, 0xed, 0xb0, 0x80);
    assertInvalid(0xed, 0xae, 0x80, 0xed, 0xbf, 0xbf);
    assertInvalid(0xed, 0xaf, 0xbf, 0xed, 0xb0, 0x80);
    assertInvalid(0xed, 0xaf, 0xbf, 0xed, 0xbf, 0xbf);
  }

  @Test
  public void testBufferSlice() throws IOException {
    String str = "The quick brown fox jumps over the lazy dog";
    assertRoundTrips(str, 10, 4);
    assertRoundTrips(str, str.length(), 0);
  }

  @Test
  public void testInvalidBufferSlice() { //these are pure Memory bounds violations
    byte[] bytes  = "The quick brown fox jumps over the lazy dog".getBytes(UTF_8);
    assertInvalidSlice(bytes, bytes.length - 3, 4);
    assertInvalidSlice(bytes, bytes.length, 1);
    assertInvalidSlice(bytes, bytes.length + 1, 0);
    assertInvalidSlice(bytes, 0, bytes.length + 1);
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }

  private static void assertInvalid(int... bytesAsInt) { //invalid byte sequences
    byte[] bytes = new byte[bytesAsInt.length];
    for (int i = 0; i < bytesAsInt.length; i++) {
      bytes[i] = (byte) bytesAsInt[i];
    }
    assertInvalid(bytes);
  }

  private static void assertInvalid(byte[] bytes) {
    try {
      Memory.wrap(bytes).getCharsFromUtf8(0, bytes.length, new StringBuilder());
      fail();
    } catch (Utf8CodingException e) {
      // Expected.
    }
  }

  private static void assertInvalidSlice(byte[] bytes, int index, int size) {
    try {
      Memory mem = Memory.wrap(bytes);
      mem.getCharsFromUtf8(index, size, new StringBuilder());
      fail();
    } catch (IllegalArgumentException e) { //Pure bounds violation
      // Expected.
    }
  }

  private static void assertRoundTrips(String str) throws IOException {
    assertRoundTrips(str, 0, -1);
  }


  private static void assertRoundTrips(String str, int index, int size) throws IOException {
    byte[] bytes = str.getBytes(UTF_8);
    if (size == -1) {
      size = bytes.length;
    }
    Memory mem = Memory.wrap(bytes);

    byte[] bytes2 = new byte[bytes.length + 1];
    System.arraycopy(bytes, 0, bytes2, 1, bytes.length);
    Memory mem2 = Memory.wrap(bytes2).region(1, bytes.length);

    WritableMemory writeMem = WritableMemory.allocate(bytes.length);
    WritableMemory writeMem2 =
            WritableMemory.allocate(bytes.length + 1).writableRegion(1, bytes.length);

    // Test with Memory objects, where base offset != 0
    assertRoundTrips(str, index, size, bytes, mem, writeMem);
    assertRoundTrips(str, index, size, bytes, mem, writeMem2);
    assertRoundTrips(str, index, size, bytes, mem2, writeMem);
    assertRoundTrips(str, index, size, bytes, mem2, writeMem2);
  }

  private static void assertRoundTrips(String str, int index, int size, byte[] bytes, Memory mem,
        WritableMemory writeMem) throws IOException {
    StringBuilder sb = new StringBuilder();

    mem.getCharsFromUtf8(index, size, sb);
    checkStrings(sb.toString(), new String(bytes, index, size, UTF_8));

    CharBuffer cb = CharBuffer.allocate(bytes.length + 1);
    cb.position(1);
    // Make CharBuffer 1-based, to check correct offset handling
    cb = cb.slice();
    mem.getCharsFromUtf8(index, size, cb);
    cb.flip();
    checkStrings(cb.toString(), new String(bytes, index, size, UTF_8));

    assertEquals(writeMem.putCharsToUtf8(0, str), bytes.length);
    assertEquals(0, writeMem.compareTo(0, bytes.length, mem, 0, bytes.length));

    // Test write overflow
    WritableMemory writeMem2 = WritableMemory.allocate(bytes.length - 1);
    try {
      writeMem2.putCharsToUtf8(0, str);
      fail();
    } catch (Utf8CodingException e) {
      // Expected.
    }
  }

  private static void checkStrings(String actual, String expected) {
    if (!expected.equals(actual)) {
      fail("Failure: Expected (" + codepoints(expected) + ") Actual (" + codepoints(actual) + ")");
    }
  }

  private static List<String> codepoints(String str) {
    List<String> codepoints = new ArrayList<>();
    for (int i = 0; i < str.length(); i++) {
      codepoints.add(Long.toHexString(str.charAt(i)));
    }
    return codepoints;
  }

}
