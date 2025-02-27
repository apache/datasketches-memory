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

/*
 * Note: Lincoln's Gettysburg Address is in the public domain. See LICENSE.
 */

package org.apache.datasketches.memory.internal;

import static java.nio.file.Files.readString;
import static org.apache.datasketches.memory.internal.Util.getResourceFile;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import org.testng.annotations.Test;

public class UtilTest {

  static final String getFileAttributes(File file) throws IOException {
    PosixFileAttributes attrs = Files.getFileAttributeView(
       file.toPath(), PosixFileAttributeView.class, new LinkOption[0]).readAttributes();
    String s = String.format("%s: %s %s %s%n",
       file.getPath(),
       attrs.owner().getName(),
       attrs.group().getName(),
       PosixFilePermissions.toString(attrs.permissions()));
    return s;
  }

  static final File setGettysburgAddressFileToReadOnly() {
    File file = getResourceFile("GettysburgAddress.txt");
    if (!file.setWritable(false)) { throw new IllegalStateException("File could not set Read-Only"); }
    return file;
  }

  //Resources

  @Test
  public void resourceFileExits() {
    final String shortFileName = "GettysburgAddress.txt";
    final File file = getResourceFile(shortFileName);
    assertTrue(file.exists());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void resourceFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    getResourceFile(shortFileName + "123");
  }

  static final String LS = System.getProperty("line.separator");

  @Test
  public void resourceStringLikelyCorrect() {
    final String shortFileName = "GettysburgAddress.txt";
    final File file = getResourceFile(shortFileName);
    String str;
    try {
      str = readString(file.toPath());
    }
    catch (IOException e) { throw new IllegalArgumentException(e); }
    assertTrue(str.startsWith("Abraham Lincoln's Gettysburg Address:"));
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  static void println(final Object o) {
    if (o == null) { print(LS); }
    else { print(o.toString() + LS); }
  }

  /**
   * @param o value to print
   */
  static void print(final Object o) {
    if (o != null) {
      //System.out.print(o.toString()); //disable here
    }
  }

}
