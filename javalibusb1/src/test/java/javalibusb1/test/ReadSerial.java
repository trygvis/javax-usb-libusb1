package javalibusb1.test;

import javax.usb.*;
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

        UsbInterface usbInterface = configuration.getUsbInterface((byte) 0);

        usbInterface.claim();

        UsbEndpoint inEndpoint = usbInterface.getUsbEndpoint((byte) 0x02);
        UsbPipe inPipe = inEndpoint.getUsbPipe();

        UsbEndpoint outEndpoint = usbInterface.getUsbEndpoint((byte) 0x81);
        UsbPipe outPipe = outEndpoint.getUsbPipe();

        byte outRequestType = UsbConst.REQUESTTYPE_TYPE_VENDOR | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE | UsbConst.REQUESTTYPE_DIRECTION_OUT;
        byte inRequestType = UsbConst.REQUESTTYPE_TYPE_VENDOR | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE | UsbConst.REQUESTTYPE_DIRECTION_IN;

        // Set baud rate. See AN232B-05_BaudRates.pdf, page 7.
        byte value = 0x38;
        byte index = 0x41;
        byte SIO_SET_BAUD_RATE = 3;
        byte bRequest = SIO_SET_BAUD_RATE;

        // b2400 = 3
        value = 3;
        index = 0;

        UsbControlIrp irp = outPipe.createUsbControlIrp(outRequestType, bRequest, value, index);

        System.out.println("Sending IRP");
        outPipe.syncSubmit(irp);
        System.out.println("IRP sent");

//        libusb_control_transfer (libusb_device_handle *dev_handle, uint8_t bmRequestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, unsigned char *data, uint16_t wLength, unsigned int timeout)

//libusb_control_transfer(ftdi->usb_dev, outRequestType, SIO_SET_BAUDRATE_REQUEST, value,index, NULL, 0, ftdi->usb_write_timeout)
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
