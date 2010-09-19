package no.hackaton.usb.tools;

import static javax.usb.util.UsbUtil.*;
import no.hackaton.usb.tools.IntelHex.*;
import static no.hackaton.usb.tools.IntelHex.RecordType.*;
import static no.hackaton.usb.tools.IntelHex.createLine;
import static org.junit.Assert.*;
import org.junit.*;

import java.io.*;
import java.util.*;

public class IntelHexTest {
    @Test
    public void testCreateLine() {
        assertEquals(":040000000201BB320C", createLine(DATA, 0, new byte[]{0x02, 0x01, (byte)0xbb, 0x32}));
    }
    
    @Test
    public void testParseLine() throws Exception {
        IntelHexPacket hexPacket = IntelHex.parseLine(1, ":040000000201BB320C");
        assertNotNull(hexPacket);
        assertEquals(2, hexPacket.lineNo);
        assertEquals(DATA, hexPacket.recordType);
        assertEquals(0x0000, hexPacket.address);
        assertEquals(4, hexPacket.data.length);
        assertEquals(0x02, hexPacket.data[0]);
        assertEquals(0x01, hexPacket.data[1]);
        assertEquals(0xbb, unsignedInt(hexPacket.data[2]));
        assertEquals(0x32, hexPacket.data[3]);
    }

    @Test
    public void testParseFile() throws Exception {
        ByteArrayInputStream reader = new ByteArrayInputStream((":040000000201BB320C\n" +
            ":01000B0032C2").getBytes("ascii"));
        List<IntelHexPacket> packets = IntelHex.openIntelHexFile(reader);
        IntelHexPacket packet0 = packets.get(0);
        IntelHexPacket packet1 = packets.get(1);
        assertEquals(1, packet0.lineNo);
        assertEquals(RecordType.DATA, packet0.recordType);
        assertEquals(0, packet0.address);
        assertArrayEquals(new byte[]{0x02, 0x01, (byte)0xbb, 0x32}, packet0.data);

        assertEquals(2, packet1.lineNo);
        assertEquals(RecordType.DATA, packet1.recordType);
        assertEquals(0x0b, packet1.address);
        assertArrayEquals(new byte[]{0x32}, packet1.data);
    }
}
