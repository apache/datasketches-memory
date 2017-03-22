/**
 * The goal of this package is to provide high performance access to native memory for primitives
 * and primitive arrays. It also provides consistent views into heap-based arrays,
 * Java ByteBuffers and memory mapped files. This package is general purpose, has no external
 * dependencies and can be used in any application that needs to manage data structures outside
 * the Java heap.
 *
 * <p>More specifically, this package provides access to four different types of resources using
 * the same API:
 * <ul><li>Native (or Direct) Memory allocated by the user.</li>
 * <li>{@code ByteBuffers}, both heap-based and direct, writable and read-only.</li>
 * <li>Memory-mapped files, both writable and read-only.</li>
 * <li>Heap-based primitive arrays</li>
 * </ul>
 *
 * The features of this design are as follows:
 * <ul><li>The writable and read-only APIs are separated to make them compile time detectable.</li>
 *
 * <li>Converting a writable instance into a read-only instance is a simple up-cast.
 * For example: <pre>{@code
 *     WritableMemory wMem = ...
 *     Memory mem = wMem;
 * }</pre>
 * </li>
 *
 * <li>The native memory and the memory mapped file resources implement {@code AutoCloseable} so
 * that these resources are automatically closed if wrapped in a try-with-resources (TRW) block.
 * These resources are acquired using simple handlers that implement the {@code close()}
 * method: <pre>{@code
 *     try (WritableMemoryMapHandler handler = WritableResource.map(File file)) {
 *       WritableMemory wMem = handler.get();
 *       // read and write to memory mapped file.
 *     }
 * }</pre>
 * Where it is desirable to pass ownership of the resource (and the {@code close()} responsibility)
 * one can not use the TWR block. Instead:<pre>{@code
 *     WritableMemoryMapHandler handler = WritableResource.map(File file);
 *     ...
 *     doWorkAndClose(handler); //passes the handler to downstream object that closes the resource.
 * }</pre>
 * </li>
 *
 *<li>This package offers both a direct offset access API (<i>Memory</i>) and a positional API
 *(<i>Buffer</i>). Moving back and forth between these two views of the same resource is simple:
 *<pre>{@code
 *    Memory mem = ...
 *    Buffer buf = mem.asBuffer();
 *    ...
 *    Memory mem2 = buf.asMemory();
 *    ...
 * }</pre>
 * </li>
 *
 * <li>With asserts enabled in the JVM, all methods are checked for bounds and
 * use-after-close violations.</li>
 *
 * <li>Hierarchical memory regions can be easily created:<pre>{@code
 *     WritableMemory wMem = ...
 *     WritableMemory wReg = wMem.writableRegion(offset, length); //OR
 *     Memory reg = wMem.region(offset, length);
 * }</pre>
 * </li>
 * </ul>
 * @author Lee Rhodes
 */
package com.yahoo.memory;
