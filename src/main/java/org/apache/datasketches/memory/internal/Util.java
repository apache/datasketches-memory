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

import static java.util.Arrays.fill;
import static org.apache.datasketches.memory.internal.ResourceImpl.LS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Lee Rhodes
 */
//@SuppressWarnings("javadoc")
final class Util {

  private Util() { }

  /**
   * Prepend the given string with zeros. If the given string is equal or greater than the given
   * field length, it will be returned without modification.
   * @param s the given string
   * @param fieldLength desired total field length including the given string
   * @return the given string prepended with zeros.
   */
  public static final String zeroPad(final String s, final int fieldLength) {
    return characterPad(s, fieldLength, '0', false);
  }

  /**
   * Prepend or postpend the given string with the given character to fill the given field length.
   * If the given string is equal or greater than the given field length, it will be returned
   * without modification.
   * @param s the given string
   * @param fieldLength the desired field length
   * @param padChar the desired pad character
   * @param postpend if true append the pacCharacters to the end of the string.
   * @return prepended or postpended given string with the given character to fill the given field length.
   */
  public static final String characterPad(final String s, final int fieldLength, final char padChar, final boolean postpend) {
    final int sLen = s.length();
    if (sLen < fieldLength) {
      final char[] cArr = new char[fieldLength - sLen];
      fill(cArr, padChar);
      final String addstr = String.valueOf(cArr);
      return (postpend) ? s.concat(addstr) : addstr.concat(s);
    }
    return s;
  }

  /**
   * Return true if all the masked bits of value are zero
   * @param value the value to be tested
   * @param bitMask defines the bits of interest
   * @return true if all the masked bits of value are zero
   */
  static final boolean isAllBitsClear(final long value, final long bitMask) {
    return (~value & bitMask) == bitMask;
  }

  /**
   * Return true if all the masked bits of value are one
   * @param value the value to be tested
   * @param bitMask defines the bits of interest
   * @return true if all the masked bits of value are one
   */
  static final boolean isAllBitsSet(final long value, final long bitMask) {
    return (value & bitMask) == bitMask;
  }

  /**
   * Return true if any the masked bits of value are zero
   * @param value the value to be tested
   * @param bitMask defines the bits of interest
   * @return true if any the masked bits of value are zero
   */
  static final boolean isAnyBitsClear(final long value, final long bitMask) {
    return (~value & bitMask) != 0;
  }

  /**
   * Return true if any the masked bits of value are one
   * @param value the value to be tested
   * @param bitMask defines the bits of interest
   * @return true if any the masked bits of value are one
   */
  static final boolean isAnyBitsSet(final long value, final long bitMask) {
    return (value & bitMask) != 0;
  }

  //Resources mention: these 3 methods are duplicated in Java/ datasketches/Util

  /**
   * Gets the absolute path of the given resource file's shortName.
   *
   * <p>Note that the ClassLoader.getResource(shortName) returns a URL,
   * which can have special characters, e.g., "%20" for spaces. This method
   * obtains the URL, converts it to a URI, then does a uri.getPath(), which
   * decodes any special characters in the URI path. This is required to make
   * obtaining resources operating-system independent.</p>
   *
   * @param shortFileName the last name in the pathname's name sequence.
   * @return the absolute path of the given resource file's shortName.
   * @throws IllegalArgumentException if resource cannot be found
   */
  private static String getResourcePath(final String shortFileName) {
    Objects.requireNonNull(shortFileName, "input parameter " + shortFileName + " cannot be null.");
    try {
      final URL url = Util.class.getClassLoader().getResource(shortFileName);
      Objects.requireNonNull(url, "resource " + shortFileName + " could not be acquired.");
      final URI uri = url.toURI();
      //decodes any special characters
      final String path = uri.isAbsolute() ? Paths.get(uri).toAbsolutePath().toString() : uri.getPath();
      return path;
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException("Cannot find resource: " + shortFileName + LS + e);
    }
  }

  /**
   * Gets the file defined by the given resource file's shortFileName.
   * @param shortFileName the last name in the pathname's name sequence.
   * @return the file defined by the given resource file's shortFileName.
   */
  static File getResourceFile(final String shortFileName) {
    return new File(getResourcePath(shortFileName));
  }

  /**
   * Returns a byte array of the contents of the file defined by the given resource file's shortFileName.
   * @param shortFileName the last name in the pathname's name sequence.
   * @return a byte array of the contents of the file defined by the given resource file's shortFileName.
   * @throws IllegalArgumentException if resource cannot be read.
   */
  static byte[] getResourceBytes(final String shortFileName) {
    File file = getResourceFile(shortFileName);
    byte[] bytes = new byte[(int) file.length()];
    try (FileInputStream fis = new FileInputStream(file)) {
      fis.read(bytes);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot read resource: " + shortFileName + LS + e);
    }
    return bytes;
  }

}
