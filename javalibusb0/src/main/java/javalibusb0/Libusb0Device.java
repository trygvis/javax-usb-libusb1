package javalibusb0;

import javax.usb.*;

public class Libusb0Device {
    public final String filename;
    public final UsbDeviceDescriptor deviceDescriptor;
//    public final DefaultUsbConfigDescriptor configDescriptor;

    public Libusb0Device(String filename, UsbDeviceDescriptor deviceDescriptor) {
        this.filename = filename;
        this.deviceDescriptor = deviceDescriptor;
    }
}
