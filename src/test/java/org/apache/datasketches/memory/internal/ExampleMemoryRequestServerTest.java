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

import java.lang.foreign.Arena;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.DefaultMemoryRequestServer;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.Resource;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * Example of how to use the MemoryRequestServer with a memory hungry client.
 *
 * <p>Note: this example only works with Java 21 and above.</p>
 *
 * @author Lee Rhodes
 */
public class ExampleMemoryRequestServerTest {

  /**
   * This version is without a TWR block. All of the memory allocations are done through the MemoryRequestServer
   * and each is closed by the MemoryClient when it is done with them.
   * @throws Exception
   */
  @Test
  public void checkExampleMemoryRequestServer1() {

    long workingMemBytes = 8;

    Arena arena = Arena.ofConfined();
    //Configure the memReqSvr to create new subsequent allocations off-heap, each with a new Arena.
    MemoryRequestServer myMemReqSvr = new DefaultMemoryRequestServer(8, ByteOrder.nativeOrder(), false, true);

    //Create the initial working memory for the client
    WritableMemory workingMem = WritableMemory.allocateDirect(
      workingMemBytes,
      myMemReqSvr,
      arena);

    MemoryHungryClient client = new MemoryHungryClient(workingMem);
    client.process();
  }

  /**
   * This little client is never happy with how much memory it has been allocated and keeps
   * requesting for more. The client must request the MemoryRequestServer to release all the memory at the end.
   * The client continues working and requesting more memory.
   *
   * <p>In reality, these memory requests should be quite rare.</p>
   */
  static class MemoryHungryClient {
    WritableMemory workingMem;
    MemoryRequestServer memReqSvr;

    MemoryHungryClient(WritableMemory workingMemory) {
      this.workingMem = workingMemory;
      memReqSvr = workingMemory.getMemoryRequestServer();
    }

    void process() {
      WritableMemory newMem; //placeholder
      byte itr = 1;
      int oldWorkingCap = 0;
      int newWorkingCap = (int)workingMem.getCapacity();

      while (itr <= 4) { //limited to 4 iterations to keep the unit test time fast, but it proves the point.
        //use all the given memory
        workingMem.fill(oldWorkingCap, newWorkingCap - oldWorkingCap, itr);

        println(workingMem.toString("Size: " + newWorkingCap + " Bytes", 0, newWorkingCap, true));

        //Not big enough, expand
        oldWorkingCap = newWorkingCap;
        newWorkingCap = 2 * oldWorkingCap;
        Arena arena = Arena.ofConfined(); // new confined scope for each iteration
        newMem = memReqSvr.request(workingMem, newWorkingCap);

        //done with old memory, close it
        memReqSvr.requestClose(workingMem);
        workingMem = newMem;
        itr++;
      }
      //close the last allocation
      workingMem.getArena().close();
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
