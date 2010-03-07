package javax.usb;

public interface UsbControlIrp extends UsbIrp {
    byte bmRequestType();

    byte bRequest();

    short wIndex();

    short wValue();
}
