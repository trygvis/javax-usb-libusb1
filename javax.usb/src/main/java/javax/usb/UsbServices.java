package javax.usb;

import javax.usb.event.*;

public interface UsbServices {
    String getApiVersion();

    String getImpDescription();

    String getImpVersion();

    UsbHub getRootUsbHub() throws UsbException, SecurityException;

    void addUsbServicesListener(UsbServicesListener listener);

    void removeUsbServicesListener(UsbServicesListener listener);
}
