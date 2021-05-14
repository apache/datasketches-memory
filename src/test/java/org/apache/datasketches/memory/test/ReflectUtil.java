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

package org.apache.datasketches.memory.test;

import java.io.File;
import java.lang.reflect.*;
import java.nio.ByteOrder;

import org.apache.datasketches.memory.internal.MemoryRequestServer;
import org.apache.datasketches.memory.internal.WritableDirectHandle;

public final class ReflectUtil {

  private ReflectUtil() {}
  
  static final Class<?> BASE_STATE;
  static final Class<?> BASE_WRITABLE_MEMORY_IMPL;
  static final Class<?> ALLOCATE_DIRECT_MAP;
  static final Class<?> NIO_BITS;
  
  static final Method CHECK_VALID; //BaseState  
  static final Method GET_DIRECT_ALLOCATIONS_COUNT; //NioBits
  static final Method GET_MAX_DIRECT_BYTE_BUFFER_MEMORY; //NioBits
  static final Method GET_NATIVE_BASE_OFFSET; //BaseState
  static final Method GET_RESERVED_MEMORY; //NioBits
  static final Method GET_TOTAL_CAPACITY; //NioBits
  static final Method GET_UNSAFE_OBJECT; //BaseState
  static final Method IS_BB_TYPE; //BaseState
  static final Method IS_BUFFER_TYPE; //BaseState
  static final Method IS_DIRECT_TYPE; //BaseState
  static final Method IS_DUPLICATE_TYPE; //BaseState
  static final Method IS_FILE_READ_ONLY; //AllocateDirectMap
  static final Method IS_HEAP_TYPE; //BaseState
  static final Method IS_MAP_TYPE; //BaseState
  static final Method IS_NON_NATIVE_TYPE; //BaseState
  static final Method IS_PAGE_ALIGHED; //NioBits
  static final Method IS_READ_ONLY_TYPE; //BaseState
  static final Method IS_REGION_TYPE; //BaseState
  static final Method PAGE_COUNT; //NioBits
  static final Method PAGE_SIZE; //NioBits
  static final Method RESERVE_MEMORY; //NioBits
  static final Method UNRESERVE_MEMORY; //NioBits
  static final Method WRAP_DIRECT; //BaseWritableMemoryImpl
  
  static {
    BASE_STATE = 
        getClass("org.apache.datasketches.memory.internal.BaseState");
    BASE_WRITABLE_MEMORY_IMPL = 
        getClass("org.apache.datasketches.memory.internal.BaseWritableMemoryImpl");
    ALLOCATE_DIRECT_MAP = 
        getClass("org.apache.datasketches.memory.internal.AllocateDirectMap");
    NIO_BITS = 
        getClass("org.apache.datasketches.memory.internal.NioBits");

    CHECK_VALID =
        getMethod(BASE_STATE, "checkValid", (Class<?>[])null); //not static
    GET_DIRECT_ALLOCATIONS_COUNT =
        getMethod(NIO_BITS, "getDirectAllocationsCount", (Class<?>[])null); //static
    GET_MAX_DIRECT_BYTE_BUFFER_MEMORY =
        getMethod(NIO_BITS, "getMaxDirectByteBufferMemory", (Class<?>[])null); //static
    GET_NATIVE_BASE_OFFSET =
        getMethod(BASE_STATE, "getNativeBaseOffset", (Class<?>[])null);
    GET_RESERVED_MEMORY =
        getMethod(NIO_BITS, "getReservedMemory", (Class<?>[])null); //static
    GET_TOTAL_CAPACITY =
        getMethod(NIO_BITS, "getTotalCapacity", (Class<?>[])null); //static
    GET_UNSAFE_OBJECT = 
        getMethod(BASE_STATE, "getUnsafeObject", (Class<?>[])null); //not static
    IS_BB_TYPE =
        getMethod(BASE_STATE, "isBBType", (Class<?>[])null); //not static
    IS_BUFFER_TYPE =
        getMethod(BASE_STATE, "isBufferType", (Class<?>[])null); //not static
    IS_DIRECT_TYPE =
        getMethod(BASE_STATE, "isDirectType", (Class<?>[])null); //not static
    IS_DUPLICATE_TYPE =
        getMethod(BASE_STATE, "isDuplicateType", (Class<?>[])null); //not static
    IS_FILE_READ_ONLY =
        getMethod(ALLOCATE_DIRECT_MAP, "isFileReadOnly", File.class);
    IS_HEAP_TYPE =
        getMethod(BASE_STATE, "isHeapType", (Class<?>[])null); //not static
    IS_MAP_TYPE =
        getMethod(BASE_STATE, "isMapType", (Class<?>[])null); //not static
    IS_NON_NATIVE_TYPE =
        getMethod(BASE_STATE, "isNonNativeType", (Class<?>[])null); //not static
    IS_PAGE_ALIGHED =
        getMethod(NIO_BITS, "isPageAligned", (Class<?>[])null); //static
    IS_READ_ONLY_TYPE =
        getMethod(BASE_STATE, "isReadOnlyType", (Class<?>[])null); //not static
    IS_REGION_TYPE =
        getMethod(BASE_STATE, "isRegionType", (Class<?>[])null); //not static
    PAGE_COUNT =
        getMethod(NIO_BITS, "pageCount", long.class); //static
    PAGE_SIZE =
        getMethod(NIO_BITS, "pageSize", (Class<?>[])null); //static
    RESERVE_MEMORY =
        getMethod(NIO_BITS, "reserveMemory", long.class, long.class); //static
    UNRESERVE_MEMORY =
        getMethod(NIO_BITS, "unreserveMemory", long.class, long.class); //static
    WRAP_DIRECT =
        getMethod(BASE_WRITABLE_MEMORY_IMPL, 
            "wrapDirect", long.class, ByteOrder.class, MemoryRequestServer.class);  //static method
  }
  
  /**
   * Gets a Class reference to the given class loaded by the SystemClassLoader. 
   * This will work for private, package-private and abstract classes. 
   * @param fullyQualifiedBinaryName the binary name is the name of the class file on disk. This does not instantiate
   * a concrete class, but allows access to constructors, static fields and static methods.
   * @return the Class object of the given class.
   */
  public static Class<?> getClass(String fullyQualifiedBinaryName) {
    try {
      ClassLoader scl = ClassLoader.getSystemClassLoader();
      return scl.loadClass(fullyQualifiedBinaryName);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets a declared constructor given the owner class and parameter types
   * @param ownerClass the Class<?> object of the class loaded by the SystemClassLoader.
   * @param parameterTypes parameter types for the constructor
   * @return the constructor
   */
  public static Constructor<?> getConstructor(final Class<?> ownerClass, final Class<?>... parameterTypes ) {
    try {
      Constructor<?> ctor = ownerClass.getDeclaredConstructor(parameterTypes);
      ctor.setAccessible(true);
      return ctor;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets a class instance from its constructor and initializing arguments.
   * @param constructor the given Constructor
   * @param initargs the initializing arguments
   * @return the instantiated class.
   */
  public static Object getInstance(final Constructor<?> constructor, final Object... initargs) {
    try {
      constructor.setAccessible(true);
      return constructor.newInstance(initargs);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Gets a declared field of the given the loaded owner class and field name. The accessible flag will be set true.
   * @param ownerClass the Class<?> object of the class loaded by the SystemClassLoader.
   * @param fieldName the desired field name
   * @return the desired field.  
   */
  public static Field getField(final Class<?> ownerClass, final String fieldName) {
    try {
      Field field = ownerClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Gets a field value given the loaded owner class and the Field. The accessible flag will be set true.
   * @param ownerClass the loaded class owning the field
   * @param field The Field object
   * @return the returned value as an object.
   */
  public static Object getFieldValue(final Class<?> ownerClass, final Field field) {
    try {
      field.setAccessible(true);
      return field.get(ownerClass);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Gets a declared method of the given the loaded owning class, method name and parameter types. 
   * The accessible flag will be set true. 
   * @param ownerClass the given 
   * @param methodName the given method name
   * @param parameterTypes the list of parameter types
   * @return the desired method.
   */
  public static Method getMethod(final Class<?> ownerClass, final String methodName, final Class<?>... parameterTypes ) {
    try {
      Method method = (parameterTypes == null) 
          ? ownerClass.getDeclaredMethod(methodName)
          : ownerClass.getDeclaredMethod(methodName, parameterTypes);
      method.setAccessible(true);
      return method;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  static void checkValid(Object target) throws Exception {
    //because method can throw an exception, all exceptions must be handled locally
    CHECK_VALID.invoke(target);
  }

//  static long getCurrentDirectMemoryAllocated() {
//    try {
//      return (long) GET_CURRENT_DIRECT_MEMORY_ALLOCATED.invoke(null);
//    } catch (Exception e) { throw new RuntimeException(e); }
//  }
//
//  static long getCurrentDirectMemoryAllocations() {
//    try {
//      return (long) GET_CURRENT_DIRECT_MEMORY_ALLOCATIONS.invoke(null);
//    } catch (Exception e) { throw new RuntimeException(e); }
//  }
//
//  static long getCurrentDirectMemoryMapAllocated() {
//    try {
//      return (long) GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATED.invoke(null);
//    } catch (Exception e) { throw new RuntimeException(e); }
//  }
//
//  static long getCurrentDirectMemoryMapAllocations() {
//    try {
//      return (long) GET_CURRENT_DIRECT_MEMORY_MAP_ALLOCATIONS.invoke(null);
//    } catch (Exception e) { throw new RuntimeException(e); }
//  }

  static long getDirectAllocationsCount() {
    try {
      return (long) GET_DIRECT_ALLOCATIONS_COUNT.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static long getMaxDirectByteBufferMemory() {
    try {
      return (long) GET_MAX_DIRECT_BYTE_BUFFER_MEMORY.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static long getNativeBaseOffset(final Object target) {
    try {
      return (long) GET_NATIVE_BASE_OFFSET.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static long getReservedMemory() {
    try {
      return (long) GET_RESERVED_MEMORY.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static long getTotalCapacity() {
    try {
      return (long) GET_TOTAL_CAPACITY.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static Object getUnsafeObject(Object target) {
    try {
      return GET_UNSAFE_OBJECT.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isBBType(final Object target) {
    try {
      return (boolean) IS_BB_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isBufferType(final Object target) {
    try {
      return (boolean) IS_BUFFER_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isDirectType(final Object target) {
    try {
      return (boolean) IS_DIRECT_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isDuplicateType(final Object target) {
    try {
      return (boolean) IS_DUPLICATE_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isFileReadOnly(final File file) {
    try {
      return (boolean) IS_FILE_READ_ONLY.invoke(null, file);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isHeapType(final Object target) {
    try {
      return (boolean) IS_HEAP_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isMapType(final Object target) {
    try {
      return (boolean) IS_MAP_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isNonNativeType(final Object target) {
    try {
      return (boolean) IS_NON_NATIVE_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isPageAligned() {
    try {
      return (boolean) IS_PAGE_ALIGHED.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isReadOnlyType(final Object target) {
    try {
      return (boolean) IS_READ_ONLY_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static boolean isRegionType(final Object target) {
    try {
      return (boolean) IS_REGION_TYPE.invoke(target);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static int pageCount(final long bytes) {
    try {
      return (int) PAGE_COUNT.invoke(null, bytes);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static int pageSize() {
    try {
      return (int) PAGE_SIZE.invoke(null);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static void reserveMemory(final long allocationSize, final long capacity) {
    try {
     RESERVE_MEMORY.invoke(null, allocationSize, capacity);
    } catch (Exception e) { 
      throw new RuntimeException(e); }
  }

  static void unreserveMemory(final long allocationSize, final long capacity) {
    try {
      UNRESERVE_MEMORY.invoke(null, allocationSize, capacity);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  static WritableDirectHandle wrapDirect(final long capacityBytes,
      final ByteOrder byteOrder, final MemoryRequestServer memReqSvr) {
    try {
      return (WritableDirectHandle) WRAP_DIRECT.invoke(null, capacityBytes, byteOrder, memReqSvr);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

}
