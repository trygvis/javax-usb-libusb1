package javax.usb;

import java.io.*;
import java.util.*;

@SuppressWarnings({"DuplicateThrows"})
public interface UsbInterface {

    void claim() throws UsbClaimException, UsbException, UsbNotActiveException, UsbDisconnectedException;

    void claim(UsbInterfacePolicy policy) throws UsbClaimException, UsbException, UsbNotActiveException, UsbDisconnectedException;

    boolean containsSetting(byte number);

    boolean containsUsbEndpoint(byte address);

    UsbInterface getActiveSetting() throws UsbNotActiveException;

    byte getActiveSettingNumber() throws UsbNotActiveException;

    java.lang.String getInterfaceString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException;

    int getNumSettings();

    UsbInterface getSetting(byte number);

    List<UsbInterfaceDescriptor> getSettings();

    UsbConfiguration getUsbConfiguration();

    UsbEndpoint getUsbEndpoint(byte address);

    List<UsbEndpoint> getUsbEndpoints();

    UsbInterfaceDescriptor getUsbInterfaceDescriptor();

    boolean isActive();

    boolean isClaimed();

    void release() throws UsbClaimException, UsbException, UsbNotActiveException, UsbDisconnectedException;
}
