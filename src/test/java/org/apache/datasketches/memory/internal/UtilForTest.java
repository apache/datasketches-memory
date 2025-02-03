package org.apache.datasketches.memory.internal;

import static org.apache.datasketches.memory.internal.ResourceImpl.NATIVE_BYTE_ORDER;
import static org.apache.datasketches.memory.internal.ResourceImpl.NON_NATIVE_BYTE_ORDER;

import java.nio.ByteOrder;

public class UtilForTest {

  static final int BB = Byte.BYTES;
  static final int CB = Character.BYTES;
  static final int SB = Short.BYTES;
  static final int IB = Integer.BYTES;
  static final int LB = Long.BYTES;
  static final int DB = Double.BYTES;
  static final int FB = Float.BYTES;
  static ByteOrder NNBO = NON_NATIVE_BYTE_ORDER;
  static ByteOrder NBO = NATIVE_BYTE_ORDER;

  //Java does not provide reverse bytes on doubles or floats

  static double doubleReverseBytes(double value) {
    long longIn = Double.doubleToRawLongBits(value);
    long longOut = Long.reverseBytes(longIn);
    return Double.longBitsToDouble(longOut);
  }

  static float floatReverseBytes(float value) {
    int intIn = Float.floatToRawIntBits(value);
    int intOut = Integer.reverseBytes(intIn);
    return Float.intBitsToFloat(intOut);
  }

}
