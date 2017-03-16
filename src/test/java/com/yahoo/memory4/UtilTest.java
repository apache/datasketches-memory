/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import static com.yahoo.memory4.Util.characterPad;
import static com.yahoo.memory4.Util.memoryRequestHandler;
import static com.yahoo.memory4.Util.zeroPad;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class UtilTest {
  @Test
  public void checkGoodCallback() {
    int k = 128;
    GoodMemoryManager goodMM = new GoodMemoryManager();
    WritableMemory origMem = goodMM.request(k);
    //expand X 2
    WritableMemory newMem = memoryRequestHandler(origMem, 2 * k, true);
    assertEquals(newMem.getCapacity(), 2 * k);
    goodMM.closeRequest(origMem, newMem);
    //expand X 2 again
    WritableMemory newMem2 = memoryRequestHandler(newMem, 4 * k, false);
    assertEquals(newMem2.getCapacity(), 4 * k);
    newMem2.toHexString("Test", 0, (int)newMem2.getCapacity());
    goodMM.closeRequest(newMem);
    goodMM.closeRequest(newMem2);
  }

  @Test
  public void checkNullMemoryReturned() {
    int k = 128;
    BadMemoryManager1 badMM1 = new BadMemoryManager1();
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(k, badMM1)) {
      WritableMemory origMem = wrh.get();
      memoryRequestHandler(origMem, 2 * k, true); //returns a null Memory
    } catch (IllegalArgumentException e) {
      //OK
    }
  }

  @Test
  public void checkSmallReturnedMemory() {
    int k = 128;
    BadMemoryManager2 badMM2 = new BadMemoryManager2();
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(k, badMM2)) {
      WritableMemory origMem = wrh.get();
      memoryRequestHandler(origMem, 2 * k, true); //returns Memory too small
    } catch (IllegalArgumentException e) {
      //OK
    }
  }

  @Test
  public void checkNullMemoryRequest() {
    int k = 128;
    BadMemoryManager3 badMM3 = new BadMemoryManager3();
    try (WritableResourceHandler wrh = WritableMemory.allocateDirect(k, badMM3)) {
      WritableMemory origMem = wrh.get();
      WritableMemory newMem = memoryRequestHandler(origMem, 2 * k, false);
      memoryRequestHandler(newMem, 4 * k, false); //returns null MemoryRequest
    } catch (IllegalArgumentException e) {
      //OK
    }
  }

  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////
  private static class GoodMemoryManager implements MemoryRequest { //Allocates what was requested
    WritableResourceHandler last = null; //simple means of tracking the last Handler allocated

    @Override
    public WritableMemory request(long capacityBytes) {
      WritableResourceHandler wrh = WritableMemory.allocateDirect(capacityBytes, this);
      last = wrh;
      //println("\nReqCap: "+capacityBytes + ", Granted: "+last.get().getCapacity());
      return last.get();
    }

    @Override
    public WritableMemory request(WritableMemory origMem, long copyToBytes, long capacityBytes) {
      WritableMemory newMem = request(capacityBytes);
      origMem.copyTo(0, newMem, 0, copyToBytes);
//      println("\nOldCap: " + origMem.getCapacity() + ", ReqCap: " + capacityBytes
//          + ", Granted: "+ newMem.getCapacity());
      return newMem;
    }

    @Override
    public void closeRequest(WritableMemory wmem) {
      if (wmem == last.get()) {
        //println("\nmem Freed bytes : " + wmem.getCapacity());
        last.close();
      }
    }

    @Override
    public void closeRequest(WritableMemory memToFree, WritableMemory newMem) {
      closeRequest(memToFree);
      //println("newMem Allocated bytes: " + newMem.getCapacity());
    }
  } //end class GoodMemoryManager

  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////
  private static class BadMemoryManager1 implements MemoryRequest { //returns a null Memory

    @Override
    public WritableMemory request(long capacityBytes) {
      return null;
    }

    @Override //not used
    public WritableMemory request(WritableMemory origMem, long copyToBytes, long capacityBytes) {
      return null;
    }

    @Override //not used
    public void closeRequest(WritableMemory wmem) { }

    @Override //not used
    public void closeRequest(WritableMemory memToFree, WritableMemory newMem) { }
  } //end class BadMemoryManager1

  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////
  private static class BadMemoryManager2 implements MemoryRequest { //Allocates too small
    WritableResourceHandler last = null; //simple means of tracking the last Handler allocated

    @Override
    public WritableMemory request(long capacityBytes) {
      WritableResourceHandler wrh = WritableMemory.allocateDirect(capacityBytes -1, this);
      last = wrh;
      //println("\nReqCap: "+capacityBytes + ", Granted: "+last.get().getCapacity());
      return last.get();
    }

    @Override
    public WritableMemory request(WritableMemory origMem, long copyToBytes, long capacityBytes) {
      WritableMemory newMem = request(capacityBytes);
      origMem.copyTo(0, newMem, 0, copyToBytes);
//      println("\nOldCap: " + origMem.getCapacity() + ", ReqCap: " + capacityBytes
//          + ", Granted: "+ newMem.getCapacity());
      return newMem;
    }

    @Override //not used
    public void closeRequest(WritableMemory wmem) { }

    @Override //not used
    public void closeRequest(WritableMemory memToFree, WritableMemory newMem) { }
  } //end class BadMemoryManager2
  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////
  private static class BadMemoryManager3 implements MemoryRequest { //returns a null MemoryRequest
    WritableResourceHandler last = null; //simple means of tracking the last Handler allocated

    @Override
    public WritableMemory request(long capacityBytes) {
      WritableResourceHandler wrh = WritableMemory.allocateDirect(capacityBytes, null); //bad!
      last = wrh;
      //println("\nReqCap: "+capacityBytes + ", Granted: "+last.get().getCapacity());
      return last.get();
    }

    @Override //not used
    public WritableMemory request(WritableMemory origMem, long copyToBytes, long capacityBytes) {
      return null;
    }

    @Override //not used
    public void closeRequest(WritableMemory wmem) { }

    @Override //not used
    public void closeRequest(WritableMemory memToFree, WritableMemory newMem) { }
  } //end class BadMemoryManager1

  //////////////////////////////////////////////////////
  //////////////////////////////////////////////////////

  //Binary Search
  @Test
  public void checkBinarySearch() {
    int k = 1024; //longs
    WritableMemory wMem = WritableMemory.allocate(k << 3); //1024 longs
    for (int i = 0; i < k; i++) { wMem.putLong(i << 3, i); }
    long idx = Util.binarySearchLongs(wMem, 0, k - 1, k / 2);
    long val = wMem.getLong(idx << 3);
    assertEquals(idx, k/2);
    assertEquals(val, k/2);

    idx = Util.binarySearchLongs(wMem, 0, k - 1, k);
    assertEquals(idx, -1024);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void checkBoundsTest() {
    UnsafeUtil.checkBounds(999, 2, 1000);
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
