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
 * off-heap memory and memory-mapped file resources, and consistent views into
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
 * two different access APIs. These resources are contiguous blobs of bytes that provide at least
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
 * <p>An access API is joined with
 * a resource either with a static factory method or in combination with a
 * {@link org.apache.datasketches.memory.Handle}, which is used exclusively for resources that are
 * external to the JVM, such as allocation of direct memory and memory-mapped files.</p>
 *
 * <p>The role of a Handle is to hold onto the reference of a resource that is outside the control
 * of the JVM. The resource is obtained from the handle with {@code get()}.</p>
 *
 * <p>When a handle is extended for an AutoCloseable resource and then joined with an access API
 * it becomes an <i>implementation handle</i>. There are 3 implementation handles:</p>
 *
 * <ul><li>{@link org.apache.datasketches.memory.MapHandle}
 * for read-only access to a memory-mapped file</li>
 * <li>{@link org.apache.datasketches.memory.WritableMapHandle}
 * for writable access to a memory-mapped file</li>
 * <li>{@link org.apache.datasketches.memory.WritableHandle}
 * for writable access to off-heap memory.</li>
 * </ul>
 *
 * <p>As long as the implementation handle is valid the JVM will not attempt to close the resource.</p>
 *
 * <p>An implementation handle implements {@link java.lang.AutoCloseable},
 * which also enables compile-time checks for non-closed resources. If a Handle is acquired
 * in a try-with-resources (TWR) block, it's associated resource will be automatically closed by
 * the JVM at the end of the block.
 * The resource can also be explicitly closed by the user by calling {@code Handle.close()}.</p>
 * <blockquote><pre>
 *     //Using try-with-resources block:
 *     try (WritableyMapHandle handle = WritableMemory.map(File file)) {
 *       WritableMemory wMem = handle.get();
 *       doWork(wMem) // read and write to memory mapped file.
 *     }
 *
 *     //Using explicit close():
 *     WritableMapHandle handle = WritableMemory.map(File file);
 *     WritableMemory wMem = handle.get();
 *     doWork(wMem) // read and write to memory mapped file.
 *     handle.close();
 * </pre></blockquote>
 *
 * <p>Where it is desirable to pass ownership of the resource (and the {@code close()}
 * responsibility) one can not use the TWR block. Instead:</p>
 * <blockquote><pre>
 *     WritableMapHandle handler = WritableMemory.map(File file);
 *     doWorkAndClose(handle); //passes the handle to object that closes the resource.
 * </pre></blockquote>
 *
 * <p>Whatever part of your process is responsible for allocating a resource external
 * to the JVM must be responsible for closing it or making sure it gets closed.
 * Since only the implementation Handles implement AutoCloseable, you must not let go of the
 * handle reference until you are done with its associated resource.</p>
 *
 * <p>As mentioned above, there are two ways to do this:</p>
 * <ul><li>Use a try-with-resources block.  At the end of the block, the JVM will automatically
 * close the resource.</li>
 *
 * <li>If you need to pass an external resource, pass the implementation resource handle, not the
 * access API. This means you are also passing the responsibility to close the resource.
 * If you have different parts of your code holding references to the same handle,
 * whichever one closes it first will make all the other resources invalid, so be careful.
 * As long as there is at least one reference to the handle that is still valid and the resource
 * has not been closed, the resource will remain valid. If you drop all references to all handles,
 * the JVM will eventually close the resource, making it invalid, but it is possible that you might
 * run out of memory first. Depending on this is a bad idea and a could be a serious,
 * hard-to-find bug.</li>
 * </ul>
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
 *     Memory reg = wMem.region(offset, length);
 * </pre></blockquote>
 *
 * <p>With asserts enabled in the JVM, all methods are checked for bounds and
 * use-after-close violations.</p>
 *
 * @author Lee Rhodes
 */
package org.apache.datasketches.memory;
