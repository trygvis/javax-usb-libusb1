package no.hackaton.usb.tools.ztex;

import no.hackaton.usb.tools.*;

import javax.usb.*;
import java.io.*;

public class ZtexLoadBitStream {
    public static void main(String[] args) throws UsbException, IOException {
//        System.setProperty("javax.usb.libusb.trace", "true");
//        System.setProperty("javax.usb.libusb.debug", "3");

        UsbServices usbServices = UsbHostManager.getUsbServices();

        UsbDevice usbDevice = UsbCliUtil.findDevice(usbServices.getRootUsbHub(), (short) 0x221a, (short) 0x0100, null);

        if (usbDevice == null) {
            return;
        }

        ZtexDevice ztexDevice = ZtexDevice.createZtexDevice(usbDevice);

        System.out.println("device: " + ztexDevice.deviceDescriptor);

        System.out.println("fpgaState = " + ztexDevice.getFpgaState());

        System.out.println("Resetting...");
        ztexDevice.resetFpga();

        System.out.println("fpgaState = " + ztexDevice.getFpgaState());

        if (args.length > 0) {
            File file = new File(args[0]);

            if (!file.canRead()) {
                System.err.println("Can't read " + args[0] + ".");
                return;
            }

            ZtexService service = new ZtexService(ztexDevice);

            System.out.println("Uploading " + file + "...");
            service.loadBitStream(file);
            System.out.println("Bitstream uploaded");
        }

        System.out.println("fpgaState = " + ztexDevice.getFpgaState());
    }
}
