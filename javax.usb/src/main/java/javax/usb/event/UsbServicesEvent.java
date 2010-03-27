package javax.usb.event;

import javax.usb.*;
import java.util.*;

public class UsbServicesEvent extends EventObject {
    private final UsbServices usbServices;
    private final UsbDevice usbDevice;

    public UsbServicesEvent(UsbServices usbServices, UsbDevice usbDevice) {
        super(usbServices);
        this.usbServices = usbServices;
        this.usbDevice = usbDevice;
    }

    public UsbServices getUsbServices() {
        return usbServices;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }
}
