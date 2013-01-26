package io.trygvis.usb.tools;

import static java.lang.Integer.*;
import static javax.usb.extra.ExtraUsbUtil.*;
import static javax.usb.util.UsbUtil.toHexString;

import javax.usb.*;
import java.util.*;

/**
 * Utilities for writing command line USB tools.
 */
public class UsbCliUtil {

    /**
     * Tries to find a device with either the id given or the vendor/product id given.
     * <p/>
     * This method will use stdout/stderr to print error messages so no messages should be
     *
     * @return Returns null if no device could be found.
     */
    public static UsbDevice findDevice(UsbHub hub, Short idVendor, Short idProduct, String id) {
        // TODO: Parse out --id=VVVV.PPPP[.N] and --device=[device path]

        if (id != null) {
            int i = id.indexOf(':');
            if (i == 4) {
                // this is an id on the form VVVV.PPPP
                idVendor = (short) parseInt(id.substring(0, 4), 16);
                idProduct = (short) parseInt(id.substring(5, 9), 16);

                id = null;
            }
        }

        if (id != null) {
            // As this library is implemented with libusb which returns
            // everything in a single, flat list for now just do this simple search.

            int index;
            try {
                index = parseInt(id);
            } catch (NumberFormatException e) {
                System.err.println("Invalid 'id' parameter, has to be an integer.");
                return null;
            }

            List<UsbDevice> devices = hub.getAttachedUsbDevices();

            if (index >= devices.size() || index < 0) {
                System.err.println("'id' parameter is out of range.");
                return null;
            }

            return devices.get(index);
        } else if (idProduct != null && idVendor != null) {
            UsbDevice usbDevice = findUsbDevice(hub, idVendor, idProduct);

            if (usbDevice == null) {
                System.err.println("Could not find device with id " + deviceIdToString(idVendor, idProduct) + ".");
            }

            return usbDevice;
        }
        return null;
    }

    public static void listDevices(UsbHub hub, short idVendor, short idProduct) {
        listDevices("0", hub, idVendor, idProduct);
    }

    private static void listDevices(String prefix, UsbHub hub, short idVendor, short idProduct) {
        List<UsbDevice> list = hub.getAttachedUsbDevices();
        for (int i = 0; i < list.size(); i++) {
            UsbDevice device = list.get(i);
            if (device.isUsbHub()) {
                listDevices(prefix + "." + i, (UsbHub) device, idVendor, idProduct);
            } else {
                UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();

                System.err.print(prefix + "." + i + " " + toHexString(deviceDescriptor.idVendor()) + ":" + toHexString(deviceDescriptor.idProduct()));

                if (isUsbDevice(deviceDescriptor, idVendor, idProduct)) {
                    System.err.print(" (Unconfigured FX2)");
                }
                System.err.println();
            }
        }
    }
}
