package javax.usb.event;

import java.util.*;

public interface UsbDeviceListener extends EventListener {
    void usbDeviceDetached(UsbDeviceEvent event);

    void errorEventOccurred(UsbDeviceErrorEvent event);

    void dataEventOccurred(UsbDeviceDataEvent event);
}
