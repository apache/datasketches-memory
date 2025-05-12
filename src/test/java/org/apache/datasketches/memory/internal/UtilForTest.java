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

public class UtilForTest {

  //Java does not provide reverse bytes on doubles or floats

  static double doubleReverseBytes(double value) {
    long longIn = Double.doubleToRawLongBits(value);
    long longOut = Long.reverseBytes(longIn);
    return Double.longBitsToDouble(longOut);
  }

  static float floatReverseBytes(float value) {
    int intIn = Float.floatToRawIntBits(value);
    int intOut = Integer.reverseBytes(intIn);
    return Float.intBitsToFloat(intOut);
  }

}
