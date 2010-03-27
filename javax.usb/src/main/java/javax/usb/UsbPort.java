package javax.usb;

public interface UsbPort {

    byte getPortNumber();

    UsbHub getUsbHub();

    UsbDevice getUsbDevice();

    boolean isUsbDeviceAttached();
}
