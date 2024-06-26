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

import static org.testng.Assert.assertEquals;

import java.nio.ByteOrder;

import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class NonNativeWritableMemoryImplTest {
  private byte[] bArr = new byte[8];
  private final WritableMemory wmem = WritableMemory.writableWrap(bArr, ByteOrder.BIG_ENDIAN);

//Check primitives
  @Test
  public void checkCharacters() {
    int m = Character.BYTES;
    int n = ((1 << 20) / m) + m;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem1 = WritableMemory.writableWrap(arr1, ByteOrder.BIG_ENDIAN);
    for (int i = 0; i < n; i++) { wmem1.putChar(i * m, (char) i++); }
    for (int i = 0; i < n; i++) {
      assertEquals(wmem1.getChar(i * m), (char) i++);
    }
    //getArr & putArr
    char[] cArr = new char[n]; //native
    wmem1.getCharArray(0, cArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.writableWrap(arr2, ByteOrder.BIG_ENDIAN);
    wmem2.putCharArray(0, cArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkDoubles() {
    int m = Double.BYTES;
    int n = ((1 << 20) / m) + m;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem1 = WritableMemory.writableWrap(arr1, ByteOrder.BIG_ENDIAN);
    double dbl = 1.0;
    for (int i = 0; i < n; i++) { wmem1.putDouble(i * m, dbl++); }
    dbl = 1.0;
    for (int i = 0; i < n; i++) {
      assertEquals(wmem1.getDouble(i * m), dbl++);
    }
    //getArr & putArr
    double[] dblArr = new double[n]; //native
    wmem1.getDoubleArray(0, dblArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.writableWrap(arr2, ByteOrder.BIG_ENDIAN);
    wmem2.putDoubleArray(0, dblArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkFloats() {
    int m = Float.BYTES;
    int n = ((1 << 20) / m) + m;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem1 = WritableMemory.writableWrap(arr1, ByteOrder.BIG_ENDIAN);
    float flt = 1.0F;
    for (int i = 0; i < n; i++) { wmem1.putFloat(i * m, flt++); }
    flt = 1.0F;
    for (int i = 0; i < n; i++) {
      assertEquals(wmem1.getFloat(i * m), flt++);
    }
    //getArr & putArr
    float[] fltArr = new float[n]; //native
    wmem1.getFloatArray(0, fltArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.writableWrap(arr2, ByteOrder.BIG_ENDIAN);
    wmem2.putFloatArray(0, fltArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkInts() {
    int m = Integer.BYTES;
    int n = ((1 << 20) / m) + m;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem1 = WritableMemory.writableWrap(arr1, ByteOrder.BIG_ENDIAN);
    int intg = 1;
    for (int i = 0; i < n; i++) { wmem1.putInt(i * m, intg++); }
    intg = 1;
    for (int i = 0; i < n; i++) {
      assertEquals(wmem1.getInt(i * m), intg++);
    }
    //getArr & putArr
    int[] intArr = new int[n]; //native
    wmem1.getIntArray(0, intArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.writableWrap(arr2, ByteOrder.BIG_ENDIAN);
    wmem2.putIntArray(0, intArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkLongs() {
    int m = Long.BYTES;
    int n = ((1 << 20) / m) + m;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem1 = WritableMemory.writableWrap(arr1, ByteOrder.BIG_ENDIAN);
    long lng = 1;
    for (int i = 0; i < n; i++) { wmem1.putLong(i * m, lng++); }
    lng = 1;
    for (int i = 0; i < n; i++) {
      assertEquals(wmem1.getLong(i * m), lng++);
    }
    //getArr & putArr
    long[] longArr = new long[n]; //native
    wmem1.getLongArray(0, longArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.writableWrap(arr2, ByteOrder.BIG_ENDIAN);
    wmem2.putLongArray(0, longArr, 0, n);
    assertEquals(arr2, arr1);
  }

  @Test
  public void checkShorts() {
    int m = Short.BYTES;
    int n = ((1 << 20) / m) + m;
    byte[] arr1 = new byte[n * m]; //non-native
    //put & get
    WritableMemory wmem1 = WritableMemory.writableWrap(arr1, ByteOrder.BIG_ENDIAN);
    short sht = 1;
    for (int i = 0; i < n; i++) { wmem1.putShort(i * m, sht++); }
    sht = 1;
    for (int i = 0; i < n; i++) {
      assertEquals(wmem1.getShort(i * m), sht++);
    }
    //getArr & putArr
    short[] shortArr = new short[n]; //native
    wmem1.getShortArray(0, shortArr, 0, n); //wmem is non-native
    byte[] arr2 = new byte[n * m];
    WritableMemory wmem2 = WritableMemory.writableWrap(arr2, ByteOrder.BIG_ENDIAN);
    wmem2.putShortArray(0, shortArr, 0, n);
    assertEquals(arr2, arr1);
  }

  //check Region
  @Test
  public void checkRegion() {
    WritableMemory wreg = wmem.writableRegion(0, wmem.getCapacity());
    assertEquals(wreg.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

  @Test
  public void checkRegionZeros() {
    byte[] bArr1 = new byte[0];
    WritableMemory wmem1 = WritableMemory.writableWrap(bArr1, ByteOrder.BIG_ENDIAN);
    Memory reg = wmem1.region(0, wmem1.getCapacity());
    assertEquals(reg.getTypeByteOrder(), ByteOrder.BIG_ENDIAN);
  }

}
