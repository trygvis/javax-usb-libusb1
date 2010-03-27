package javax.usb;

import java.io.*;
import java.util.List;

public interface UsbConfiguration {
    boolean containsUsbInterface(byte number);

    String getConfigurationString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException;

    UsbConfigurationDescriptor getUsbConfigurationDescriptor();

    UsbDevice getUsbDevice();

    UsbInterface getUsbInterface(byte number);

    List<UsbInterface> getUsbInterfaces();

    boolean isActive();
}
