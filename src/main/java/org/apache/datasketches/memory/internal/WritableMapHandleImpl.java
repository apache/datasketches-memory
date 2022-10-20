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

import org.apache.datasketches.memory.Handle;
import org.apache.datasketches.memory.WritableMapHandle;
import org.apache.datasketches.memory.WritableMemory;

/**
 * A Handle for a memory-mapped, writable file resource.
 * Joins a WritableHandle with an AutoCloseable WritableMap resource
 * Please read Javadocs for {@link Handle}.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
public final class WritableMapHandleImpl extends MapHandleImpl
    implements WritableMapHandle {

  WritableMapHandleImpl(
      final AllocateDirectWritableMap dirWmap,
      final BaseWritableMemoryImpl wMem) {
    super(dirWmap, wMem);
  }

  @Override
  public WritableMemory getWritable() {
    return wMem;
  }

  @Override
  public void force() {
    ((AllocateDirectWritableMap)dirMap).force();
  }
}
