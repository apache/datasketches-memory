package org.apache.datasketches.memory.internal;

import org.testng.annotations.BeforeSuite;

public class A_BeforeSuite {

  @BeforeSuite
  public void printTestEnvironment() {
    System.out.println("====================================================");
    System.out.println("TEST JDK: " + System.getProperty("java.version"));
    System.out.println("TEST JDK HOME: " + System.getProperty("java.home"));
    System.out.println("=====================================================");
  }
}
