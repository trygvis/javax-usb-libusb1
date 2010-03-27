package javax.usb;

import java.util.*;

public interface UsbHub extends UsbDevice {
    boolean isRootUsbHub();

    List<UsbDevice> getAttachedUsbDevices();

    byte getNumberOfPorts();

    UsbPort getUsbPort(byte number);

    List<UsbPort> getUsbPorts();
}
