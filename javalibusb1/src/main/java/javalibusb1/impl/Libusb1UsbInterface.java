package javalibusb1.impl;

import javax.usb.*;
import java.util.Arrays;
import java.util.List;

public class Libusb1UsbInterface implements UsbInterface {

    public final Libusb1UsbConfiguration configuration;
    public final UsbInterfaceDescriptor descriptor;
    public final UsbEndpoint[] endpoints;
    private boolean active;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final int libusb_device;
    @SuppressWarnings({"UnusedDeclaration"})
    private int libusb_handle;

    public Libusb1UsbInterface(Libusb1UsbConfiguration configuration, UsbInterfaceDescriptor descriptor, UsbEndpoint[] endpoints, boolean active) {
        this.configuration = configuration;
        this.descriptor = descriptor;
        this.endpoints = endpoints;
        this.active = active;

        this.libusb_device = configuration.device.libusb_device;
    }

    // -----------------------------------------------------------------------
    // UsbInterface Implementation
    // -----------------------------------------------------------------------

    // Should do set_configuration+claim_interface
    public void claim() throws UsbException {
        nativeSetConfiguration(configuration.configurationDescriptor.bConfigurationValue());
        configuration.setActive(true);

        libusb_handle = nativeClaimInterface(descriptor.bInterfaceNumber());
    }

    public void claim(UsbInterfacePolicy policy) throws UsbException {
        claim();
    }

    public boolean containsSetting(byte number) {
        throw new RuntimeException("Not implemented");
    }

    public boolean containsUsbEndpoint(byte address) {
        for (UsbEndpoint endpoint : endpoints) {
            if (endpoint.getUsbEndpointDescriptor().bEndpointAddress() == address) {
                return true;
            }
        }

        return false;
    }

    public UsbInterface getActiveSetting() {
        throw new RuntimeException("Not implemented");
    }

    public byte getActiveSettingNumber() {
        return getActiveSetting().getUsbInterfaceDescriptor().bAlternateSetting();
    }

    public String getInterfaceString() throws UsbException {
        return configuration.getUsbDevice().getString(descriptor.iInterface());
    }

    public int getNumSettings() {
        throw new RuntimeException("Not implemented");
    }

    public UsbInterface getSetting(byte number) {
        throw new RuntimeException("Not implemented");
    }

    public List<UsbInterfaceDescriptor> getSettings() {
        throw new RuntimeException("Not implemented");
    }

    public UsbConfiguration getUsbConfiguration() {
        throw new RuntimeException("Not implemented");
    }

    public UsbEndpoint getUsbEndpoint(byte address) {
        for (UsbEndpoint endpoint : endpoints) {
            if (endpoint.getUsbEndpointDescriptor().bEndpointAddress() == address) {
                return endpoint;
            }
        }

        return null;
    }

    public List<UsbEndpoint> getUsbEndpoints() {
        return Arrays.asList(endpoints);
    }

    public UsbInterfaceDescriptor getUsbInterfaceDescriptor() {
        return descriptor;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isClaimed() {
        return libusb_handle != 0;
    }

    public void release() throws UsbException {
        try {
            nativeRelease(libusb_handle);
        } finally {
            libusb_handle = 0;
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    native
    private void nativeSetConfiguration(int configuration)
        throws UsbException;

    native
    private int nativeClaimInterface(int bInterfaceNumber);

    native
    private void nativeRelease(int libusb_handle)
        throws UsbException;
}
