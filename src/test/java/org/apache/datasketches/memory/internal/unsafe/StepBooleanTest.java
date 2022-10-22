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

package org.apache.datasketches.memory.internal.unsafe;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * 
 */
public class StepBooleanTest {
    //StepBoolean checks
    @Test
    public void checkStepBoolean() {
      checkStepBoolean(true);
      checkStepBoolean(false);
    }

    private static void checkStepBoolean(boolean initialState) {
      StepBoolean step = new StepBoolean(initialState);
      assertTrue(step.get() == initialState); //confirm initialState
      step.change();
      assertTrue(step.hasChanged());      //1st change was successful
      assertTrue(step.get() != initialState); //confirm it is different from initialState
      step.change();
      assertTrue(step.get() != initialState); //Still different from initialState
      assertTrue(step.hasChanged());  //confirm it was changed from initialState value
    }


}
