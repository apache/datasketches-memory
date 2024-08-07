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

import static org.apache.datasketches.memory.internal.Util.getResourceFile;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThreadTest {

  File file;
  Memory mem;
  WritableMemory wmem;
  Thread altThread;

  @BeforeClass
  public void prepareFileAndMemory() throws IOException {
    UtilTest.setGettysburgAddressFileToReadOnly();
    file = getResourceFile("GettysburgAddress.txt");
    assertTrue(AllocateDirectWritableMap.isFileReadOnly(file));
  }

  void initMap() throws IOException {
    mem = Memory.map(file); assertTrue(mem.isAlive());
  }

  void initDirectMem() {
    wmem = WritableMemory.allocateDirect(1024); assertTrue(wmem.isAlive());
  }

  Runnable tryMapClose = () -> {
    try { mem.close(); fail(); }
    catch (IllegalStateException expected) { }
  };

  Runnable tryDirectClose = () -> {
    try { wmem.close(); fail(); }
    catch (IllegalStateException expected) { }
  };

  @Test
  public void runTests() throws IOException {
    initMap();
    altThread = new Thread(tryMapClose, "altThread"); altThread.start();
    mem.close();
    initDirectMem();
    altThread = new Thread(tryDirectClose, "altThread"); altThread.start();
    wmem.close();
  }

}
