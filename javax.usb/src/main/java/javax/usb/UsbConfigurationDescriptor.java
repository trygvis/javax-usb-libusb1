package javax.usb;

public interface UsbConfigurationDescriptor extends UsbDescriptor {
    byte bConfigurationValue();

    byte bmAttributes();

    byte bMaxPower();

    byte bNumInterfaces();

    byte iConfiguration();

    short wTotalLength();
}
