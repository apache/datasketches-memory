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

package org.apache.datasketches.memory;

/**
 * A handle for read-only Memory resource.
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

}
