package javax.usb;

public interface UsbEndpointDescriptor extends UsbDescriptor {
    byte bEndpointAddress();

    byte bInterval();

    byte bmAttributes();

    short wMaxPacketSize();
}
