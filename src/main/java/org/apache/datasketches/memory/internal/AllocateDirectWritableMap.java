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

import java.io.File;

/**
 * Allocates direct memory used to memory map files for write operations
 * (including those &gt; 2GB).
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 * @author Praveenkumar Venkatesan
 */
//Called from WritableMemory, implements combo of WritableMemory with WritableMap resource
final class AllocateDirectWritableMap extends AllocateDirectMap implements WritableMap {

  AllocateDirectWritableMap(final File file, final long fileOffsetBytes,
      final long capacityBytes, final boolean localReadOnly) {
    super(file, fileOffsetBytes, capacityBytes, localReadOnly);
  }

  @Override
  public void force() {
    if (resourceReadOnly) {
      throw new ReadOnlyException("Memory Mapped File is Read Only.");
    }
    try {
      MAPPED_BYTE_BUFFER_FORCE0_METHOD
          //force0 is effectively static, so ZERO_READ_ONLY_DIRECT_BYTE_BUFFER is not modified
          .invoke(AccessByteBuffer.ZERO_READ_ONLY_DIRECT_BYTE_BUFFER,
              raf.getFD(),
              nativeBaseOffset,
              capacityBytes);
    } catch (final Exception e) {
      throw new RuntimeException(String.format("Encountered %s exception in force. "
          + e.getClass()));
    }
  }
}
