package no.hackaton.usb.tools;

import static no.hackaton.usb.tools.HexFormatter.writeBytes;
import static org.junit.Assert.*;
import org.junit.*;

import java.io.*;

public class HexFormatterTest {

    static String EOL = System.getProperty("line.separator");

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintStream print = new PrintStream(os);

    @Test
    public void testEmptyBuffer() {
        writeBytes(print, new byte[0]);
        assertEquals("", os.toString());
    }

    @Test
    public void test7Bytes() {
        writeBytes(print, new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g'});
        assertEquals("61 62 63 64 65 66 67                              abcdefg" + EOL, os.toString());
    }

    @Test
    public void test8Bytes() {
        writeBytes(print, new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'});
        assertEquals("61 62 63 64 65 66 67 68                           abcdefgh" + EOL, os.toString());
    }

    @Test
    public void test9Bytes() {
        writeBytes(print, new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'});
        assertEquals("61 62 63 64 65 66 67 68   69                      abcdefgh i" + EOL, os.toString());
    }

    @Test
    public void test16Bytes() {
        writeBytes(print, new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p'});
        assertEquals("61 62 63 64 65 66 67 68   69 6a 6b 6c 6d 6e 6f 70 abcdefgh ijklmnop" + EOL, os.toString());
    }

    @Test
    public void test20Bytes() {
        writeBytes(print, new byte[]{'a', 0x00, 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't'});
        assertEquals("61 00 63 64 65 66 67 68   69 6a 6b 6c 6d 6e 6f 70 a.cdefgh ijklmnop" + EOL +
                     "71 72 73 74                                       qrst" + EOL, os.toString());
    }

    @Test
    public void test26Bytes() {
        writeBytes(print, new byte[]{'a', 0x00, 127, 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'});
        assertEquals("61 00 7f 64 65 66 67 68   69 6a 6b 6c 6d 6e 6f 70 a..defgh ijklmnop" + EOL +
                     "71 72 73 74 75 76 77 78   79 7a                   qrstuvwx yz" + EOL, os.toString());
    }
}
