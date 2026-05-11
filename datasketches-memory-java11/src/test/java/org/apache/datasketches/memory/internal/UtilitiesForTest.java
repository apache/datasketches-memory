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

public class UtilitiesForTest {

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
