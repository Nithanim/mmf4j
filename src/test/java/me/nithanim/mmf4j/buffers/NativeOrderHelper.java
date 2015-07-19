package me.nithanim.mmf4j.buffers;

import java.nio.ByteOrder;

public class NativeOrderHelper {
    private static final boolean isNativeOrder = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    public static short nativeShort(int value) {
        if ((value >> 2 * 8) != 0) {
            throw new IllegalArgumentException("Input is not a short!");
        }
        return isNativeOrder ? (short) value : Short.reverseBytes((short) value);
    }

    public static int nativeInt(int value) {
        return isNativeOrder ? value : Integer.reverseBytes(value);
    }

    public static long nativeLong(long value) {
        return isNativeOrder ? value : Long.reverseBytes(value);
    }

    public static float nativeFloat(float value) {
        if (isNativeOrder) {
            return value;
        } else {
            return Float.intBitsToFloat(
                Integer.reverseBytes(
                    Float.floatToRawIntBits(value)));
        }
    }
}
