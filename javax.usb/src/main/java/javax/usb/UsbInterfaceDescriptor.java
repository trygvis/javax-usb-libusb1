package javax.usb;

public interface UsbInterfaceDescriptor {
    byte bAlternateSetting();

    byte bInterfaceClass();

    byte bInterfaceNumber();

    byte bInterfaceProtocol();

    byte bInterfaceSubClass();

    byte bNumEndpoints();

    byte iInterface();
}
