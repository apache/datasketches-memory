package com.yahoo.memory;

import static org.testng.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.testng.annotations.Test;

public class BufferTest2
{
  @Test
  public void testWrapByteBuf() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(0);

    Buffer buffer = Buffer.wrap(bb.asReadOnlyBuffer().order(ByteOrder.nativeOrder()));
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }

    assertEquals(true, buffer.hasArray());
    assertEquals(true, buffer.hasByteBuffer());
  }

  @Test
  public void testWrapDirectBB() {
    ByteBuffer bb = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(0);

    Buffer buffer = Buffer.wrap(bb);
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }

    assertEquals(false, buffer.hasArray());
    assertEquals(true, buffer.hasByteBuffer());
  }

  @Test
  public void testWrapByteArray() {
    byte[] byteArray = new byte[64];

    for (byte i = 0; i < 64; i++) {
      byteArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(byteArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(byteArray[i++], buffer.getByte());
    }

    buffer.setPosition(0);
    byte[] copyByteArray = new byte[64];
    buffer.getByteArray(copyByteArray, 0, 64);
    assertEquals(byteArray, copyByteArray);

    assertEquals(true, buffer.hasArray());
    assertEquals(false, buffer.hasByteBuffer());
  }

  @Test
  public void testWrapCharArray() {
    char[] charArray = new char[64];

    for (char i = 0; i < 64; i++) {
      charArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(charArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(charArray[i++], buffer.getChar());
    }

    buffer.setPosition(0);
    char[] copyCharArray = new char[64];
    buffer.getCharArray(copyCharArray, 0, 64);
    assertEquals(charArray, copyCharArray);
  }

  @Test
  public void testWrapShortArray() {
    short[] shortArray = new short[64];

    for (short i = 0; i < 64; i++) {
      shortArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(shortArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(shortArray[i++], buffer.getShort());
    }

    buffer.setPosition(0);
    short[] copyShortArray = new short[64];
    buffer.getShortArray(copyShortArray, 0, 64);
    assertEquals(shortArray, copyShortArray);
  }

  @Test
  public void testWrapIntArray() {
    int[] intArray = new int[64];

    for (int i = 0; i < 64; i++) {
      intArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(intArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(intArray[i++], buffer.getInt());
    }

    buffer.setPosition(0);
    int[] copyIntArray = new int[64];
    buffer.getIntArray(copyIntArray, 0, 64);
    assertEquals(intArray, copyIntArray);
  }

  @Test
  public void testWrapLongArray() {
    long[] longArray = new long[64];

    for (int i = 0; i < 64; i++) {
      longArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(longArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(longArray[i++], buffer.getLong());
    }

    buffer.setPosition(0);
    long[] copyLongArray = new long[64];
    buffer.getLongArray(copyLongArray, 0, 64);
    assertEquals(longArray, copyLongArray);
  }

  @Test
  public void testWrapFloatArray() {
    float[] floatArray = new float[64];

    for (int i = 0; i < 64; i++) {
      floatArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(floatArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(floatArray[i++], buffer.getFloat());
    }

    buffer.setPosition(0);
    float[] copyFloatArray = new float[64];
    buffer.getFloatArray(copyFloatArray, 0, 64);
    assertEquals(floatArray, copyFloatArray);
  }

  @Test
  public void testWrapDoubleArray() {
    double[] doubleArray = new double[64];

    for (int i = 0; i < 64; i++) {
      doubleArray[i] = i;
    }

    Buffer buffer = Buffer.wrap(doubleArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(doubleArray[i++], buffer.getDouble());
    }

    buffer.setPosition(0);
    double[] copyDoubleArray = new double[64];
    buffer.getDoubleArray(copyDoubleArray, 0, 64);
    assertEquals(doubleArray, copyDoubleArray);
  }

  @Test
  public void testWrapBooleanArray() {
    boolean[] booleanArray = new boolean[64];

    for (int i = 0; i < 64; i++) {
      if (i % 3 == 0) {
        booleanArray[i] = true;
      }
    }

    Buffer buffer = Buffer.wrap(booleanArray);
    int i = 0;
    while (buffer.hasRemaining()) {
      assertEquals(booleanArray[i++], buffer.getBoolean());
    }

    buffer.setPosition(0);
    boolean[] copyBooleanArray = new boolean[64];
    buffer.getBooleanArray(copyBooleanArray, 0, 64);
    assertEquals(booleanArray, copyBooleanArray);
  }

  @Test
  public void testByteBufferPositionPreservation() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }
  }

  @Test
  public void testGetAndHasRemaining() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    assertEquals(bb.hasRemaining(), buffer.hasRemaining());
    assertEquals(bb.remaining(), buffer.getRemaining());
  }

  @Test
  public void testGetSetIncResetPosition() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    assertEquals(bb.position(), buffer.getPosition());
    assertEquals(30, buffer.setPosition(30).getPosition());
    assertEquals(40, buffer.incrementPosition(10).getPosition());
    assertEquals(0, buffer.resetPosition().getPosition());
  }

  @Test
  public void testByteBufferSlice() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb.slice().order(ByteOrder.nativeOrder()));
    while (buffer.hasRemaining()) {
      assertEquals(bb.get(), buffer.getByte());
    }

    assertEquals(bb.position(), buffer.getPosition() + 10);
    assertEquals(30, buffer.setPosition(30).getPosition());
    assertEquals(40, buffer.incrementPosition(10).getPosition());
    assertEquals(0, buffer.resetPosition().getPosition());
  }

  @Test
  public void testDuplicateAndRegion() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb.slice().order(ByteOrder.nativeOrder())); //slice = 54
    buffer.setPosition(30);//remaining = 24
    Buffer dupBuffer = buffer.duplicate(); //all 54
    println(dupBuffer.toHexString("dup", 0, (int) dupBuffer.getCapacity()));
    Buffer regionBuffer = buffer.region();
    println(regionBuffer.toHexString("reg", 0, (int) regionBuffer.getCapacity()));

    assertEquals(dupBuffer.getStart(), buffer.getStart());
    assertEquals(regionBuffer.getStart(), buffer.getStart());
    assertEquals(dupBuffer.getEnd(), buffer.getEnd());
    assertEquals(regionBuffer.getEnd(), buffer.getEnd());
    assertEquals(dupBuffer.getPosition(), buffer.getPosition());
    assertEquals(regionBuffer.getPosition(), 0);
    assertEquals(dupBuffer.getCapacity(), buffer.getCapacity());
    assertEquals(regionBuffer.getCapacity(), buffer.getCapacity() - 30);
  }

  @Test
  public void testAsMemory() {
    ByteBuffer bb = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

    Byte b = 0;
    while (bb.hasRemaining()) {
      bb.put(b);
      b++;
    }
    bb.position(10);

    Buffer buffer = Buffer.wrap(bb);
    Memory memory = buffer.asMemory();

    assertEquals(buffer.getCapacity(), memory.getCapacity());

    while(buffer.hasRemaining()){
      assertEquals(memory.getByte(buffer.getPosition()), buffer.getByte());
    }
  }

  @Test
  public void printlnTest() {
    println("PRINTING: "+this.getClass().getName());
  }

  /**
   * @param s value to print
   */
  static void println(String s) {
    System.out.println(s); //disable here
  }

}
