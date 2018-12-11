/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

/**
 * A handle for read-only resource.
 *
 * <p>The purpose of a Handle is to
 * <ul><li>Provide a <i>strong reference</i> to an external <i>resource</i>.</li>
 * <li>Extend <i>AutoCloseable</i>, which provides a means to close the resource.</li>
 * <li>Provide other capabilites unique to a particular resource.</li>
 * </ul>
 *
 * <p>Maintaining strong references to external resources is critical to avoid accidental
 * <i>use-after-free</i> scenarios, where the Garbage Collector will automatically close an external
 * resource if there are no remaining strong references to it. One very common mistake, is to allow
 * a newly created Handle to fall out-of-scope from the block where it was created, such as from a
 * try-with-resources statement. The Garbage Collector will eventually close the Handle referent
 * resource.</p>
 *
 * <p>Another <i>use-after-free</i> scenario is where a thread or agent, with access to the
 * Handle, prematurely closes a resource, when another part of the program is still using that
 * same resource. Avoiding this scenario requires careful planning and design.</p>
 *
 * <p>The design philosophy here is that whatever process created the external resource has the
 * responsibility to <i>close()</i> that resource when it is no longer needed.  This responsibility
 * can be delegated, by passing the appropriate Handle to the delegatee. In principle, however, at
 * any one time there should be only one agent holding the Handle and responsible for closing the
 * resource.</p>
 *
 * @author Lee Rhodes
 * @author Roman Leventov
 */
public interface Handle extends AutoCloseable {

  /**
   * Gets a Memory
   * @return a Memory
   */
  Memory get();

  @Override
  void close();
}
