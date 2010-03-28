package javax.usb.event;

import javax.usb.*;
import java.util.*;

public class UsbDeviceEvent extends EventObject {
    public UsbDeviceEvent(UsbDevice usbDevice) {
        super(usbDevice);
    }

    public UsbDevice getUsbDevice() {
        return (UsbDevice) getSource();
    }
}
