package javax.usb.event;

import java.util.*;

public interface UsbServicesListener extends EventListener {
    void usbDeviceAttached(UsbServicesEvent event);

    void usbDeviceDetached(UsbServicesEvent event);
}
