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

import sun.misc.Cleaner;

/**
 * The package name and API for Cleaner changed from Java 8 to Java 9+.
 * This extracts the java 8 `sun.misc.Cleaner` into a standalone class.
 */
@SuppressWarnings("restriction")
public class MemoryCleaner {
    private final Cleaner cleaner;

    /**
     * Creates a new `sun.misc.Cleaner`.
     * @param referent the object to be cleaned
     * @param deallocator - the cleanup code to be run when the cleaner is invoked.
     * return MemoryCleaner
     */
    public MemoryCleaner(final Object referent, final Runnable deallocator) {
        cleaner = Cleaner.create(referent, deallocator);
    }

    /**
     * Runs this cleaner, if it has not been run before.
     */
    public void clean() {
        cleaner.clean();
    }
}

