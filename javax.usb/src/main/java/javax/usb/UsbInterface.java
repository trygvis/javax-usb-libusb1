package javax.usb;

import java.util.List;

public interface UsbInterface {

    void claim();

    void claim(UsbInterfacePolicy policy);

    boolean containsSetting(byte number);

    boolean containsUsbEndpoint(byte address);

    UsbInterface getActiveSetting();

    byte getActiveSettingNumber();

    java.lang.String getInterfaceString() throws UsbException;

    int getNumSettings();

    UsbInterface getSetting(byte number);

    List<UsbInterfaceDescriptor> getSettings();

    UsbConfiguration getUsbConfiguration();

    UsbEndpoint getUsbEndpoint(byte address);

    List<UsbEndpoint> getUsbEndpoints();

    UsbInterfaceDescriptor getUsbInterfaceDescriptor();

    boolean isActive();

    boolean isClaimed();

    void release();
}
