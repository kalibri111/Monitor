package com.example.monitor;

import android.util.Log;

import java.nio.ByteBuffer;

public class BytesInterpret {
    private static final byte[] HEX_CHAR = new byte[] { '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    public static String dumpBytes(byte[] buffer) {
        if (buffer == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        for (int i = 0; i < buffer.length; i++) {
            sb.append((char) (HEX_CHAR[(buffer[i] & 0x00F0) >> 4]))
                    .append((char) (HEX_CHAR[buffer[i] & 0x000F])).append(' ');
        }
        return sb.toString();
    }

    public static float toFloat(byte[] bytes) {
        try {
            byte[] values = {bytes[1], bytes[2], bytes[3], bytes[4]};
            return ByteBuffer.wrap(values).getFloat();
        } catch (Exception e) {
            Log.e("Monitor", String.format("Cant cast to float %s", dumpBytes(bytes)));
            throw e;
        }

    }

    public static float asFloat(byte[] bytes) {
        try {
            int bigger = bytes[1] & 0xff;
            int lesser = bytes[2] & 0xff;
            return (float)((bigger << 8) + lesser) / 10;
        } catch (Exception e) {
            Log.e("Monitor", String.format("Cant interpret as float %s", dumpBytes(bytes)));
            throw e;
        }
    }

    public static int toInt8(byte value) {
        return value;
    }

    public static boolean asOn(byte value) {
        return (value & 0x01) == DeviceProtocol.MODE_ON;
    }

    public static boolean asOf(byte value) {
        return (value & 0x01) == DeviceProtocol.MODE_OF;
    }
}
