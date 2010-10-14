package no.hackaton.usb.tools.ztex;

import static javax.usb.extra.ExtraUsbUtil.*;
import static javax.usb.util.UsbUtil.*;

import javax.usb.util.*;

public class ZtexFpgaState {
    public final boolean configured;
    public final byte checksum;
    public final int bytesTransferred;
    public final int initB;

    public ZtexFpgaState(boolean configured, byte checksum, int bytesTransferred, int initB) {
        this.configured = configured;
        this.checksum = checksum;
        this.bytesTransferred = bytesTransferred;
        this.initB = initB;
    }

    public static ZtexFpgaState ztexFpgaStateFromBytes(byte[] bytes) {
        if(bytes.length < 7) {
            throw new RuntimeException("Invalid FPGA state descriptor, expected at least 7 bytes, got " + bytes.length + " bytes.");
        }
        return new ZtexFpgaState(bytes[0] == 0,
            bytes[1],
            toInt(bytes[2], bytes[3], bytes[4], bytes[5]),
            UsbUtil.unsignedInt(bytes[6]));
    }

    public String toString() {
        if(!configured) {
            return "Configured: no, checksum=" + toHexString(checksum) + ", bytesTransferred=" + bytesTransferred + ", initB=" + initB + ".";
        }
        return "Configured: yes, checksum=" + toHexString(checksum) + ", bytesTransferred=" + bytesTransferred + ", initB=" + initB + ".";
    }
}
