package io.trygvis.usb.tools.ztex;

import io.trygvis.usb.tools.*;

import javax.usb.*;
import static javax.usb.util.UsbUtil.*;
import java.io.*;

public class ZtexLoadBitStream {
    public static void main(String[] args) throws Exception {
//        System.setProperty("javax.usb.libusb.trace", "true");
//        System.setProperty("javax.usb.libusb.debug", "3");

        UsbServices usbServices = UsbHostManager.getUsbServices();

        UsbDevice usbDevice = UsbCliUtil.findDevice(usbServices.getRootUsbHub(), (short) 0x221a, (short) 0x0100, null);

        if (usbDevice == null) {
            return;
        }

        ZtexDevice ztexDevice = ZtexDevice.createZtexDevice(usbDevice);

        System.out.println("device: " + ztexDevice.deviceDescriptor);

        /*
        ZtexFpgaState state = ztexDevice.getFpgaState();
        System.out.println("FPGA state:");
        System.out.println("  Configured: " + state.configured);
        System.out.println("  Checksum: 0x" + toHexString(state.checksum));
        System.out.println("  Bytes transferred: " + state.bytesTransferred);
        System.out.println("  INIT_B: " + state.initB);
        */

        System.out.println("Resetting...");
        ztexDevice.resetFpga();

        /*
        state = ztexDevice.getFpgaState();
        System.out.println("FPGA state:");
        System.out.println("  Configured: " + state.configured);
        System.out.println("  Checksum: 0x" + toHexString(state.checksum));
        System.out.println("  Bytes transferred: " + state.bytesTransferred);
        System.out.println("  INIT_B: " + state.initB);
        */

        if (args.length > 0) {
            File file = new File(args[0]);

            if (!file.canRead()) {
                System.err.println("Can't read " + args[0] + ".");
                return;
            }

            boolean success = loadFile(ztexDevice, file);
            System.exit(success ? 0 : 1);
        }
        System.exit(1);
    }

    public static boolean loadFile(ZtexDevice ztexDevice, File file) throws Exception {
        System.out.println("Uploading " + file + "...");
        byte checksum = ZtexService.loadBitStream(ztexDevice, file);

        ZtexFpgaState state = ztexDevice.getFpgaState();

        if (checksum == state.checksum) {
            System.out.println("Bitstream uploaded successfully");
            return true;
        }

        System.err.println("Checksum mismatch!");
        System.out.println("Host checksum: 0x" + toHexString(checksum));
        System.out.println("FPGA checksum: 0x" + toHexString(state.checksum));

        System.out.println("FPGA state:");
        System.out.println("  Configured: " + state.configured);
        System.out.println("  Checksum: 0x" + toHexString(state.checksum));
        System.out.println("  Bytes transferred: " + state.bytesTransferred);
        System.out.println("  INIT_B: " + state.initB);

        return false;
    }
}
