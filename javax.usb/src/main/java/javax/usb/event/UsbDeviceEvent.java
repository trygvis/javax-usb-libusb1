package javax.usb.event;

import javax.usb.*;
import java.util.*;

public class UsbDeviceEvent extends EventObject {
    private final UsbDevice usbDevice;

    public UsbDeviceEvent(UsbDevice usbDevice) {
        super(usbDevice);
        this.usbDevice = usbDevice;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }
}
