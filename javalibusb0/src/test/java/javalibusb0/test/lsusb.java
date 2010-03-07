package javalibusb0.test;

import javax.usb.*;

public class lsusb {
    public static void main(String[] args) throws UsbException {
        UsbServices usbServices = UsbHostManager.getUsbServices();

        dump(usbServices.getRootUsbHub());
    }

    private static void dump(UsbHub usbHub) {
        System.out.println("Usb hub");
        System.out.println(" Product: " + usbHub.getProductString());
        System.out.println(" Manufacturer: " + usbHub.getManufacturerString());
        System.out.println(" Serial: " + usbHub.getSerialNumberString());

        for (UsbDevice usbDevice : usbHub.getAttachedUsbDevices()) {
            dump(usbDevice);
        }
    }

    private static void dump(UsbDevice usbDevice) {
        System.out.println("usbDevice = " + usbDevice);
        System.out.println("usbDevice.getManufacturerString() = " + usbDevice.getManufacturerString());
    }
}
