package io.trygvis.usb.ftdi;

import javax.usb.*;

public class Ftdi232Util {
    public static int calculateBaudRate(int requestedBaudRate) {
        int base = 3000000 / requestedBaudRate;

        return base << 16;
    }

    public static UsbDevice findDevice(UsbHub usbHub) {
        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                device = findDevice((UsbHub) device);

                if (device != null) {
                    return device;
                }
            } else {
                UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();
                if (deviceDescriptor.idVendor() == 0x0403 && deviceDescriptor.idProduct() == 0x6001) {
                    return device;
                }
            }
        }

        return null;
    }
}
