package no.hackaton.usb.tools;

import static javax.usb.util.UsbUtil.*;

import java.io.*;

public class HexFormatter {
    public static void writeBytes(PrintStream print, byte[] bytes) {
        writeBytes(print, bytes, 0, bytes.length);
    }

    public static void writeBytes(PrintStream print, byte[] bytes, int offset, int length) {
        for (int i = offset; i < length; i++) {
            byte b = bytes[i];

            String s = Integer.toHexString(unsignedShort(b));
            if(s.length() == 1) {
                print.print('0');
            }
            print.print(s);
            print.print(' ');

            if (i == 0) {
                continue;
            }

            if (i % 16 == 0) {
                print.println();
            } else if (i % 8 == 0) {
                print.print("  ");
            }
        }

        print.println();
    }
}
