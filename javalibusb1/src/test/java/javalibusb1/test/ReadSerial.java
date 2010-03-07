package javalibusb1.test;

import javax.usb.*;
import javax.usb.util.UsbUtil;
import java.util.List;

public class ReadSerial {
    public static void main(String[] args) throws UsbException {
        UsbServices usbServices = UsbHostManager.getUsbServices();

        UsbDevice device = findDeviceByVendorAndProduct(usbServices.getRootUsbHub(), (short) 0x067b, (short) 0x2303);

        if (device == null) {
            System.err.println("Unable to find FTDI device.");
            return;
        }

        System.out.println("Found device: Manufacturer: " + device.getManufacturerString());
        System.out.println("Found device: Product: " + device.getProductString());

        UsbConfiguration configuration = device.getActiveUsbConfiguration();

        UsbInterface usbInterface = configuration.getUsbInterface((byte)0);

        usbInterface.claim();

        UsbEndpoint inEndpoint = usbInterface.getUsbEndpoint((byte) 0x02);
        UsbPipe inPipe = inEndpoint.getUsbPipe();

        UsbEndpoint outEndpoint = usbInterface.getUsbEndpoint((byte) 0x81);
        UsbPipe outPipe = outEndpoint.getUsbPipe();

        inPipe.close();
        outPipe.close();
    }

    public static UsbDevice findDeviceByVendorAndProduct(UsbHub usbHub, short idVendor, short idProduct) {

        List<UsbDevice> devices = usbHub.getAttachedUsbDevices();
        for (UsbDevice device : devices) {
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();

            if (descriptor.idVendor() == idVendor && descriptor.idProduct() == idProduct) {
                return device;
            }

            if (device.isUsbHub()) {
                UsbDevice d = findDeviceByVendorAndProduct((UsbHub) device, idVendor, idProduct);
                if (d != null) {
                    return d;
                }
            }
        }

        return null;
    }
}
