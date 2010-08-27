package javax.usb.extra;

import javax.usb.*;

public class ExtraUsbUtil {
    public static UsbDevice findDevice(UsbHub usbHub, short idVendor, short idProduct) {
        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                continue;
            }

            UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();

            if (deviceDescriptor.idVendor() == idVendor && deviceDescriptor.idProduct() == idProduct) {
                return device;
            }
        }

        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (!device.isUsbHub()) {
                continue;
            }

            UsbDevice foundDevice = findDevice((UsbHub) device, idVendor, idProduct);

            if (foundDevice != null) {
                return foundDevice;
            }
        }

        return null;
    }
}
