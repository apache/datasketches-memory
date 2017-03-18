/**
 * The goal of this package is to provide high performance access to native memory for primitives
 * and primitive arrays. It also provides compatible views into heap-based arrays and the
 * Java ByteBuffer class. This package is general purpose, has no external dependencies and can
 * be used in any application that needs to manage data structures outside the Java heap.
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
 *     Memory mem = (Memory)wMem;
 * }</pre>
 * </li>
 * <li>The Native Memory and the Memory Mapped File resources implement {@code AutoCloseable} so
 * that these resources are automatically closed if wrapped in a try-with-resources block.
 * These resources are acquired using simple handlers that implement the {@code close()}
 * method: <pre>{@code
 *     try (WritableResourceHandler handler = WritableResource.map(File file)) {
 *       WritableMemory wMem = handler.get();
 *       // read and write to memory mapped file.
 *     }
 * }</pre>
 * Where it is desirable to pass ownership of the resource (and the {@code close()} responsibility)
 * one can not use the TWR block. Instead:<pre>{@code
 *     WritableResourceHandler handler = WritableResource.map(File file);
 *     ...
 *     doWorkAndClose(handler); //passes the handler to downstream that closes the resource.
 * }</pre>
 * </li>
 *
 * <li>With asserts enabled in the JVM, all methods are checked for bounds violations and
 * use-after-close violations.</li>
 *
 * <li>Hierarchical memory regions can be easily created:<pre>{@code
 *     WritableMemory wMem = ...
 *     WritableMemory wReg = wMem.writableRegion(offset, length); //OR
 *     Memory reg = wMem.region(offset, length);
 * }</pre>
 * </li>
 * </ul>
 */
package com.yahoo.memory;
