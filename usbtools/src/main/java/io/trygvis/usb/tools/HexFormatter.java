package io.trygvis.usb.tools;

import static javax.usb.util.UsbUtil.*;

import java.io.*;

public class HexFormatter {
    private static final int lineLength = 16;
    private static final int chunkSize = 8;

    public static void writeBytes(PrintStream print, byte[] bytes) {
        writeBytes(print, bytes, 0, bytes.length);
    }

    public static void writeBytes(PrintStream print, byte[] bytes, int offset, int length) {

        int size = length - offset;
        int lines = size / lineLength;

        int i = offset;
        int line;
        for (line = 0; line < lines; line++) {
            printLine(print, lineLength, bytes, i);
            i += lineLength;
            print.println();
        }

        // The last line
        int left = size % lineLength;
        printLine(print, left, bytes, i);
        if (left > 0) {
            print.println();
        }
    }

    private static void printLine(PrintStream print, int count, byte[] bytes, int i) {
        if (count == 0) {
            return;
        }

        StringBuilder buffer = new StringBuilder(count);

        for (int col = 0; col < count; col++) {
                byte b = bytes[i++];
                if (col == chunkSize) {
                    print.print("   ");
                    buffer.append(' ');
                } else if (col != 0) {
                    print.print(' ');
                }
                print.print(toHexString(b));

                if (b >= 0x20 && b < 127) {
                    buffer.append(Character.valueOf((char) b));
                } else {
                    buffer.append('.');
                }
        }

        for(int col = count; col < lineLength; col++) {
            if (col == chunkSize) {
                print.print("  ");
            }
            print.print("   ");
        }

        print.print(' ');
        print.print(buffer);
    }
}
