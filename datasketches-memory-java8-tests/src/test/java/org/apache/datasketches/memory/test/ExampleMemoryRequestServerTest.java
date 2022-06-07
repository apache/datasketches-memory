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

import static org.testng.Assert.assertFalse;

import java.nio.ByteOrder;
import java.util.IdentityHashMap;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * Examples of how to use the MemoryRequestServer with a memory hungry client.
 * @author Lee Rhodes
 */
public class ExampleMemoryRequestServerTest {

  /**
   * This version is without a TWR block.all of the memory allocations are done through the MemoryRequestServer
   * and each is closed by the MemoryClient when it is done with each.
   * @throws Exception
   */
  @Test
  public void checkExampleMemoryRequestServer1() throws Exception {
    int bytes = 8;
    ExampleMemoryRequestServer svr = new ExampleMemoryRequestServer();
    try (WritableHandle wh = WritableMemory.allocateDirect(8)) {
      WritableMemory memStart = wh.getWritable();
      WritableMemory wMem = svr.request(memStart, bytes);
      MemoryClient client = new MemoryClient(wMem);
      client.process();
      svr.cleanup();
    }
  }

  /**
   * In this version the first memory allocation is done up front in a TWR block.
   * And then the MemoryClient allocates new memories as needed, which are then closed
   * by the MemoryClient when it is done with the new memory allocations.
   * The initial allocation stays open until the end where it is closed at the end of the
   * TWR scope.
   * @throws Exception
   */
  @Test
  public void checkExampleMemoryRequestServer2() throws Exception {
    int bytes = 8;
    ExampleMemoryRequestServer svr = new ExampleMemoryRequestServer();
    try (WritableHandle handle = WritableMemory.allocateDirect(bytes, ByteOrder.nativeOrder(), svr)) {
      WritableMemory memStart = handle.getWritable();
      MemoryClient client = new MemoryClient(memStart);
      client.process();
      svr.cleanup(); //just to be sure all are closed.
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkZeroCapacity() throws Exception {
    ExampleMemoryRequestServer svr = new ExampleMemoryRequestServer();
    try (WritableHandle wh = WritableMemory.allocateDirect(0, ByteOrder.nativeOrder(), svr)) {

    }
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
      println(smallMem.toHexString("Small", 0, (int)cap1));

      WritableMemory bigMem = svr.request(smallMem, 2 * cap1); //get bigger mem
      long cap2 = bigMem.getCapacity();
      smallMem.copyTo(0, bigMem, 0, cap1);    //copy data from small to big
      svr.requestClose(smallMem, bigMem);     //done with smallMem, release it

      bigMem.fill(cap1, cap1, (byte) 2);      //fill the rest of bigMem, still not big enough
      println(bigMem.toHexString("Big", 0, (int)cap2));

      WritableMemory giantMem = svr.request(bigMem, 2 * cap2); //get giant mem
      long cap3 = giantMem.getCapacity();
      bigMem.copyTo(0, giantMem, 0, cap2);    //copy data from small to big
      svr.requestClose(bigMem, giantMem);     //done with bigMem, release it

      giantMem.fill(cap2, cap2, (byte) 3);    //fill the rest of giantMem
      println(giantMem.toHexString("Giant", 0, (int)cap3));
      svr.requestClose(giantMem, null);                 //done with giantMem, release it
    }
  }

  /**
   * This example MemoryRequestServer is simplistic but demonstrates one of many ways to
   * possibly manage the continuous requests for larger memory and to track the associations between
   * handles and their associated memory.
   */
  public static class ExampleMemoryRequestServer implements MemoryRequestServer {
    IdentityHashMap<WritableMemory, WritableHandle> map = new IdentityHashMap<>();

    @Override
    public WritableMemory request(WritableMemory currentWMem, long capacityBytes) {
     ByteOrder order = currentWMem.getTypeByteOrder();
     WritableHandle handle = WritableMemory.allocateDirect(capacityBytes, order, this);
     WritableMemory wmem = handle.getWritable();
     map.put(wmem, handle); //We track the newly allocated memory and its handle.
     return wmem;
    }

    @Override
    //here we actually release it, in reality it might be a lot more complex.
    public void requestClose(WritableMemory memToRelease, WritableMemory newMemory) {
      WritableHandle handle = map.get(memToRelease);
      if (handle != null && handle.getWritable() == memToRelease) {
        try {
          handle.close();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }

    public void cleanup() {
      map.forEach((k,v) -> {
        assertFalse(k.isValid()); //all entries in the map should be invalid
        try {
          v.close();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
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
