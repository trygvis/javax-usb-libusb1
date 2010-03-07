package javax.usb;

public interface UsbDeviceDescriptor extends UsbDescriptor {
    short bcdUSB();

    byte bDeviceClass();

    byte bDeviceSubClass();

    byte bDeviceProtocol();

    byte bMaxPacketSize0();

    short idVendor();

    short idProduct();

    short bcdDevice();

    byte iManufacturer();

    byte iProduct();

    byte iSerialNumber();

    byte bNumConfigurations();
}
