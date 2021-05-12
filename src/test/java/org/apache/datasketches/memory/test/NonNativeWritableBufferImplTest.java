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

import java.nio.ByteOrder;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("javadoc")
public class NonNativeWritableBufferImplTest {

//Check primitives
  @Test
  public void checkCharacters() {
    int n = 8;
    int m = Character.BYTES;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem = WritableMemory.wrap(arr1, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    char ch = 'a';
    for (int i = 0; i < n; i++) { wbuf.putChar(i * m, ch++); }
    ch = 'a';
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getChar(i * m), ch++);
    }
    ch = 'a';
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) { wbuf.putChar(ch++); }
    ch = 'a';
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getChar(), ch++);
    }
    //getArr & putArr
    char[] cArr = new char[n]; //native
    wbuf.setPosition(0);
    wbuf.getCharArray(cArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.wrap(arr2, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf2 = wmem2.asWritableBuffer();
    wbuf2.putCharArray(cArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkDoubles() {
    int n = 8;
    int m = Double.BYTES;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem = WritableMemory.wrap(arr1, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    double dbl = 1.0;
    for (int i = 0; i < n; i++) { wbuf.putDouble(i * m, dbl++); }
    dbl = 1.0;
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getDouble(i * m), dbl++);
    }
    dbl = 1.0;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) { wbuf.putDouble(dbl++); }
    dbl = 1.0;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getDouble(), dbl++);
    }
    //getArr & putArr
    double[] dblArr = new double[n]; //native
    wbuf.setPosition(0);
    wbuf.getDoubleArray(dblArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.wrap(arr2, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf2 = wmem2.asWritableBuffer();
    wbuf2.putDoubleArray(dblArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkFloats() {
    int n = 8;
    int m = Float.BYTES;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem = WritableMemory.wrap(arr1, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    float flt = 1.0F;
    for (int i = 0; i < n; i++) { wbuf.putFloat(i * m, flt++); }
    flt = 1.0F;
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getFloat(i * m), flt++);
    }
    flt = 1.0F;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) { wbuf.putFloat(flt++); }
    flt = 1.0F;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getFloat(), flt++);
    }
    //getArr & putArr
    float[] fltArr = new float[n]; //native
    wbuf.setPosition(0);
    wbuf.getFloatArray(fltArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.wrap(arr2, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf2 = wmem2.asWritableBuffer();
    wbuf2.putFloatArray(fltArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkInts() {
    int n = 8;
    int m = Integer.BYTES;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem = WritableMemory.wrap(arr1, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    int intg = 1;
    for (int i = 0; i < n; i++) { wbuf.putInt(i * m, intg++); }
    intg = 1;
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getInt(i * m), intg++);
    }
    intg = 1;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) { wbuf.putInt(intg++); }
    intg = 1;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getInt(), intg++);
    }
    //getArr & putArr
    int[] intArr = new int[n]; //native
    wbuf.setPosition(0);
    wbuf.getIntArray(intArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.wrap(arr2, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf2 = wmem2.asWritableBuffer();
    wbuf2.putIntArray(intArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkLongs() {
    int n = 8;
    int m = Long.BYTES;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem = WritableMemory.wrap(arr1, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    long lng = 1;
    for (int i = 0; i < n; i++) { wbuf.putLong(i * m, lng++); }
    lng = 1;
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getLong(i * m), lng++);
    }
    lng = 1;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) { wbuf.putLong(lng++); }
    lng = 1;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getLong(), lng++);
    }
    //getArr & putArr
    long[] longArr = new long[n]; //native
    wbuf.setPosition(0);
    wbuf.getLongArray(longArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.wrap(arr2, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf2 = wmem2.asWritableBuffer();
    wbuf2.putLongArray(longArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkShorts() {
    int n = 8;
    int m = Short.BYTES;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem = WritableMemory.wrap(arr1, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    short sht = 1;
    for (int i = 0; i < n; i++) { wbuf.putShort(i * m, sht++); }
    sht = 1;
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getShort(i * m), sht++);
    }
    sht = 1;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) { wbuf.putShort(sht++); }
    sht = 1;
    wbuf.setPosition(0);
    for (int i = 0; i < n; i++) {
      assertEquals(wbuf.getShort(), sht++);
    }
    //getArr & putArr
    short[] shortArr = new short[n]; //native
    wbuf.setPosition(0);
    wbuf.getShortArray(shortArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.wrap(arr2, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf2 = wmem2.asWritableBuffer();
    wbuf2.putShortArray(shortArr, 0, n);
    assertEquals(arr2, arr1);
  }

  //check Duplicate, Region
  @Test
  public void checkDuplicate() {
    byte[] bArr = new byte[8];
    WritableMemory wmem = WritableMemory.wrap(bArr, ByteOrder.BIG_ENDIAN);
    WritableBuffer wbuf = wmem.asWritableBuffer();
    WritableBuffer wdup = wbuf.writableDuplicate();
    assertEquals(wdup.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);

    WritableBuffer wreg = wbuf.writableRegion();
    assertEquals(wreg.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkDuplicateZeros() {
    byte[] bArr = new byte[0];
    WritableMemory wmem = WritableMemory.wrap(bArr, ByteOrder.BIG_ENDIAN);
    Buffer buf = wmem.asBuffer();
    Buffer dup = buf.duplicate();
    assertEquals(dup.getTypeByteOrder(), ByteOrder.LITTLE_ENDIAN);

    Buffer reg = buf.region();
    assertEquals(reg.getTypeByteOrder(), ByteOrder.LITTLE_ENDIAN);
  }

}
