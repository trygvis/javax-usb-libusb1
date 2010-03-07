package javalibusb1.impl;

import javax.usb.UsbEndpoint;
import javax.usb.UsbEndpointDescriptor;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;

import static javax.usb.UsbConst.ENDPOINT_DIRECTION_MASK;

public class Libusb1UsbEndpoint implements UsbEndpoint {
    public final Libusb1UsbInterface usbInterface;
    public final byte type;
    public final UsbEndpointDescriptor descriptor;

    public Libusb1UsbEndpoint(Libusb1UsbInterface usbInterface, byte type, UsbEndpointDescriptor descriptor) {
        this.usbInterface = usbInterface;
        this.type = type;
        this.descriptor = descriptor;
    }

    // -----------------------------------------------------------------------
    // UsbEndpoint Implementation
    // -----------------------------------------------------------------------

    public byte getDirection() {
        return (byte) (descriptor.bEndpointAddress() & ENDPOINT_DIRECTION_MASK);
    }

    public byte getType() {
        return type;
    }

    public UsbEndpointDescriptor getUsbEndpointDescriptor() {
        return descriptor;
    }

    public UsbInterface getUsbInterface() {
        return usbInterface;
    }

    public UsbPipe getUsbPipe() {
        return new Libusb1UsbPipe(this);
    }
}
