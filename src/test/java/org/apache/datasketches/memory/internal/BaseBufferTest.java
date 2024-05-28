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

import static org.testng.Assert.fail;

import org.apache.datasketches.memory.BaseState;
import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

/**
 * @author Lee Rhodes
 */
public class BaseBufferTest {
  private static final MemoryRequestServer memReqSvr = BaseState.defaultMemReqSvr;

  @Test
  public void checkLimits() {
    Buffer buf = Memory.wrap(new byte[100]).asBuffer();
    buf.setStartPositionEnd(40, 45, 50);
    buf.setStartPositionEnd(0, 0, 100);
    try {
      buf.setStartPositionEnd(0, 0, 101);
      fail();
    } catch (AssertionError e) {
      //ok
    }
  }

  @Test
  public void checkLimitsAndCheck() {
    Buffer buf = Memory.wrap(new byte[100]).asBuffer();
    buf.setAndCheckStartPositionEnd(40, 45, 50);
    buf.setAndCheckStartPositionEnd(0, 0, 100);
    try {
      buf.setAndCheckStartPositionEnd(0, 0, 101);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
    buf.setAndCheckPosition(100);
    try {
      buf.setAndCheckPosition(101);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
    buf.setPosition(99);
    buf.incrementAndCheckPosition(1L);
    try {
      buf.incrementAndCheckPosition(1L);
      fail();
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void checkCheckNotAliveAfterTWR() {
    WritableMemory wmem;
    Buffer buf;
    try (ResourceScope scope = ResourceScope.newConfinedScope()) {
      wmem = WritableMemory.allocateDirect(100, scope, memReqSvr);
      buf = wmem.asBuffer();
    }
    try {
      buf.asMemory(); //not alive
    } catch (IllegalStateException e) { }
  }
}
