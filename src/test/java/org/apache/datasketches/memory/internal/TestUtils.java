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

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.internal.unsafe.UnsafeUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;

/**
 * Utility functions used in unit tests
 */
public class TestUtils {
    public static final String LS = System.lineSeparator();

    /**
     * 
     * Return true if all the masked bits of value are zero
     * 
     * @param value the value to be tested
     * @param bitMask defines the bits of interest
     * @return true if all the masked bits of value are zero
     */
    public static final boolean isAllBitsClear(final long value, final long bitMask) {
        return (~value & bitMask) == bitMask;
    }

    /**
     * Return true if all the masked bits of value are one
     * 
     * @param value the value to be tested
     * @param bitMask defines the bits of interest
     * @return true if all the masked bits of value are one
     */
    public static final boolean isAllBitsSet(final long value, final long bitMask) {
        return (value & bitMask) == bitMask;
    }

    /**
     * Return true if any the masked bits of value are zero
     * 
     * @param value the value to be tested
     * @param bitMask defines the bits of interest
     * @return true if any the masked bits of value are zero
     */
    public static final boolean isAnyBitsClear(final long value, final long bitMask) {
        return (~value & bitMask) != 0;
    }

    /**
     * Return true if any the masked bits of value are one
     * 
     * @param value the value to be tested
     * @param bitMask defines the bits of interest
     * @return true if any the masked bits of value are one
     */
    public static final boolean isAnyBitsSet(final long value, final long bitMask) {
        return (value & bitMask) != 0;
    }

    // Resources NOTE: these 3 methods are duplicated in Java/ datasketches/Util

    /**
     * Gets the absolute path of the given resource file's shortName.
     *
     * <p>
     * Note that the ClassLoader.getResource(shortName) returns a URL,
     * which can have special characters, e.g., "%20" for spaces. This method
     * obtains the URL, converts it to a URI, then does a uri.getPath(), which
     * decodes any special characters in the URI path. This is required to make
     * obtaining resources operating-system independent.
     * </p>
     *
     * @param shortFileName the last name in the pathname's name sequence.
     * @return the absolute path of the given resource file's shortName.
     */
    public static String getResourcePath(final String shortFileName) {
        Objects.requireNonNull(shortFileName, "input parameter " + shortFileName + " cannot be null.");
        try {
            final URL url = Util.class.getClassLoader().getResource(shortFileName);
            Objects.requireNonNull(url, "resource " + shortFileName + " could not be acquired.");
            final URI uri = url.toURI();
            // decodes any special characters
            final String path = uri.isAbsolute() ? Paths.get(uri).toAbsolutePath().toString() : uri.getPath();
            return path;
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Cannot find resource: " + shortFileName + LS + e);
        }
    }

    /**
     * Gets the file defined by the given resource file's shortFileName.
     * 
     * @param shortFileName the last name in the pathname's name sequence.
     * @return the file defined by the given resource file's shortFileName.
     */
    public static File getResourceFile(final String shortFileName) {
        return new File(getResourcePath(shortFileName));
    }

    /**
     * Returns a byte array of the contents of the file defined by the given resource file's
     * shortFileName.
     * 
     * @param shortFileName the last name in the pathname's name sequence.
     * @return a byte array of the contents of the file defined by the given resource file's
     *         shortFileName.
     */
    public static byte[] getResourceBytes(final String shortFileName) {
        try {
            return Files.readAllBytes(Paths.get(getResourcePath(shortFileName)));
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot read resource: " + shortFileName + LS + e);
        }
    }

    public static final String getFileAttributes(File file) {
        try {
            PosixFileAttributes attrs =
                    Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class, new LinkOption[0])
                            .readAttributes();
            String s = String.format("%s: %s %s %s%n", file.getPath(), attrs.owner().getName(), attrs.group().getName(),
                    PosixFilePermissions.toString(attrs.permissions()));
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Searches a range of the specified array of longs for the specified value using the binary
     * search algorithm. The range must be sorted method) prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains
     * multiple elements with the specified value, there is no guarantee which one will be found.
     * @param mem the Memory to be searched
     * @param fromLongIndex the index of the first element (inclusive) to be searched
     * @param toLongIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array within the specified range;
     * otherwise, (-(insertion point) - 1). The insertion point is defined as the point at which
     * the key would be inserted into the array: the index of the first element in the range greater
     * than the key, or toIndex if all elements in the range are less than the specified key.
     * Note that this guarantees that the return value will be &ge; 0 if and only if the key is found.
     */
    public static long binarySearchLongs(final Memory mem, final long fromLongIndex,
        final long toLongIndex, final long key) {
      UnsafeUtil.checkBounds(fromLongIndex << 3, (toLongIndex - fromLongIndex) << 3, mem.getCapacity());
      long low = fromLongIndex;
      long high = toLongIndex - 1L;

      while (low <= high) {
        final long mid = (low + high) >>> 1;
        final long midVal = mem.getLong(mid << 3);

        if (midVal < key)      { low = mid + 1;  }
        else if (midVal > key) { high = mid - 1; }
        else                   { return mid;     } // key found
      }
      return -(low + 1); // key not found.
    }

    public static final void nullCheck(final Object obj, final String arg) {
        if (obj == null) {
            throw new IllegalArgumentException("The argument '" + arg + "' may not be null.");
        }
    }

    public static final void setGettysburgAddressFileToReadOnly() {
        File file = getResourceFile("GettysburgAddress.txt");
        file.setWritable(false);
    }


    /**
     * Prepend the given string with zeros. If the given string is equal or greater than the given
     * field length, it will be returned without modification.
     * 
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
     * 
     * @param s the given string
     * @param fieldLength the desired field length
     * @param padChar the desired pad character
     * @param postpend if true append the pacCharacters to the end of the string.
     * @return prepended or postpended given string with the given character to fill the given field
     *         length.
     */
    public static final String characterPad(final String s, final int fieldLength, final char padChar,
            final boolean postpend) {
        final char[] chArr = s.toCharArray();
        final int sLen = chArr.length;
        if (sLen < fieldLength) {
            final char[] out = new char[fieldLength];
            final int blanks = fieldLength - sLen;

            if (postpend) {
                for (int i = 0; i < sLen; i++) {
                    out[i] = chArr[i];
                }
                for (int i = sLen; i < fieldLength; i++) {
                    out[i] = padChar;
                }
            } else { // prepend
                for (int i = 0; i < blanks; i++) {
                    out[i] = padChar;
                }
                for (int i = blanks; i < fieldLength; i++) {
                    out[i] = chArr[i - blanks];
                }
            }

            return String.valueOf(out);
        }
        return s;
    }
}
