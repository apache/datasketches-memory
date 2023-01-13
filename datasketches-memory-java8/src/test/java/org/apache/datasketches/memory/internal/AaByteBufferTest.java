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

import java.nio.ByteBuffer;

import org.apache.datasketches.memory.Memory;
import org.testng.annotations.Test;

public class AaByteBufferTest {

  @Test
  public void checkBB() {
    byte[] byteArr = new byte[32];
    int len = byteArr.length;
    for (byte i = 0; i < len; i++) { byteArr[i] = i; }
    ByteBuffer bb = ByteBuffer.wrap(byteArr);
    Memory mem = Memory.wrap(bb);
    for (int i = 0; i < len; i++) {
      //System.out.println(mem.getByte(i));
    }
  }

  @Test
  public void checkHeap() {
    byte[] byteArr = new byte[32];
    int len = byteArr.length;
    for (byte i = 0; i < len; i++) { byteArr[i] = i; }
    Memory mem = Memory.wrap(byteArr);
    for (int i = 0; i < len; i++) {
      //System.out.println(mem.getByte(i));
    }
  }

}

