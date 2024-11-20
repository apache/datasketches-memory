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

import java.lang.foreign.ValueLayout;
import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;

public class NonNativeValueLayouts {

  private NonNativeValueLayouts() { }

  static final ValueLayout.OfChar JAVA_CHAR_UNALIGNED_NON_NATIVE = ValueLayout.JAVA_CHAR_UNALIGNED
    .withOrder(NON_NATIVE_BYTE_ORDER);
  static final ValueLayout.OfDouble JAVA_DOUBLE_UNALIGNED_NON_NATIVE = ValueLayout.JAVA_DOUBLE_UNALIGNED
    .withOrder(NON_NATIVE_BYTE_ORDER);
  static final ValueLayout.OfFloat JAVA_FLOAT_UNALIGNED_NON_NATIVE = ValueLayout.JAVA_FLOAT_UNALIGNED
    .withOrder(NON_NATIVE_BYTE_ORDER);
  static final ValueLayout.OfInt JAVA_INT_UNALIGNED_NON_NATIVE = ValueLayout.JAVA_INT_UNALIGNED
    .withOrder(NON_NATIVE_BYTE_ORDER);
  static final ValueLayout.OfLong JAVA_LONG_UNALIGNED_NON_NATIVE = ValueLayout.JAVA_LONG_UNALIGNED
    .withOrder(NON_NATIVE_BYTE_ORDER);
  static final ValueLayout.OfShort JAVA_SHORT_UNALIGNED_NON_NATIVE = ValueLayout.JAVA_SHORT_UNALIGNED
    .withOrder(NON_NATIVE_BYTE_ORDER);
  
}