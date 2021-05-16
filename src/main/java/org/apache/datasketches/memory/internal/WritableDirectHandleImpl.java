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
import org.apache.datasketches.memory.WritableDirectHandle;

/**
 * A Handle for a writable direct memory resource.
 * Joins a WritableMemory with a writable, AutoCloseable AllocateDirect resource.
 * Please read Javadocs for {@link Handle}.
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
public final class WritableDirectHandleImpl implements WritableDirectHandle {

  /**
   * Having at least one final field makes this class safe for concurrent publication.
   */
  final AllocateDirect direct;
  private WritableMemory wMem;

  WritableDirectHandleImpl(final AllocateDirect allocatedDirect, final WritableMemory wMem) {
    direct = allocatedDirect;
    this.wMem = wMem;
  }

  @Override
  public WritableMemory get() {
    return wMem;
  }

  //AutoCloseable

  @Override
  public void close() {
    if (direct.doClose()) {
      wMem = null;
    }
  }
}
