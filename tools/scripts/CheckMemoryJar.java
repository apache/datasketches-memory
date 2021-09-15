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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.MapHandle;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableHandle;
import org.apache.datasketches.memory.WritableMemory;

public class CheckMemoryJar {

    public void printJDK() {
        String JdkVersionString = System.getProperty("java.version");
        int JdkMajorVersion = getJavaMajorVersion(JdkVersionString);
        println("JDK Full Version : " + JdkVersionString);
        println("JDK Major Version: " + JdkMajorVersion);
        println("");
    }

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

    public void checkByteBuffer() throws Exception {
        try {
            String str = "3 - Map ByteBuffer Successful";
            ByteBuffer bb = ByteBuffer.allocateDirect(2 * str.length());
            bb.order(ByteOrder.nativeOrder());
            WritableMemory wmem = WritableMemory.writableWrap(bb);
            writeReadAndPrintString(wmem, str);
        } catch (Exception ex) {
            exitOnError("Map ByteBuffer", ex);
        }
    }

    public void checkMap(String mappedFilePath) throws Exception {
        try {
            String str = "4 - Memory Map Successful";
            File file = new File(mappedFilePath);
            MapHandle mh = Memory.map(file);
            Memory mem = mh.get();
            mh.close();
            println(str);
        } catch (Exception ex) {
            exitOnError("Memory Map", ex);
        }
    }

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Please provide the full path to the memory mapped file!");
            System.exit(1);
        }

        String mappedFilePath = args[0];
        CheckMemoryJar check = new CheckMemoryJar();
        check.printJDK();
        check.checkHeapWritableMemory();
        check.checkAllocateDirect();
        check.checkByteBuffer();
        check.checkMap(mappedFilePath);
        println("");
        println("All checks passed.");
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

    private static int getJavaMajorVersion(final String jdkVersion) {
        int[] verArr = parseJavaVersion(jdkVersion);
        return (verArr[0] == 1) ? verArr[1] : verArr[0];
    }

    /**
     * Returns first two number groups of the java version string.
     * @param jdkVersion the java version string from System.getProperty("java.version").
     * @return first two number groups of the java version string.
     */
    private static int[] parseJavaVersion(final String jdkVersion) {
        final int p0, p1;
        try {
            String[] parts = jdkVersion.trim().split("[^0-9\\.]");//grab only number groups and "."
            parts = parts[0].split("\\."); //split out the number groups
            p0 = Integer.parseInt(parts[0]); //the first number group
            p1 = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0; //2nd number group, or 0
        } catch (final NumberFormatException | ArrayIndexOutOfBoundsException  e) {
            throw new IllegalArgumentException("Improper Java -version string: " + jdkVersion + "\n" + e);
        }
        return new int[] {p0, p1};
    }

    private static void println(Object obj) { System.out.println(obj.toString()); }
}
