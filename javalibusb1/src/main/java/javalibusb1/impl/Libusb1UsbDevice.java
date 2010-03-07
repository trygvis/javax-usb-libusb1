package javalibusb1.impl;

import javax.usb.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Subclass and use a Libusb1UsbHub if this is a hub.
 * TODO: Consider pre-loading all the configurations, or getting them injected.
 */
public class Libusb1UsbDevice implements UsbDevice, Closeable {

    /**
     * Used by the native code
     */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final int libusb_device;
    public final byte busNumber;
    public final byte deviceAddress;
    private final UsbDeviceDescriptor usbDeviceDescriptor;

    public Libusb1UsbDevice(int libusb_device_, byte busNumber, byte deviceAddress, UsbDeviceDescriptor usbDeviceDescriptor) {
        this.libusb_device = libusb_device_;
        this.busNumber = busNumber;
        this.deviceAddress = deviceAddress;
        this.usbDeviceDescriptor = usbDeviceDescriptor;
    }

    public String getManufacturerString() throws UsbException {
        return getString(usbDeviceDescriptor.iManufacturer());
    }

    public String getSerialNumberString() throws UsbException {
        return getString(usbDeviceDescriptor.iSerialNumber());
    }

    public String getProductString() throws UsbException {
        return getString(usbDeviceDescriptor.iProduct());
    }

    public UsbStringDescriptor getUsbStringDescriptor(byte index) {
        return null;
    }

    public String getString(byte index) throws UsbException {
        String s = getStringNative(index, 1024);

        // It would be nice to have a way to know if the device had disconnected here and
        // throw a UsbDisconnectedException() instead.

//        if(s == null) {
//            throw new UsbException("Unable to get string #" + unsignedInt(index));
//        }

        return s;
    }

    public UsbDeviceDescriptor getUsbDeviceDescriptor() {
        return usbDeviceDescriptor;
    }

    public boolean isUsbHub() {
        return false;
    }

    public UsbConfiguration getActiveUsbConfiguration() throws UsbPlatformException {
        return nativeGetActiveUsbConfiguration();
    }

    public boolean containsUsbConfiguration(byte number) throws UsbPlatformException {
        return nativeGetUsbConfiguration(number) != null;
    }

    public UsbConfiguration getUsbConfiguration(byte number) throws UsbPlatformException {
        return nativeGetUsbConfiguration(number);
    }

    public List<UsbConfiguration> getUsbConfigurations() throws UsbPlatformException {
        byte n = getUsbDeviceDescriptor().bNumConfigurations();
        List<UsbConfiguration> list = new ArrayList<UsbConfiguration>(n);
        for (byte i = 0; i < n; i++) {
            list.add(getUsbConfiguration(i));
        }
        return list;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void close() throws IOException {
        closeNative();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    // -----------------------------------------------------------------------
    // Native Interface
    // -----------------------------------------------------------------------

    native
    public void closeNative();

    /**
     * Returns null on failure.
     */
    native
    private String getStringNative(byte index, int length)
        throws UsbPlatformException;

    native
    private UsbConfiguration nativeGetActiveUsbConfiguration()
        throws UsbPlatformException;

    native
    private UsbConfiguration nativeGetUsbConfiguration(byte number)
        throws UsbPlatformException;
}
