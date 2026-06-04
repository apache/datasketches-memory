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

import static org.apache.datasketches.memory.internal.Util.characterPad;
import static org.apache.datasketches.memory.internal.Util.negativeCheck;
import static org.apache.datasketches.memory.internal.Util.zeroPad;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.datasketches.memory.MemoryBoundsException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UtilTest {
  private static final String LS = System.getProperty("line.separator");
  private File gettyFile;
  private long gettySize;
  
  @BeforeClass
  public void setReadOnly() {
    gettyFile = UtilitiesForTest.setResourceReadOnly("GettysburgAddress.txt");
    gettySize = gettyFile.length();
  }

  @Test(expectedExceptions = MemoryBoundsException.class)
  public void checkBoundsTest() {
    ResourceImpl.checkBounds(999, 2, 1000);
  }

  @Test
  public void checkPadding() {
    String s = "123";
    String t = zeroPad(s, 4);
    assertTrue(t.startsWith("0"));

    t = characterPad(s, 4, '0', true);
    assertTrue(t.endsWith("0"));

    t = characterPad(s, 3, '0', false);
    assertEquals(s, t);
  }

  @Test
  public void checkNegativeChecks() {
    try {
      negativeCheck(-1L, "Test Long");
      fail();
    } catch (IllegalArgumentException e) {
      //OK
    }
  }

  static final String getFileAttributes(File file) {
    try {
    PosixFileAttributes attrs = Files.getFileAttributeView(
        file.toPath(), PosixFileAttributeView.class, new LinkOption[0]).readAttributes();
    String s = String.format("%s: %s %s %s%n",
        file.getPath(),
        attrs.owner().getName(),
        attrs.group().getName(),
        PosixFilePermissions.toString(attrs.permissions()));
    return s;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  //Resources

  @Test
  public void resourceFileExits() {
    assertTrue(gettyFile.exists());
  }

  @Test
  public void resourceFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    try { UtilitiesForTest.getResourceFile(shortFileName + "123"); }
    catch (IllegalArgumentException e) { //OK
    }
  }

  @Test
  public void resourceBytesCorrect() {
    assertTrue(gettySize == 1541);
  }

  @Test
  public void resourceBytesFileNotFound() {
    final String shortFileName = "GettysburgAddress.txt";
    try { UtilitiesForTest.getResourceBytes(shortFileName + "123"); }
    catch (IllegalArgumentException e) { //OK
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: " + this.getClass().getName());
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
