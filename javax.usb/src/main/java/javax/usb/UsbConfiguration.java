package javax.usb;

import java.util.List;

public interface UsbConfiguration {
    boolean containsUsbInterface(byte number);

    String getConfigurationString() throws UsbException;

    UsbConfigurationDescriptor getUsbConfigurationDescriptor();

    UsbDevice getUsbDevice();

    UsbInterface getUsbInterface(byte number);

    List<UsbInterface> getUsbInterfaces();

    boolean isActive();
}
