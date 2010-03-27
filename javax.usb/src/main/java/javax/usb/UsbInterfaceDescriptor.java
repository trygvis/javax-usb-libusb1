package javax.usb;

public interface UsbInterfaceDescriptor extends UsbDescriptor {
    byte bAlternateSetting();

    byte bInterfaceClass();

    byte bInterfaceNumber();

    byte bInterfaceProtocol();

    byte bInterfaceSubClass();

    byte bNumEndpoints();

    byte iInterface();
}
