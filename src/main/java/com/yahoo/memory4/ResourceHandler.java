/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

/**
 * Wraps a resource for read operations
 * @author Lee Rhodes
 */
public interface ResourceHandler extends AutoCloseable {

  /**
   * Returns a Memory for read operations
   * @return a Memory for read operations
   */
  Memory get();

  @Override
  void close();

  /**
   * Applies only to mapped files. Otherwise is a no-op.
   * Loads content into physical memory. This method makes a best effort to ensure that, when it
   * returns, this buffer's content is resident in physical memory. Invoking this method may cause
   * some number of page faults and I/O operations to occur.
   *
   * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/MappedByteBuffer.html#load--">
   * java/nio/MappedByteBuffer.load</a>
   */
  void load();

  /**
   * Applies only to mapped files. Otherwise always returns false.
   * Tells whether or not the content is resident in physical memory. A return value of true implies
   * that it is highly likely that all of the data in this buffer is resident in physical memory and
   * may therefore be accessed without incurring any virtual-memory page faults or I/O operations. A
   * return value of false does not necessarily imply that the content is not resident in physical
   * memory. The returned value is a hint, rather than a guarantee, because the underlying operating
   * system may have paged out some of the buffer's data by the time that an invocation of this
   * method returns.
   *
   * @return true if loaded
   *
   * @see <a href=
   * "https://docs.oracle.com/javase/8/docs/api/java/nio/MappedByteBuffer.html#isLoaded--"> java
   * /nio/MappedByteBuffer.isLoaded</a>
   */
  boolean isLoaded();

  enum ResourceType { MEMORY_MAPPED_FILE, NATIVE_MEMORY }

  ResourceType getResourceType();

  boolean isResourceType(ResourceType resourceType);
}
