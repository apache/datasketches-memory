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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Random;

import org.apache.datasketches.memory.Memory;

public class TestUtil {

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
    ResourceImpl.checkBounds(fromLongIndex << 3, (toLongIndex - fromLongIndex) << 3, mem.getCapacity());
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
   * Creates random valid Character Code Points (as integers). By definition, valid CodePoints
   * are integers in the range 0 to Character.MAX_CODE_POINT, and exclude the surrogate values.
   * This is used in unit testing and characterization testing of the UTF8 class. Because the
   * characterization tools are in a separate package, this must remain public.
   *
   * @author Lee Rhodes
   */
  public static class RandomCodePoints {
    private Random rand; //
    private static final int ALL_CP = Character.MAX_CODE_POINT + 1;
    private static final int MIN_SUR = Character.MIN_SURROGATE;
    private static final int MAX_SUR = Character.MAX_SURROGATE;
  
    /**
     * @param deterministic if true, configure java.util.Random with a fixed seed.
     */
    public RandomCodePoints(final boolean deterministic) {
      rand = deterministic ? new Random(0) : new Random();
    }
  
    /**
     * Fills the given array with random valid Code Points from 0, inclusive, to
     * <i>Character.MAX_CODE_POINT</i>, inclusive.
     * The surrogate range, which is from <i>Character.MIN_SURROGATE</i>, inclusive, to
     * <i>Character.MAX_SURROGATE</i>, inclusive, is always <u>excluded</u>.
     * @param cpArr the array to fill
     */
    public final void fillCodePointArray(final int[] cpArr) {
      fillCodePointArray(cpArr, 0, ALL_CP);
    }
  
    /**
     * Fills the given array with random valid Code Points from <i>startCP</i>, inclusive, to
     * <i>endCP</i>, exclusive.
     * The surrogate range, which is from <i>Character.MIN_SURROGATE</i>, inclusive, to
     * <i>Character.MAX_SURROGATE</i>, inclusive, is always <u>excluded</u>.
     * @param cpArr the array to fill
     * @param startCP the starting Code Point, included.
     * @param endCP the ending Code Point, excluded. This value cannot exceed 0x110000.
     */
    public final void fillCodePointArray(final int[] cpArr, final int startCP, final int endCP) {
      final int arrLen = cpArr.length;
      final int numCP = Math.min(endCP, 0X110000) - Math.min(0, startCP);
      int idx = 0;
      while (idx < arrLen) {
        final int cp = startCP + rand.nextInt(numCP);
        if ((cp >= MIN_SUR) && (cp <= MAX_SUR)) {
          continue;
        }
        cpArr[idx++] = cp;
      }
    }
  
    /**
     * Return a single valid random Code Point from 0, inclusive, to
     * <i>Character.MAX_CODE_POINT</i>, inclusive.
     * The surrogate range, which is from <i>Character.MIN_SURROGATE</i>, inclusive, to
     * <i>Character.MAX_SURROGATE</i>, inclusive, is always <u>excluded</u>.
     * @return a single valid random CodePoint.
     */
    public final int getCodePoint() {
      return getCodePoint(0, ALL_CP);
    }
  
    /**
     * Return a single valid random Code Point from <i>startCP</i>, inclusive, to
     * <i>endCP</i>, exclusive.
     * The surrogate range, which is from <i>Character.MIN_SURROGATE</i>, inclusive, to
     * <i>Character.MAX_SURROGATE</i>, inclusive, is always <u>excluded</u>.
     * @param startCP the starting Code Point, included.
     * @param endCP the ending Code Point, excluded. This value cannot exceed 0x110000.
     * @return a single valid random CodePoint.
     */
    public final int getCodePoint(final int startCP, final int endCP) {
      final int numCP = Math.min(endCP, 0X110000) - Math.min(0, startCP);
      while (true) {
        final int cp = startCP + rand.nextInt(numCP);
        if ((cp < MIN_SUR) || (cp > MAX_SUR)) {
          return cp;
        }
      }
    }
  } //End class RandomCodePoints

  /**
   * Windows, POSIX, and JAR friendly, returns a byte array of the contents of the file defined by the given 
   * resourceName. This is only used in test.
   * If the resource is in a JAR it will be copied into the File System as a temporary file first.
   * 
   * @param resourceName the short name or the full path name.
   * @return a byte array of the contents of the file defined by the given resourceName.
   */
  public static byte[] getResourceBytes(final String resourceName) {
    Objects.requireNonNull(resourceName, "Given resourceName must not be null");
    
    String normalizedName = resourceName.replace('\\', '/');
    if (normalizedName.startsWith("/")) {
      normalizedName = normalizedName.substring(1);
    }
  
    final ClassLoader loader = Util.class.getClassLoader();
    try (InputStream in = loader.getResourceAsStream(normalizedName)) {
      if (in == null) {
        throw new IllegalArgumentException("Resource not found: " + normalizedName);
      }
      return in.readAllBytes();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot read resource: " + normalizedName + Util.LS + e);
    }
  }

  /**
   *   Windows, POSIX, and JAR friendly get Resource File. This is only used in test.
   *   If the resource is in a JAR it will be copied into the File System as a temporary file first.
   *   @param resourceName the simple file name or full path name. 
   *   Any back-slashes will be converted to forward slashes and a leading forward slash will be removed.
   *   No other special characters allowed.
   *   @return a File System File
   */
  public static File getResourceFile(final String resourceName) {
    Objects.requireNonNull(resourceName, "Given resourceName must not be null");
    if (resourceName.isEmpty()) { throw new IllegalArgumentException("Given resourceName must not be empty"); }
    // Normalize name: ClassLoaders MUST use forward slashes even on Windows
    String normalizedName = resourceName.replace('\\', '/');
    if (normalizedName.startsWith("/")) { normalizedName = normalizedName.substring(1); }
  
    final ClassLoader loader = Util.class.getClassLoader();
    final URL url = loader.getResource(normalizedName);
    if (url == null) { throw new IllegalArgumentException("Resource not found: " + normalizedName); }
  
    // If it's a real file, return it directly
    if ("file".equals(url.getProtocol())) {
        try { 
          final URI uri = url.toURI();
          return new File(uri); } 
        catch (final URISyntaxException e) { return new File(url.getPath()); }
    }
  
    // If it's in a JAR, we must extract it for Memory.map() to work
    // We use a prefix that won't collide with Windows reserved names
    final File tempFile;
    try { tempFile = File.createTempFile("datasketches-", ".bin"); }
    catch (final IOException e1) { throw new IllegalArgumentException(e1); }
    tempFile.deleteOnExit();
  
    try (InputStream in = loader.getResourceAsStream(normalizedName)) {
        if (in == null) { throw new IllegalArgumentException("Could not open stream for " + normalizedName); }
        
        // Use REPLACE_EXISTING to avoid "File Already Exists" errors on Windows retries
        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (final IOException e) { throw new IllegalArgumentException(e); }
  
    // Final Windows Fix: Ensure the file is actually writable if you need to setReadOnly later
    //tempFile.setWritable(true); 
    
    return tempFile;
  }

  /**
   * Windows, POSIX, and JAR friendly, checks if the given resourceName exists and sets it to Read-Only.  
   * This is only used in test.
   * If the resource is in a JAR it will be copied into the File System as a temporary file first.
   * This will not work if the file is currently memory-mapped.  
   * If it is memory-mapped, close the mapping first.
   * @param resourceName the given resource.
   * @return the read only file.
   */
  public static File setResourceReadOnly(final String resourceName) {
      final File file = getResourceFile(resourceName);
      file.setReadOnly();
      return file;
  }
}
