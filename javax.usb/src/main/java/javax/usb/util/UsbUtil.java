package javax.usb.util;

import java.io.*;

public class UsbUtil {

    private static final char[] hexDigits = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    // -----------------------------------------------------------------------
    // Hex Formatting
    // -----------------------------------------------------------------------

    public static String toHexString(byte b) {
        int i = unsignedInt(b);
        return new String(new char[]{
            hexDigits[i >> 4],
            hexDigits[i & 0x0f],
        });
    }

    public static String toHexString(short i) {
        return new String(new char[]{
            hexDigits[(i >> 12) & 0x0f],
            hexDigits[(i >> 8) & 0x0f],
            hexDigits[(i >> 4) & 0x0f],
            hexDigits[i & 0x0f],
        });
    }

    public static String toHexString(int i) {
        return new String(new char[]{
            hexDigits[(i >> 28) & 0x0f],
            hexDigits[(i >> 24) & 0x0f],
            hexDigits[(i >> 20) & 0x0f],
            hexDigits[(i >> 16) & 0x0f],
            hexDigits[(i >> 12) & 0x0f],
            hexDigits[(i >> 8) & 0x0f],
            hexDigits[(i >> 4) & 0x0f],
            hexDigits[i & 0x0f],
        });
    }

    // -----------------------------------------------------------------------
    // To Unsigned
    // -----------------------------------------------------------------------

    public static short unsignedShort(byte b) {
        return (short) (0x00ff & b);
    }

    public static int unsignedInt(byte b) {
        return 0x000000ff & b;
    }

    public static int unsignedInt(short s) {
        return 0x0000ffff & s;
    }

    public static long unsignedLong(byte b) {
        return 0x00000000000000ff & b;
    }

    public static long unsignedLong(short s) {
        return 0x000000000000ffff & s;
    }

    public static long unsignedLong(int s) {
        return 0x00000000ffffffff & s;
    }

    public static String twoDigitBdc(short bdc) {
        return ((bdc & 0xf000) >> 12) + ((bdc & 0x0f00) >> 8) + "." +
            ((bdc & 0x00f0) >> 4) + (bdc & 0x000f);
    }

    // -----------------------------------------------------------------------
    // Extras
    // -----------------------------------------------------------------------

    public static String decodeLibusbError(int code) {
        switch (code) {
            case 0:
                return "success";
            case -1:
                return "io";
            case -2:
                return "invalid parameter";
            case -3:
                return "access";
            case -4:
                return "no device";
            case -5:
                return "not found";
            case -6:
                return "busy";
            case -7:
                return "timeout";
            case -8:
                return "overflow";
            case -9:
                return "pipe";
            case -10:
                return "interrupted";
            case -11:
                return "no mem";
            case -12:
                return "not supported";
            case -99:
                return "other";
            default:
                return "unknown";
        }
    }

    /**
     * Not a part of the specification.
     */
    public static void close(Object o) {
        if (o instanceof Closeable) {
            Closeable closeable = (Closeable) o;

            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
