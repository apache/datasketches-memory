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

package org.apache.datasketches.memory.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Util;
import org.apache.datasketches.memory.WritableDirectHandle;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class AllocateDirectMemoryTest {
  
  @Test
  public void simpleAllocateDirect() {
    int longs = 32;
    WritableMemory wMem;
    try (WritableHandle wh = WritableMemory.allocateDirect(longs << 3)) {
      wMem = wh.get();
      for (int i = 0; i<longs; i++) {
        wMem.putLong(i << 3, i);
        assertEquals(wMem.getLong(i << 3), i);
      }
    }
    //The TWR block has exited, so the memory should be invalid
    try {
      final Class<?> wMemClass = wMem.getClass();
      Method method = ReflectUtil.getMethod(wMemClass, "checkValid", (Class<?>[])null);
      method.invoke(wMem, (Object[]) null);
      //wMem.checkValid();
      fail();
    } catch (final Exception e) {
      if (e instanceof IllegalStateException || e instanceof InvocationTargetException) { } //OK
      else { throw new RuntimeException(e); }
    }
  }

  @Test
  public void checkDefaultMemoryRequestServer() {
    int longs1 = 32;
    int bytes1 = longs1 << 3;
    try (WritableHandle wh = WritableMemory.allocateDirect(bytes1)) {
      WritableMemory origWmem = wh.get();
      for (int i = 0; i<longs1; i++) { //puts data in wMem1
        origWmem.putLong(i << 3, i);
        assertEquals(origWmem.getLong(i << 3), i);
      }
      println(origWmem.toHexString("Test", 0, 32 * 8));

      int longs2 = 64;
      int bytes2 = longs2 << 3;
      MemoryRequestServer memReqSvr = origWmem.getMemoryRequestServer();
      WritableMemory newWmem = memReqSvr.request(bytes2);
      assertFalse(newWmem.isDirect()); //on heap by default
      for (int i = 0; i < longs2; i++) {
          newWmem.putLong(i << 3, i);
          assertEquals(newWmem.getLong(i << 3), i);
      }
      memReqSvr.requestClose(origWmem, newWmem);
      //The default MRS doesn't actually release because it could be misused.
      // so we let the TWR release it.
    }
  }

  @Test
  public void checkNullMemoryRequestServer() {
    try (WritableHandle wh = WritableMemory.allocateDirect(128, null)) {
      WritableMemory wmem = wh.get();
      assertNotNull(wmem.getMemoryRequestServer());
    }
  }


  @Test
  public void checkNonNativeDirect() { //not allowed in public API
    Class<?> bwMemImpl = ReflectUtil.getClass("org.apache.datasketches.memory.BaseWritableMemoryImpl");
    Method wrapDirectMethod = 
        ReflectUtil.getMethod(bwMemImpl, "wrapDirect", long.class, ByteOrder.class, MemoryRequestServer.class);
    try (WritableDirectHandle h = (WritableDirectHandle) 
        wrapDirectMethod.invoke(wrapDirectMethod, 8,  Util.nonNativeByteOrder, null)) {
        //BaseWritableMemoryImpl.wrapDirect(8, Util.nonNativeByteOrder, null)) {
      WritableMemory wmem = h.get();
      wmem.putChar(0, (char) 1);
      assertEquals(wmem.getByte(1), (byte) 1);
    } catch (final Exception e) {
      if (e instanceof IllegalStateException || e instanceof InvocationTargetException) { } //OK
      else { throw new RuntimeException(e); }
    }
  }

  @Test
  public void checkExplicitClose() {
    final long cap = 128;
    try (WritableDirectHandle wdh = WritableMemory.allocateDirect(cap)) {
      wdh.close(); //explicit close. Does the work of closing
    } //end of scope call to Cleaner/Deallocator also will be redundant
  }

  @AfterClass
  public void checkDirectCounter() {
    final Class<?> baseStateClass = ReflectUtil.getClass("org.apache.datasketches.memory.BaseState");
    final Method currDirMemAllocMethod = 
        ReflectUtil.getMethod(baseStateClass, "getCurrentDirectMemoryAllocations", (Class<?>[])null);
    long count;
    try {
      count = (long) currDirMemAllocMethod.invoke(currDirMemAllocMethod, (Object[])null);
      //final long count = BaseState.getCurrentDirectMemoryAllocations();
      if (count != 0) {
        println(""+count);
        fail();
      }
    } catch (final Exception e) {
      if (e instanceof IllegalStateException || e instanceof InvocationTargetException) { } //OK
      else { throw new RuntimeException(e); }
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    //System.out.println(s); //disable here
  }
}
