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

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_OBJECT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_OBJECT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.BOOLEAN_SHIFT;
import static com.yahoo.memory.UnsafeUtil.BYTE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.OBJECT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;

/**
 * Creates easy to access association between the major Unsafe constants.
 *
 * @author Lee Rhodes
 */
enum Prim {
  BOOLEAN(ARRAY_BOOLEAN_BASE_OFFSET, ARRAY_BOOLEAN_INDEX_SCALE, BOOLEAN_SHIFT),
  BYTE(ARRAY_BYTE_BASE_OFFSET, ARRAY_BYTE_INDEX_SCALE, BYTE_SHIFT),
  CHAR(ARRAY_CHAR_BASE_OFFSET, ARRAY_CHAR_INDEX_SCALE, CHAR_SHIFT),
  SHORT(ARRAY_SHORT_BASE_OFFSET, ARRAY_SHORT_INDEX_SCALE, SHORT_SHIFT),
  INT(ARRAY_INT_BASE_OFFSET, ARRAY_INT_INDEX_SCALE, INT_SHIFT),
  LONG(ARRAY_LONG_BASE_OFFSET, ARRAY_LONG_INDEX_SCALE, LONG_SHIFT),
  FLOAT(ARRAY_FLOAT_BASE_OFFSET, ARRAY_FLOAT_INDEX_SCALE, FLOAT_SHIFT),
  DOUBLE(ARRAY_DOUBLE_BASE_OFFSET, ARRAY_DOUBLE_INDEX_SCALE, DOUBLE_SHIFT),
  OBJECT(ARRAY_OBJECT_BASE_OFFSET, ARRAY_OBJECT_INDEX_SCALE, OBJECT_SHIFT);

  private final long arrBaseOff_;
  private final long arrIdxScale_;
  private final long sizeShift_;

  private Prim(final long arrBaseOff, final long arrIdxScale, final long sizeShift) {
    this.arrBaseOff_ = arrBaseOff;
    this.arrIdxScale_ = arrIdxScale;
    this.sizeShift_ = sizeShift;
  }

  long off() {
    return arrBaseOff_;
  }

  long scale() {
    return arrIdxScale_;
  }

  long shift() {
    return sizeShift_;
  }

}
