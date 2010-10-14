package javax.usb.extra;

import static javax.usb.util.UsbUtil.toHexString;

import javax.usb.*;
import java.util.*;

public class ExtraUsbUtil {
    /**
     * @param a Least significant byte
     * @param b Most significant byte
     */
    public static short toShort(byte a, byte b) {
        return (short)(b << 8 | a);
    }

    /**
     * @param a Least significant byte
     * @param b
     * @param c
     * @param d Most significant byte
     * @return
     */
    public static int toInt(byte a, byte b, byte c, byte d) {
        return d << 24 | c << 16 | b << 8 | a;
    }

    public static boolean isUsbDevice(UsbDeviceDescriptor descriptor, short idVendor, short idProduct) {
        return descriptor.idVendor() == idVendor && descriptor.idProduct() == idProduct;
    }

    public static UsbDevice findUsbDevice(UsbHub usbHub, short idVendor, short idProduct) {
        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                continue;
            }

            UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();

            if (isUsbDevice(deviceDescriptor, idVendor, idProduct)) {
                return device;
            }
        }

        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (!device.isUsbHub()) {
                continue;
            }

            UsbDevice foundDevice = findUsbDevice((UsbHub) device, idVendor, idProduct);

            if (foundDevice != null) {
                return foundDevice;
            }
        }

        return null;
    }

    public static List<UsbDevice> findUsbDevices(UsbHub usbHub, short idVendor, short idProduct) {
        List<UsbDevice> devices = new ArrayList<UsbDevice>();

        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                continue;
            }

            UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();

            if (deviceDescriptor.idVendor() == idVendor && deviceDescriptor.idProduct() == idProduct) {
                devices.add(device);
            }
        }

        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (!device.isUsbHub()) {
                continue;
            }

            List<UsbDevice> foundDevices = findUsbDevices((UsbHub) device, idVendor, idProduct);

            devices.addAll(foundDevices);
        }

        return devices;
    }

    public static List<UsbDevice> listUsbDevices(UsbHub usbHub) {
        List<UsbDevice> devices = new ArrayList<UsbDevice>();

        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                continue;
            }

            devices.add(device);
        }

        for (UsbDevice device : usbHub.getAttachedUsbDevices()) {
            if (!device.isUsbHub()) {
                continue;
            }

            devices.addAll(listUsbDevices(usbHub));
        }

        return devices;
    }

    public static String deviceIdToString(UsbDeviceDescriptor descriptor) {
        return toHexString(descriptor.idVendor()) + ":" + toHexString(descriptor.idProduct());
    }

    public static String deviceIdToString(short idVendor, short idProduct) {
        return toHexString(idVendor) + ":" + toHexString(idProduct);
    }
}
