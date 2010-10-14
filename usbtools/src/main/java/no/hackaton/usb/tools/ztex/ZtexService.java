package no.hackaton.usb.tools.ztex;

import static java.lang.Math.*;
import static org.apache.commons.io.FileUtils.*;

import javax.usb.*;
import java.io.*;

public class ZtexService {
    private final ZtexDevice device;

    public ZtexService(ZtexDevice device) {
        this.device = device;
    }


    public void loadBitStream(File file) throws UsbException, IOException {
        device.resetFpga();

        int chunkSize = 256 * 8;

        byte[] bytes = readFileToByteArray(file);

        int cs = 0;
        for (int i = 0; i < bytes.length; i++) {
            cs = (cs + (bytes[i] & 0xff)) & 0xff;
        }

        for (int offset = 0; offset < bytes.length; offset += chunkSize) {
            int size = min(bytes.length - offset, chunkSize);
            System.out.println("Sending chunk " + (offset / chunkSize) + " of " + (bytes.length / chunkSize) + ", size: " + size);
            device.sendFpgaData(bytes, offset, size);
        }

        System.out.println("Calculated checksum=" + cs);

        ZtexFpgaState fpgaState = device.getFpgaState();

        System.out.println("fpgaState.checksum = " + fpgaState.checksum);
    }
}
