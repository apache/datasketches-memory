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

import java.nio.ByteOrder;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

import jdk.incubator.foreign.ResourceScope;

/**
 * Examples of how to use the MemoryRequestServer with a memory hungry client.
 * @author Lee Rhodes
 */
public class ExampleMemoryRequestServerTest {

  /**
   * This version is without a TWR block. All of the memory allocations are done through the MemoryRequestServer
   * and each is closed by the MemoryClient when it is done with each.
   * @throws Exception
   */
  @SuppressWarnings("resource")
  @Test
  public void checkExampleMemoryRequestServer1() throws Exception {
    int bytes = 8;
    ExampleMemoryRequestServer svr = new ExampleMemoryRequestServer(true);
    WritableMemory memStart = null;
    ResourceScope scope = ResourceScope.newConfinedScope();
    memStart = WritableMemory.allocateDirect(bytes, 8, scope, ByteOrder.nativeOrder(), svr);
    MemoryClient client = new MemoryClient(memStart);
    client.process();
  }

  /**
   * This little client is never happy with how much memory it has been allocated and keeps
   * requesting for more. When it does ask for more, it must copy its old data into the new
   * memory, release the prior memory, and then continue working from there.
   *
   * <p>In reality, these memory requests should be quite rare.</p>
   */
  static class MemoryClient {
    WritableMemory smallMem;
    MemoryRequestServer svr;

    MemoryClient(WritableMemory memStart) {
      smallMem = memStart;
      svr = memStart.getMemoryRequestServer();
    }

    void process() {
      long cap1 = smallMem.getCapacity();
      smallMem.fill((byte) 1);                //fill it, but not big enough
      println(smallMem.toHexString("Small", 0, (int)cap1, true));

      WritableMemory bigMem = svr.request(smallMem, 2 * cap1); //get bigger mem
      long cap2 = bigMem.getCapacity();
      smallMem.copyTo(0, bigMem, 0, cap1);    //copy data from small to big
      svr.requestClose(smallMem, bigMem);     //done with smallMem, release it, if offheap

      bigMem.fill(cap1, cap1, (byte) 2);      //fill the rest of bigMem, still not big enough
      println(bigMem.toHexString("Big", 0, (int)cap2, true));

      WritableMemory giantMem = svr.request(bigMem, 2 * cap2); //get giant mem
      long cap3 = giantMem.getCapacity();
      bigMem.copyTo(0, giantMem, 0, cap2);    //copy data from small to big
      svr.requestClose(bigMem, giantMem);     //done with bigMem, release it, if offheap

      giantMem.fill(cap2, cap2, (byte) 3);    //fill the rest of giantMem
      println(giantMem.toHexString("Giant", 0, (int)cap3, true));
      svr.requestClose(giantMem, null);       //done with giantMem, release it
    }
  }

  /**
   * This example MemoryRequestServer is simplistic but demonstrates one of many ways to
   * possibly manage the continuous requests for larger memory.
   */
  public static class ExampleMemoryRequestServer implements MemoryRequestServer {
    final boolean offHeap;

    public ExampleMemoryRequestServer(final boolean offHeap) {
      this.offHeap = offHeap;
    }

    @SuppressWarnings("resource")
    @Override
    public WritableMemory request(WritableMemory currentWMem, long newCapacityBytes) {
     ByteOrder order = currentWMem.getByteOrder();
     WritableMemory wmem;
     if (offHeap) {
       wmem = WritableMemory.allocateDirect(newCapacityBytes, 8, ResourceScope.newConfinedScope(), order, this);
     } else {
       if (newCapacityBytes > Integer.MAX_VALUE) {
         throw new IllegalArgumentException("Requested capacity exceeds Integer.MAX_VALUE.");
       }
       wmem = WritableMemory.allocate((int)newCapacityBytes, order, this);
     }
     return wmem;
    }

    @Override
    //here we actually release it, in reality it might be a lot more complex.
    public void requestClose(WritableMemory memToRelease, WritableMemory newMemory) {
      memToRelease.close();
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param o value to print
   */
  static void println(Object o) {
    //System.out.println(o); //disable here
  }
}
