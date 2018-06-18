/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.nio.ByteOrder;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class DirectNNWBufImplTest {

  @Test
  public void simpleTest() {
    try (WritableDirectHandle wdh = WritableMemory.allocateDirect(128)) {
      WritableMemory wmemLE = wdh.get();
      wmemLE.putShort(64, (short) 1);
      assertEquals(wmemLE.getShort(64), 1); //LE
      //create Buf
      WritableBuffer wbufLE = wmemLE.asWritableBuffer();
      assertEquals(wbufLE.getShort(64), 1); //LE
      //create BE region in upper half
      WritableBuffer regBE = wbufLE.writableRegion(64, 64, ByteOrder.BIG_ENDIAN);
      assertEquals(regBE.getShort(0), 256);
      //create another BE region in upper half from first BE region
      WritableBuffer regBE2 = regBE.writableRegion(0, 64, ByteOrder.BIG_ENDIAN);
      assertEquals(regBE2.getShort(0), 256);
      //sw back to LE
      WritableBuffer regLE = regBE.writableRegion(0, 64, ByteOrder.LITTLE_ENDIAN);
      assertEquals(regLE.getShort(0), 1);
      //make dup of regBE
      WritableBuffer regBE3 = regBE.writableDuplicate();
      assertEquals(regBE3.getShort(0), 256);
      //make another dup of regBE
      WritableBuffer regLE2 = regBE.writableDuplicate(ByteOrder.LITTLE_ENDIAN);
      assertEquals(regLE2.getShort(0), 1);

      assertNull(regBE.getByteBuffer());

      assertNotNull(regBE.getMemoryRequestServer());

      assertTrue(regBE.getNativeBaseOffset() > 0);

      assertTrue(regBE.getValid() != null);
    }
  }


}
