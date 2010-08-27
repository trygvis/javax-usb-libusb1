package javalibusb1;

import javax.usb.*;
import javax.usb.impl.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Libusb1UsbInterface implements UsbInterface {

    public final Libusb1UsbDevice device;
    public final Libusb1UsbConfiguration configuration;
    public final UsbInterfaceDescriptor descriptor;
    public final UsbEndpoint endpointZero;
    public final UsbEndpoint[] endpoints;
    private boolean active;

    /**
     * This holds the value of the <code>struct libusb_device_handle*</code> if this interface is claimed.
     */
    long libusb_device_handle_ptr;
    private static final UsbEndpointDescriptor ENDPOINT_ZERO_DESCRIPTOR = new DefaultUsbEndpointDescriptor((byte)0, (byte)0, (byte)0, (byte)0);

    public Libusb1UsbInterface(Libusb1UsbConfiguration configuration, UsbInterfaceDescriptor descriptor, UsbEndpoint[] endpoints, boolean active) {
        this.device = configuration.device;
        this.configuration = configuration;
        this.descriptor = descriptor;
        this.endpoints = endpoints;
        this.active = active;

        endpointZero = new Libusb1UsbEndpoint(this, ENDPOINT_ZERO_DESCRIPTOR);
    }

    // -----------------------------------------------------------------------
    // UsbInterface Implementation
    // -----------------------------------------------------------------------

    // Should do set_configuration+claim_interface
    public void claim() throws UsbException {
        nativeSetConfiguration(device.libusb_device_ptr, configuration.configurationDescriptor.bConfigurationValue());
        configuration.setActive(true);

        libusb_device_handle_ptr = nativeClaimInterface(device.libusb_device_ptr, descriptor.bInterfaceNumber());
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

    public String getInterfaceString() throws UsbException, UnsupportedEncodingException {
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
        return configuration;
    }

    public UsbEndpoint getUsbEndpoint(byte address) {
        // I'm not sure if one should check the endpoints list for endpoint 0x00, but I've
        // never seen it in the array so I assume not - trygve
        if (address == 0x00) {
            return endpointZero;
        }

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
        return libusb_device_handle_ptr != 0;
    }

    public void release() throws UsbException {
        try {
            nativeRelease(libusb_device_handle_ptr);
        } finally {
            libusb_device_handle_ptr = 0;
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    native
    private void nativeSetConfiguration(long libusb_device_ptr, int configuration)
        throws UsbException;

    native
    private long nativeClaimInterface(long libusb_device_ptr, int bInterfaceNumber);

    native
    private void nativeRelease(long libusb_device_handle_ptr)
        throws UsbException;
}
