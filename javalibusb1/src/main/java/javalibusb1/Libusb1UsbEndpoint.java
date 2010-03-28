package javalibusb1;

import javax.usb.*;

import static javax.usb.UsbConst.ENDPOINT_DIRECTION_MASK;
import static javax.usb.UsbConst.ENDPOINT_TYPE_MASK;

public class Libusb1UsbEndpoint implements UsbEndpoint {
    public final Libusb1UsbInterface usbInterface;
    public final UsbEndpointDescriptor descriptor;

    public Libusb1UsbEndpoint(Libusb1UsbInterface usbInterface, UsbEndpointDescriptor descriptor) {
        this.usbInterface = usbInterface;
        this.descriptor = descriptor;
    }

    // -----------------------------------------------------------------------
    // UsbEndpoint Implementation
    // -----------------------------------------------------------------------

    public byte getDirection() {
        return (byte) (descriptor.bEndpointAddress() & ENDPOINT_DIRECTION_MASK);
    }

    public byte getType() {
        return (byte) (descriptor.bmAttributes() & ENDPOINT_TYPE_MASK);
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
