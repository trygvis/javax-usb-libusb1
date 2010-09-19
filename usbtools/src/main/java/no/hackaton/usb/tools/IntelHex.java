package no.hackaton.usb.tools;

import static java.lang.Integer.*;
import static no.hackaton.usb.tools.IntelHex.RecordType.*;

import javax.usb.util.*;
import java.io.*;
import java.util.*;

public class IntelHex {
    public static List<IntelHexPacket> openIntelHexFile(final File file) throws IOException {
        return openIntelHexFile(new FileInputStream(file));
    }

    public static List<IntelHexPacket> openIntelHexFile(final InputStream is) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ascii"));
        String line = reader.readLine();

        if (line == null) {
            throw new IOException("The Intel hex file must contain at least one line");
        }

        IntelHexPacket packet = parseLine(0, line);

        List<IntelHexPacket> list = new ArrayList<IntelHexPacket>();

        while (true) {
            list.add(packet);

            line = reader.readLine();
            if (line == null) {
                break;
            }

            packet = parseLine(packet.lineNo, line);
        }

        return list;
    }

    public static enum RecordType {
        DATA,
        END_OF_FILE,
        EXTENDED_SEGMENT_ADDRESS_RECORD,
        START_SEGMENT_ADDRESS_RECORD,
        EXTENDED_LINEAR_ADDRESS_RECORD,
        START_LINEAR_ADDRESS_RECORD
    }

    public static class IntelHexPacket {
        public final int lineNo;
        public final RecordType recordType;
        public final int address;
        public final byte[] data;

        public IntelHexPacket(int lineNo, RecordType recordType, int address, byte[] data) {
            this.lineNo = lineNo;
            this.recordType = recordType;
            this.address = address;
            this.data = data;
        }
    }

    public static String createLine(RecordType recordType, int address, byte[] data) {
        byte r;

        switch (recordType) {
            case DATA:
                r = 0;
                break;
            case END_OF_FILE:
                r = 4;
                break;
            default:
                throw new RuntimeException("Un-supported record type: " + recordType + ".");
        }

        StringBuilder builder = new StringBuilder(9 + data.length * 2);
        builder.append(':')
            .append(UsbUtil.toHexString((byte) data.length))
            .append(UsbUtil.toHexString((short) address))
            .append('0')
            .append(r);

        int checksum = (byte) data.length + (short) address + r;

        for (byte b : data) {
            builder.append(UsbUtil.toHexString(b));
            checksum += b;
        }

        return builder.append(UsbUtil.toHexString((byte) -(checksum & 0xff))).toString().toUpperCase();
    }

    public static IntelHexPacket parseLine(int lineNo, String line) throws IOException {
        if (line.length() < 9) {
            throw new IOException("line " + lineNo + ": the line must contain at least 9 characters.");
        }
        lineNo++;

        char startCode = line.charAt(0);
        int count = parseInt(line.substring(1, 3), 16);
        int address = parseInt(line.substring(3, 7), 16);
        int recordType = parseInt(line.substring(7, 9), 16);

        if (startCode != ':') {
            throw new IOException("line " + lineNo + ": The first character must be ':'.");
        }

        if (recordType == 0) {
            int expectedLineLength = 9 + count * 2 + 2;
            if (line.length() != expectedLineLength) {
                throw new IOException("line " + lineNo + ": Expected line to be " + expectedLineLength + " characters, was " + line.length() + ".");
            }

            byte data[] = new byte[count];

            int x = 9;
            for (int i = 0; i < count; i++) {
                data[i] = (byte) parseInt(line.substring(x, x + 2), 16);
                x += 2;
            }

            return new IntelHexPacket(lineNo, DATA, address, data);
        }

        throw new IOException("line " + lineNo + ": Unknown record type: 0x" + Long.toHexString(recordType) + ".");
    }
}
