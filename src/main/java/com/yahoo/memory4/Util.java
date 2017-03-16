/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static com.yahoo.memory4.UnsafeUtil.assertBounds;

final class Util {

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
    assertBounds(fromLongIndex << 3, (toLongIndex - fromLongIndex) << 3, mem.getCapacity());
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

  /**
   * Exception handler for requesting a new Memory allocation of the given newCapacityBytes,
   * using the MemoryRequest callback interface.
   * If <i>copy</i> is true, the <i>origMem</i> will be copied into the new Memory.
   *
   * @param origMem The original Memory that needs to be replaced by a newly allocated Memory.
   * @param newCapacityBytes The required capacity of the new Memory.
   * @param copy if true, data from the origMem will be copied to the new Memory as space allows
   * and the origMemory will be requested to be freed.
   * If false, no copy will occur and the request to free the origMem will not occur.
   * @return the newly requested Memory
   */
  public static WritableMemory memoryRequestHandler(final WritableMemory origMem,
      final long newCapacityBytes, final boolean copy) {
    final MemoryRequest memReq = origMem.getMemoryRequest();
    if (memReq == null) {
      throw new IllegalArgumentException(
          "Insufficient space. MemoryRequest callback cannot be null.");
    }
    final WritableMemory newDstMem = (copy)
        ? memReq.request(origMem, origMem.getCapacity(), newCapacityBytes)
        : memReq.request(newCapacityBytes);

    if (newDstMem == null) {
      throw new IllegalArgumentException(
          "Insufficient space and Memory returned by MemoryRequest cannot be null.");
    }
    final long newCap = newDstMem.getCapacity(); //may be more than requested, but not less.
    if (newCap < newCapacityBytes) {
      memReq.closeRequest(newDstMem);
      throw new IllegalArgumentException(
          "Insufficient space. Memory returned by MemoryRequest is not the requested capacity: "
          + "Returned: " + newCap + " < Requested: " + newCapacityBytes);
    }

    if (copy) { //copy and request free.
      origMem.copyTo(0, newDstMem, 0, Math.min(origMem.getCapacity(), newCap));
      memReq.closeRequest(origMem, newDstMem);
    }
    return newDstMem;
  }

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
   * @return prepended or postpended given string with the given character to fill the given field
   * length.
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
      } else { //prepend
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
