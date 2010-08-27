package no.hackaton.usbtools;

import javalibusb1.*;
import static no.hackaton.usbtools.Fx2Device.*;

import javax.usb.*;
import java.io.*;

public class Fx2Programmer {
    public static void main(String[] args) throws UsbException {
        System.setProperty(Libusb1UsbServices.JAVAX_USB_LIBUSB_TRACE_PROPERTY, "true");
        UsbServices usbServices = UsbHostManager.getUsbServices();

        Fx2Device device = findFx2Device(usbServices.getRootUsbHub());

        if (device == null) {
            System.err.println("No FX2 attached.");
            return;
        }

        try {
            device.setReset();

            byte[] data = device.read((short)0xe000, 16);
            HexFormatter.writeBytes(System.out, data);

            data = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
            device.write((short)0xe000, data);

            data = device.read((short)0xe000, 16);
            HexFormatter.writeBytes(System.out, data);

            device.releaseReset();
        } finally {
            try {
                device.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
