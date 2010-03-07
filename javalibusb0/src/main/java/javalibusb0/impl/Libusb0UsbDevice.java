package javalibusb0.impl;

import javalibusb0.*;

import javax.usb.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Libusb0UsbDevice implements UsbDevice {
    public final Libusb0Device libusb0Device;

    public Libusb0UsbDevice(Libusb0Device libusb0Device) {
        this.libusb0Device = libusb0Device;
    }

    public String getManufacturerString() {
        throw new RuntimeException("Not implemented");
    }

    public String getSerialNumberString() {
        throw new RuntimeException("Not implemented");
    }

    public String getProductString() {
        throw new RuntimeException("Not implemented");
    }

    public UsbStringDescriptor getUsbStringDescriptor(byte index) {
        throw new RuntimeException("Not implemented");
    }

    public String getString(byte index) {
        throw new RuntimeException("Not implemented");
    }

    public UsbDeviceDescriptor getUsbDeviceDescriptor() {
        throw new RuntimeException("Not implemented");
    }
}
