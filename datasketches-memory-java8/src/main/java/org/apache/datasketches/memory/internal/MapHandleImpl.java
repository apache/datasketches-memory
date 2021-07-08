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

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.Memory;

class MapHandleImpl implements MapHandle {

  final AllocateDirectMap dirMap;
  BaseWritableMemoryImpl wMem;

  MapHandleImpl(final AllocateDirectMap dirMap, final BaseWritableMemoryImpl wMem) {
    this.dirMap = dirMap;
    this.wMem = wMem;
  }

  @Override
  public Memory get() {
    return wMem;
  }

  @Override
  public void close() {
    if (dirMap.doClose("MapHandle")) {
      wMem = null;
    }
  }

  @Override
  public void load() {
    dirMap.load();
  }

  @Override
  public boolean isLoaded() {
    return dirMap.isLoaded();
  }

}
