package javalibusb1.impl;

import javax.usb.*;
import java.util.List;

public class Libusb1UsbInterface implements UsbInterface {

    public final UsbConfiguration configuration;
    public final UsbInterfaceDescriptor usbInterfaceDescriptor;
    private boolean active;

    public Libusb1UsbInterface(UsbConfiguration configuration, UsbInterfaceDescriptor usbInterfaceDescriptor, boolean active) {
        this.configuration = configuration;
        this.usbInterfaceDescriptor = usbInterfaceDescriptor;
        this.active = active;
    }

    // -----------------------------------------------------------------------
    // UsbInterface Implementation
    // -----------------------------------------------------------------------

    // Should do set_configuration+claim_interface
    public void claim() {
        throw new RuntimeException("Not implemented");
    }

    public void claim(UsbInterfacePolicy policy) {
        throw new RuntimeException("Not implemented");
    }

    public boolean containsSetting(byte number) {
        throw new RuntimeException("Not implemented");
//        configuration.
//        for (UsbInterfaceDescriptor setting : settings) {
//            if (setting.bInterfaceClass() == number) {
//                return true;
//            }
//        }
//
//        return false;
    }

    public boolean containsUsbEndpoint(byte address) {
        throw new RuntimeException("Not implemented");
    }

    public UsbInterface getActiveSetting() {
        throw new RuntimeException("Not implemented");
    }

    public byte getActiveSettingNumber() {
        return getActiveSetting().getUsbInterfaceDescriptor().bAlternateSetting();
    }

    public String getInterfaceString() throws UsbException {
        return configuration.getUsbDevice().getString(usbInterfaceDescriptor.iInterface());
    }

    public int getNumSettings() {
        return getActiveSettingNumber();
    }

    public UsbInterface getSetting(byte number) {
        throw new RuntimeException("Not implemented");
    }

    public List<UsbInterfaceDescriptor> getSettings() {
        throw new RuntimeException("Not implemented");
//        return settings;
    }

    public UsbConfiguration getUsbConfiguration() {
        throw new RuntimeException("Not implemented");
    }

    public UsbEndpoint getUsbEndpoint(byte address) {
        throw new RuntimeException("Not implemented");
    }

    public List<UsbEndpoint> getUsbEndpoints() {
        throw new RuntimeException("Not implemented");
    }

    public UsbInterfaceDescriptor getUsbInterfaceDescriptor() {
        return usbInterfaceDescriptor;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isClaimed() {
        throw new RuntimeException("Not implemented");
    }

    public void release() {
        throw new RuntimeException("Not implemented");
    }
}
