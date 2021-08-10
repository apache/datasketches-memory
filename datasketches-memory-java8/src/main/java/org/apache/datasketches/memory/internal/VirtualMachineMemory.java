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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Extracts a version-dependent reference to the `sun.misc.VM` into a standalone
 * class. The package name for VM has changed in later versions. The appropriate
 * class will be loaded by the class loader depending on the Java version that
 * is used.
 * For more information, see: https://openjdk.java.net/jeps/238
 */
public final class VirtualMachineMemory {

    private static final Class<?> VM_CLASS;
    private static final Method VM_MAX_DIRECT_MEMORY_METHOD;
    private static final Method VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD;
    private static final long maxDBBMemory;
    private static final boolean isPageAligned;

    static {
        try {
            VM_CLASS = Class.forName("sun.misc.VM");
            VM_MAX_DIRECT_MEMORY_METHOD = VM_CLASS.getDeclaredMethod("maxDirectMemory");
            VM_MAX_DIRECT_MEMORY_METHOD.setAccessible(true);
            maxDBBMemory = (long) VM_MAX_DIRECT_MEMORY_METHOD.invoke(null); // static method

            VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD = VM_CLASS.getDeclaredMethod("isDirectMemoryPageAligned");
            VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD.setAccessible(true);
            isPageAligned = (boolean) VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD.invoke(null); // static method
        } catch (final ClassNotFoundException | NoSuchMethodException |  IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            throw new RuntimeException("Could not acquire sun.misc.VM class: " + e.getClass());
        }
    }

    /**
     * Returns the maximum amount of allocatable direct buffer memory. The
     * directMemory variable is initialized during system initialization.
     *
     * @return the maximum amount of allocatable direct buffer memory.
     */
    public static long getMaxDBBMemory() {
        return maxDBBMemory;
    }

    /**
     * Returns true if the direct buffers should be page aligned.
     *
     * @return flag that determines whether direct buffers should be page aligned.
     */
    public static boolean getIsPageAligned() {
        return isPageAligned;
    }
}
