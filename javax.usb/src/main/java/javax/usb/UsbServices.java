package javax.usb;

public interface UsbServices {
    String getApiVersion();

    String getImplDescription();

    String getImplVersion();

    UsbHub getRootUsbHub();
}
