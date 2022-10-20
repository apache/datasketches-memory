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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.datasketches.memory.DefaultMemoryFactory;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import com.google.protobuf.ByteString;

/**
 * Adapted version of
 * https://github.com/protocolbuffers/protobuf/blob/master/java/core/src/test/java/com/google/protobuf/DecodeUtf8Test.java
 *
 * Copyright 2008 Google Inc.  All rights reserved.
 * https://developers.google.com/protocol-buffers/
 * See LICENSE.
 */
public class Utf8Test {

  @Test
  public void testRoundTripAllValidCodePoints() throws IOException { //the non-surrogate code pts
    for (int cp = Character.MIN_CODE_POINT; cp < Character.MAX_CODE_POINT; cp++) {
      if (!isSurrogateCodePoint(cp)) {
        String refStr = new String(Character.toChars(cp));
        assertRoundTrips(refStr);
      }
    }
  }

  @Test
  public void testPutInvalidChars() { //The surrogates must be a pair, thus invalid alone
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(10);
    WritableMemory emptyMem = DefaultMemoryFactory.DEFAULT.allocate(0);
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
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(4);
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
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(2);
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
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(1);
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
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(0);
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
    WritableMemory mem = DefaultMemoryFactory.DEFAULT.allocate(4);
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

  //@Test
  //This test is very long, and doesn't cover the 4-byte combinations.
  // This is replaced by the test following which does cover some 4-byte combinations.
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

  /* These code points can be used by the following test to customize different regions of the
   * Code Point space. This randomized test can replace the exhaustive
   * combinatorially explosive previous test, which doesn't cover the 4 byte combinations.
   */
  static final int min1ByteCP   = 0; //ASCII
  static final int min2ByteCP   = 0X000080;
  static final int min3ByteCP   = 0X000800;
  static final int min4ByteCP   = Character.MIN_SUPPLEMENTARY_CODE_POINT; //0X010000;
  static final int minPlane2CP  = 0X020000;
  static final int maxCodePoint = Character.MAX_CODE_POINT;               //0X10FFFF
  static final int minSurr      = Character.MIN_SURROGATE;                //0X00D800;
  static final int maxSurr      = Character.MAX_SURROGATE;                //0X00E000;

  @Test
  //randomly selects CP from a range that include 1, 2, 3 and 4 byte encodings.
  // with 50% coming from plane 0 and 50% coming from plane 1.
  public void checkRandomValidCodePoints() {
    RandomCodePoints rcp = new RandomCodePoints(true);
    int numCP = 1000;
    int[] cpArr = new int[numCP];
    rcp.fillCodePointArray(cpArr, 0, minPlane2CP);
    String rcpStr = new String(cpArr, 0, numCP);
    //println(rcpStr);
    WritableMemory wmem = DefaultMemoryFactory.DEFAULT.allocate(4 * numCP);
    int utf8Bytes = (int) wmem.putCharsToUtf8(0, rcpStr);

    StringBuilder sb = new StringBuilder();
    try {
      wmem.getCharsFromUtf8(0L, utf8Bytes, (Appendable) sb);
    } catch (IOException | Utf8CodingException e) {
      throw new RuntimeException(e);
    }
    checkStrings(sb.toString(), rcpStr);

    CharBuffer cb = CharBuffer.allocate(rcpStr.length());
    try {
      wmem.getCharsFromUtf8(0L, utf8Bytes, cb);
    } catch (IOException | Utf8CodingException e) {
      throw new RuntimeException(e);
    }
    String cbStr = sb.toString();
    assertEquals(cbStr.length(), rcpStr.length());
    checkStrings(cbStr, rcpStr);
  }

  @Test
  public void checkRandomValidCodePoints2() {
    //checks the non-deterministic constructor
    @SuppressWarnings("unused")
    RandomCodePoints rcp = new RandomCodePoints(false);
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
  public void checkNonEmptyDestinationForDecode() {
    StringBuilder sb = new StringBuilder();
    sb.append("abc"); //current contents of destination
    int startChars = sb.toString().toCharArray().length;
    String refStr = "Quizdeltagerne spiste jordb\u00e6r med fl\u00f8de, mens cirkusklovnen";
    byte[] refByteArr = refStr.getBytes(UTF_8);
    int addBytes = refByteArr.length;
    WritableMemory refMem = DefaultMemoryFactory.DEFAULT.writableWrap(refByteArr);
    int decodedChars = refMem.getCharsFromUtf8(0, addBytes, sb);
    String finalStr = sb.toString();
    int finalChars = finalStr.toCharArray().length;
    assertEquals(decodedChars + startChars, finalChars);
    println("Decoded chars: " + decodedChars);
    println("Final chars: " + finalChars);
    println(sb.toString());
  }

  @Test
  public void checkNonEmptyDestinationForEncode() {
    String refStr = "Quizdeltagerne spiste jordb\u00e6r med fl\u00f8de, mens cirkusklovnen";
    byte[] refByteArr = refStr.getBytes(UTF_8);
    int refBytes = refByteArr.length;
    int offset = 100;
    WritableMemory tgtMem = DefaultMemoryFactory.DEFAULT.allocate(refBytes + offset);
    long bytesEncoded = tgtMem.putCharsToUtf8(offset, refStr);
    assertEquals(bytesEncoded, refBytes);
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
    assertRoundTrips(str, 4, 10, 4);
    assertRoundTrips(str, 0, str.length(), 0);
  }

  @Test
  public void testInvalidBufferSlice() { //these are pure Memory bounds violations
    byte[] bytes  = "The quick brown fox jumps over the lazy dog".getBytes(UTF_8);
    assertInvalidSlice(bytes, bytes.length - 3, 4);
    assertInvalidSlice(bytes, bytes.length, 1);
    assertInvalidSlice(bytes, bytes.length + 1, 0);
    assertInvalidSlice(bytes, 0, bytes.length + 1);
  }

  private static void assertInvalid(int... bytesAsInt) { //invalid byte sequences
    byte[] bytes = new byte[bytesAsInt.length];
    for (int i = 0; i < bytesAsInt.length; i++) {
      bytes[i] = (byte) bytesAsInt[i];
    }
    assertInvalid(bytes);
  }

  private static void assertInvalid(byte[] bytes) {
    int bytesLen = bytes.length;
    try {
        DefaultMemoryFactory.DEFAULT.wrap(bytes).getCharsFromUtf8(0, bytesLen, new StringBuilder());
      fail();
    } catch (Utf8CodingException e) {
      // Expected.
    }
    try {
      CharBuffer cb = CharBuffer.allocate(bytesLen);
      DefaultMemoryFactory.DEFAULT.wrap(bytes).getCharsFromUtf8(0, bytesLen, cb);
      fail();
    } catch (Utf8CodingException | IOException e) {
      // Expected.
    }
  }

  private static void assertInvalidSlice(byte[] bytes, int index, int size) {
    try {
      Memory mem = DefaultMemoryFactory.DEFAULT.wrap(bytes);
      mem.getCharsFromUtf8(index, size, new StringBuilder());
      fail();
    } catch (IllegalArgumentException e) { //Pure bounds violation
      // Expected.
    }
  }

  /**
   * Performs round-trip test using the given reference string
   * @param refStr the reference string
   * @throws IOException
   */
  private static void assertRoundTrips(String refStr) throws IOException {
    assertRoundTrips(refStr, refStr.toCharArray().length, 0, -1);
  }

  /**
   * Performs round-trip test using the given reference string
   * @param refStr the reference string
   * @param refSubCharLen the number of characters expected to be decoded
   * @param offsetBytes starting utf8 byte offset
   * @param utf8LengthBytes length of utf8 bytes
   * @throws IOException
   */
  private static void assertRoundTrips(String refStr, int refSubCharLen, int offsetBytes,
      int utf8LengthBytes) throws IOException {
    byte[] refByteArr = refStr.getBytes(UTF_8);
    if (utf8LengthBytes == -1) {
      utf8LengthBytes = refByteArr.length;
    }
    Memory refMem = DefaultMemoryFactory.DEFAULT.wrap(refByteArr);

    byte[] refByteArr2 = new byte[refByteArr.length + 1];
    System.arraycopy(refByteArr, 0, refByteArr2, 1, refByteArr.length);
    Memory refReg = DefaultMemoryFactory.DEFAULT.wrap(refByteArr2).region(1, refByteArr.length);

    WritableMemory dstMem = DefaultMemoryFactory.DEFAULT.allocate(refByteArr.length);
    WritableMemory dstMem2 =
            DefaultMemoryFactory.DEFAULT.allocate(refByteArr.length + 1).writableRegion(1, refByteArr.length);

    // Test with Memory objects, where base offset != 0
    assertRoundTrips(refStr, refSubCharLen, offsetBytes, utf8LengthBytes, refByteArr, refMem, dstMem);
    assertRoundTrips(refStr, refSubCharLen, offsetBytes, utf8LengthBytes, refByteArr, refMem, dstMem2);
    assertRoundTrips(refStr, refSubCharLen, offsetBytes, utf8LengthBytes, refByteArr, refReg, dstMem);
    assertRoundTrips(refStr, refSubCharLen, offsetBytes, utf8LengthBytes, refByteArr, refReg, dstMem2);
  }

  private static void assertRoundTrips(String refStr, int refSubCharLen, int offsetBytes,
      int utf8LengthBytes, byte[] refByteArr, Memory refMem, WritableMemory dstMem)
          throws IOException {
    StringBuilder sb = new StringBuilder();

    int charPos = refMem.getCharsFromUtf8(offsetBytes, utf8LengthBytes, sb);
    checkStrings(sb.toString(), new String(refByteArr, offsetBytes, utf8LengthBytes, UTF_8));
    assertEquals(charPos, refSubCharLen);

    CharBuffer cb = CharBuffer.allocate(refByteArr.length + 1);
    cb.position(1);
    // Make CharBuffer 1-based, to check correct offset handling
    cb = cb.slice();
    refMem.getCharsFromUtf8(offsetBytes, utf8LengthBytes, cb);
    cb.flip();
    checkStrings(cb.toString(), new String(refByteArr, offsetBytes, utf8LengthBytes, UTF_8));

    long encodedUtf8Bytes = dstMem.putCharsToUtf8(0, refStr); //encodes entire refStr
    assertEquals(encodedUtf8Bytes, refByteArr.length); //compares bytes length
    //compare the actual bytes encoded
    assertEquals(0, dstMem.compareTo(0, refByteArr.length, refMem, 0, refByteArr.length));

    // Test write overflow
    WritableMemory writeMem2 = DefaultMemoryFactory.DEFAULT.allocate(refByteArr.length - 1);
    try {
      writeMem2.putCharsToUtf8(0, refStr);
      fail();
    } catch (Utf8CodingException e) {
      // Expected.
    }
  }

  private static boolean isSurrogateCodePoint(final int cp) {
    return (cp >= Character.MIN_SURROGATE) && (cp <= Character.MAX_SURROGATE);
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
}
