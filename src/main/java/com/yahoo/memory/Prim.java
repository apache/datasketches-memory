/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
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
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;

enum Prim {
  BOOLEAN(ARRAY_BOOLEAN_BASE_OFFSET, ARRAY_BOOLEAN_INDEX_SCALE, BOOLEAN_SHIFT),
  BYTE(ARRAY_BYTE_BASE_OFFSET, ARRAY_BYTE_INDEX_SCALE, BYTE_SHIFT),
  CHAR(ARRAY_CHAR_BASE_OFFSET, ARRAY_CHAR_INDEX_SCALE, CHAR_SHIFT),
  SHORT(ARRAY_SHORT_BASE_OFFSET, ARRAY_SHORT_INDEX_SCALE, SHORT_SHIFT),
  INT(ARRAY_INT_BASE_OFFSET, ARRAY_INT_INDEX_SCALE, INT_SHIFT),
  LONG(ARRAY_LONG_BASE_OFFSET, ARRAY_LONG_INDEX_SCALE, LONG_SHIFT),
  FLOAT(ARRAY_FLOAT_BASE_OFFSET, ARRAY_FLOAT_INDEX_SCALE, FLOAT_SHIFT),
  DOUBLE(ARRAY_DOUBLE_BASE_OFFSET, ARRAY_DOUBLE_INDEX_SCALE, DOUBLE_SHIFT),
  OBJECT(ARRAY_OBJECT_BASE_OFFSET, ARRAY_OBJECT_INDEX_SCALE, 1);

  private final int arrBaseOff_;
  private final int arrIdxScale_;
  private final int sizeShift_;

  private Prim(final int arrBaseOff, final int arrIdxScale, final int sizeShift) {
    this.arrBaseOff_ = arrBaseOff;
    this.arrIdxScale_ = arrIdxScale;
    this.sizeShift_ = sizeShift;
  }

  int off() {
    return arrBaseOff_;
  }

  int scale() {
    return arrIdxScale_;
  }

  int shift() {
    return sizeShift_;
  }

}
