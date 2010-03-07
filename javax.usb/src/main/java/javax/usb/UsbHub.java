package javax.usb;

import java.util.*;

public interface UsbHub extends UsbDevice {
    boolean isRootHub();

    List<UsbDevice> getAttachedUsbDevices();

//    byte getNumberOfPorts();
//
//    UsbPort getUsbPort(byte number);
//
//    List<UsbPort> getUsbPorts();
}
