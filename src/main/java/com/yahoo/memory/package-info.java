/**
 * The goal of this package is to provide high performance access to native memory for primitives
 * and primitive arrays. It also provides consistent views into heap-based arrays,
 * Java ByteBuffers and memory mapped files. This package is general purpose, has no external
 * dependencies and can be used in any application that needs to manage data structures outside
 * the Java heap.
 *
 * <p>More specifically, this package provides access to four different types of resources using
 * two different APIs.  The resources are:
 * <ul><li>Direct (a.k.a. Native) memory allocated by the user.</li>
 * <li>{@code ByteBuffers}, both heap-based and direct, writable and read-only.</li>
 * <li>Memory-mapped files, both writable and read-only.</li>
 * <li>Heap-based primitive arrays, which can be accessed as writable or read-only.</li>
 * </ul>
 *
 * <p>The two different APIs are:
 * <ul><li><i>Memory, WritableMemory</i>: Absolute offset addressing into a resource.</li>
 * <li><i>Buffer, WritableBuffer</i>: Position relative addressing into a resource.</li>
 * </ul>
 *
 * This package provides the following capabilities:
 * <ul><li>Compile-time detection of writable versus read-only views of a resource.</li>
 *
 * <li>Converting a writable instance into a read-only instance is a simple up-cast.
 * For example: <pre>{@code
 *     WritableMemory wMem = ...
 *     Memory mem = wMem;
 * }</pre>
 * Although it is also possible to acquire writable access with a down-cast, this practice is
 * discouraged.
 * </li>
 *
 * <li>The direct memory and the memory-mapped file resources implement {@code AutoCloseable} so
 * that these resources are automatically closed if wrapped in a try-with-resources (TRW) block.
 * In addition, compile-time checks will flag these resources if they have not been closed.
 * These resources are acquired using simple handlers that implement the {@code close()}
 * method. <pre>{@code
 *     //Using TWR block:
 *     try (WritableMemoryMapHandler handler = WritableResource.map(File file)) {
 *       WritableMemory wMem = handler.get();
 *       doWork(wMem) // read and write to memory mapped file.
 *     }
 *
 *     //Using explicit close():
 *     WritableMemoryMapHandler handler = WritableResource.map(File file);
 *     WritableMemory wMem = handler.get();
 *     doWork(wMem) // read and write to memory mapped file.
 *     handler.close();
 * }</pre>
 *
 * Where it is desirable to pass ownership of the resource (and the {@code close()} responsibility)
 * one can not use the TWR block. Instead:<pre>{@code
 *     WritableMemoryMapHandler handler = WritableResource.map(File file);
 *     doWorkAndClose(handler); //passes the handler to object that closes the resource.
 * }</pre>
 * </li>
 *
 *<li>Moving back and forth between <i>Memory</i> and <i>Buffer</i>:
 *<pre>{@code
 *    Memory mem = ...
 *    Buffer buf = mem.asBuffer();
 *    ...
 *    Memory mem2 = buf.asMemory();
 *    ...
 * }</pre>
 * </li>
 *
 * <li>Hierarchical memory regions can be easily created:<pre>{@code
 *     WritableMemory wMem = ...
 *     WritableMemory wReg = wMem.writableRegion(offset, length); //OR
 *     Memory reg = wMem.region(offset, length);
 * }</pre>
 * </li>
 *
 * <li>With asserts enabled in the JVM, all methods are checked for bounds and
 * use-after-close violations.</li>
 * </ul>
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
package com.yahoo.memory;
