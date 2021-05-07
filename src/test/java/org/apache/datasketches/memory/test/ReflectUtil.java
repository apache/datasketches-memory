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

import java.lang.reflect.*;

public class ReflectUtil {

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
  
//  /**
//   * Invoke the given method along with its concreteOwnerClass and method argueents.
//   * Any exception is returned to the caller.
//   * @param method the given method
//   * @param concreteOwnerInstance the concrete owning class instance
//   * @param args the method arguments
//   * @return the result of the invoked method.
//   */
//  public static Object invokeMethod(
//      final Method method, 
//      final Object concreteOwnerInstance, 
//      Object... args) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
//      method.setAccessible(true);
//      return method.invoke(concreteOwnerInstance, args);
//  }
  
}


