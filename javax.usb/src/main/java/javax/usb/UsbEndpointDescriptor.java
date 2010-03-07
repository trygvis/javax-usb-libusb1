package javax.usb;

public interface UsbEndpointDescriptor {
    byte bEndpointAddress();

    byte bInterval();

    byte bmAttributes();

    short wMaxPacketSize();
}
