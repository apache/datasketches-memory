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

package org.apache.datasketches.memory.tools.scripts;

import java.io.File;

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;

public class CheckMemoryJar {

    public void checkHeapWritableMemory() {
        try {
            String str = "1 - Heap WritableMemory Successful";
            WritableMemory mem = WritableMemory.allocate(2 * str.length());
            writeReadAndPrintString(mem, str);
        } catch (Exception ex) {
            exitOnError("Heap Writable Memory", ex);
        }
    }

    public void checkAllocateDirect() throws Exception {
        try {
            String str = "2 - Allocate Direct Successful";
            WritableHandle wh = WritableMemory.allocateDirect(2 * str.length());
            WritableMemory wmem = wh.getWritable();
            writeReadAndPrintString(wmem, str);
            wh.close();
        } catch (Exception ex) {
            exitOnError("Allocate Direct", ex);
        }
    }

    public void checkMap(String mappedFilePath) throws Exception {
        try {
            String str = "3 - Memory Map Successful";
            File file = new File(mappedFilePath);
            MapHandle mh = Memory.map(file);
            Memory mem = mh.get();
            mh.close();
            println(str);
        } catch (Exception ex) {
            exitOnError("Memory Map", ex);
        }
    }

    /**********************/

    private static void writeReadAndPrintString(WritableMemory wmem, String str) {
        int len = str.length();
        char[] cArr1 = str.toCharArray();
        wmem.putCharArray(0, cArr1, 0, len);
        char[] cArr2 = new char[len];
        wmem.getCharArray(0, cArr2, 0, len);
        String s2 = String.valueOf(cArr2);
        println(s2);
    }

    private static void exitOnError(String checkName, Exception ex){
        println(checkName + " check failed. Error: " + ex.toString());
        System.exit(1);
    }

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please provide the full path to the memory mapped file!");
            System.exit(1);
        }

        String mappedFilePath = args[0];
        CheckMemoryJar check = new CheckMemoryJar();
        check.checkHeapWritableMemory();
        check.checkAllocateDirect();
        check.checkMap(mappedFilePath);
        println("");
        println("All checks passed.");
    }

    static void println(Object obj) { System.out.println(obj.toString()); }
}
