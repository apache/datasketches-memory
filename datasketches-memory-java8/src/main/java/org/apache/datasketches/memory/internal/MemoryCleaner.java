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
 * Extracts a version-dependent reference to the `sun.misc.Cleaner` into
 * a standalone class. The package name for Cleaner has changed in
 * later versions. The appropriate class will be loaded by the class loader
 * depending on the Java version that is used.
 * For more information, see: https://openjdk.java.net/jeps/238
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

