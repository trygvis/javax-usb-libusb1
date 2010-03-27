package javax.usb;

import javax.usb.event.*;
import java.util.*;

public interface UsbDevice {
    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    String getManufacturerString() throws UsbException, java.io.UnsupportedEncodingException, UsbDisconnectedException;

    String getSerialNumberString() throws UsbException, java.io.UnsupportedEncodingException, UsbDisconnectedException;

    String getProductString() throws UsbException, java.io.UnsupportedEncodingException, UsbDisconnectedException;

    UsbStringDescriptor getUsbStringDescriptor(byte index);

    String getString(byte index) throws UsbException, UsbDisconnectedException;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    UsbDeviceDescriptor getUsbDeviceDescriptor();

    boolean isUsbHub();

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    void addUsbDeviceListener(UsbDeviceListener listener);

    void removeUsbDeviceListener(UsbDeviceListener listener);

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex);

    void asyncSubmit(List<UsbControlIrp> list) throws UsbException, IllegalArgumentException, UsbDisconnectedException;

    void asyncSubmit(UsbControlIrp irp) throws UsbException, IllegalArgumentException, UsbDisconnectedException;

    void syncSubmit(List<UsbControlIrp> list) throws UsbException, IllegalArgumentException, UsbDisconnectedException;

    void syncSubmit(UsbControlIrp irp) throws UsbException, IllegalArgumentException, UsbDisconnectedException;

    UsbPort getParentUsbPort() throws UsbDisconnectedException;

    UsbConfiguration getActiveUsbConfiguration() throws UsbPlatformException;

    boolean containsUsbConfiguration(byte number) throws UsbPlatformException;

    UsbConfiguration getUsbConfiguration(byte number) throws UsbPlatformException;

    List<UsbConfiguration> getUsbConfigurations() throws UsbPlatformException;
}
