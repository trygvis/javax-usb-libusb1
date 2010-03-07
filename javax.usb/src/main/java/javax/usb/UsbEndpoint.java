package javax.usb;

public interface UsbEndpoint {
    byte getDirection();

    byte getType();

    UsbEndpointDescriptor getUsbEndpointDescriptor();

    UsbInterface getUsbInterface();

    UsbPipe getUsbPipe();
}
