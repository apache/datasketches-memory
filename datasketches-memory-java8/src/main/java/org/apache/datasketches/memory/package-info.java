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

/**
 * <p>This package provides high performance primitive and primitive array access to direct (native),
 * off-heap memory and memory-mapped file resources, consistent views into
 * {@link java.nio.ByteBuffer}, and on-heap primitive arrays. It can be used as a more
 * comprehensive and flexible replacement for {@link java.nio.ByteBuffer}.
 * </p>
 *
 * <p>In addition, this package provides:</p>
 *
 * <ul><li>Two different access APIs: read-only {@link org.apache.datasketches.memory.Memory} and
 * {@link org.apache.datasketches.memory.WritableMemory} for absolute offset access,
 * and read-only {@link org.apache.datasketches.memory.Buffer} and
 * {@link org.apache.datasketches.memory.WritableBuffer}
 * for relative positional access (similar to ByteBuffer).</li>
 *
 * <li>Clean separation of Read-only API from Writable API, which makes writable versus read-only
 * resources detectable at compile time.</li>
 *
 * <li>The conversion from Writable to read-only is just a cast, so no unnecessary objects are
 * created. For example:
 * <blockquote><pre>
 *     WritableMemory wMem = ...
 *     Memory mem = wMem;
 * </pre></blockquote>
 * </li>
 *
 * <li> {@link java.lang.AutoCloseable} for the external resources that require it,
 * which enables compile-time checks for non-closed resources.</li>
 *
 * <li>Immediate invalidation of all downstream references of an AutoCloseable
 * resource when that resource is closed, either manually or by the JVM.
 * This virtually eliminates the possibility of accidentally writing into the memory space
 * previously owned by a closed resource.</li>
 *
 * <li>Improved performance over the prior Memory implementation.</li>
 *
 * <li>Cleaner internal architecture, which will make it easier to extend in the future.</li>
 *
 * <li>No external dependencies, which makes it simple to install in virtually any Java environment.
 * </li>
 * </ul>
 *
 * <p>More specifically, this package provides access to four different types of resources using
 * two different access APIs. These resources can be viewed as contiguous blobs of bytes that provide at least
 * byte-level read and write access. The four resources are:</p>
 *
 * <ul><li>Direct (a.k.a. Native) off-heap memory allocated by the user.</li>
 * <li>Memory-mapped files, both writable and read-only.</li>
 * <li>{@code ByteBuffers}, both heap-based and direct, writable and read-only.</li>
 * <li>Heap-based primitive arrays, which can be accessed as writable or read-only.</li>
 * </ul>
 *
 * <p>The two different access APIs are:</p>
 * <ul><li><i>Memory, WritableMemory</i>: Absolute offset addressing into a resource.</li>
 * <li><i>Buffer, WritableBuffer</i>: Position relative addressing into a resource.</li>
 * </ul>
 *
 * <p>In addition, all combinations of access APIs and backing resources can be accessed via
 * multibyte primitive methods (e.g.
 * <i>getLong(...), getLongArray(...), putLong(...), putLongArray(...)</i>) as either
 * {@link java.nio.ByteOrder#BIG_ENDIAN} or {@link java.nio.ByteOrder#LITTLE_ENDIAN}.</p>
 *
 * <p>The resources don't know or care about the access APIs, and the access
 * APIs don't really know or care what resource they are accessing.</p>
 *
 * <p>A Direct or memory-mapped file resource can also be explicitly closed by the user</p>
 * <blockquote><pre>
 *     //Using try-with-resources block:
 *     try (WritableMemory wmem = WritableMemory.map(File file)) {
 *       doWork(wMem) // read and write to memory mapped file.
 *     }
 *
 *     //Using explicit close():
 *     WritableMemory wmem = WritableMemory.map(File file);
 *     doWork(wMem) // read and write to memory mapped file.
 *     wmem.close();
 * </pre></blockquote>
 *
 * <p>Whatever thread of your process is responsible for allocating a direct or memory-mapped resource
 * must be responsible for closing it or making sure it gets closed. This is also true for the special
 * memory-mapping methods load(), isLoaded() and force().</p>
 *
 *<p>Moving back and forth between <i>Memory</i> and <i>Buffer</i>:</p>
 *<blockquote><pre>
 *    Memory mem = ...
 *    Buffer buf = mem.asBuffer();
 *    ...
 *    Memory mem2 = buf.asMemory();
 *    ...
 * </pre></blockquote>
 *
 * <p>Hierarchical memory regions can be easily created:</p>
 * <blockquote><pre>
 *     WritableMemory wMem = ...
 *     WritableMemory wReg = wMem.writableRegion(offset, length); //OR
 *     Memory region = wMem.region(offset, length);
 * </pre></blockquote>
 *
 * <p>All methods are checked for bounds violations.</p>
 *
 * <p>The classes in this package are not thread-safe.</p>
 *
 * @author Lee Rhodes
 */
package org.apache.datasketches.memory;
