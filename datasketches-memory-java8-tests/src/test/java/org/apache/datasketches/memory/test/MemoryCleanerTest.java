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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.datasketches.memory.internal.MemoryCleaner;
import org.testng.annotations.Test;

public class MemoryCleanerTest {

    @Test
    public void cleanerDeallocates() {
       SimpleDeallocator deallocator = new SimpleDeallocator();
       MemoryCleaner cleaner = new MemoryCleaner(this, deallocator);
       cleaner.clean();
       assertTrue(SimpleDeallocator.getHasRun());
    }

    @Test
    public void noDeallocation() {
        SimpleDeallocator deallocator = new SimpleDeallocator();
        new MemoryCleaner(this, deallocator);
        assertFalse(SimpleDeallocator.getHasRun());
    }

    static final class SimpleDeallocator implements Runnable {
        static final AtomicBoolean hasRun = new AtomicBoolean();

        SimpleDeallocator() {
            hasRun.set(false);
        }

        @Override
        public void run() {
            hasRun.compareAndSet(false, true);
        }

        public static Boolean getHasRun() {
            return hasRun.get();
        }
    }
}
