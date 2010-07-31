package javalibusb1;

import javax.usb.*;
import javax.usb.event.*;
import javax.usb.util.*;
import java.io.*;
import java.util.*;

/**
 * TODO: Subclass and use a Libusb1UsbHub if this is a hub.
 * TODO: Consider pre-loading all the configurations, or getting them injected.
 */
public class Libusb1UsbDevice implements UsbDevice, Closeable {

    /**
     * Used by the native code
     */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    final long libusb_device;
    public final byte busNumber;
    public final byte deviceAddress;
    public final Object speed;
    private final UsbDeviceDescriptor usbDeviceDescriptor;

    private final List<UsbDeviceListener> deviceListeners = new ArrayList<UsbDeviceListener>();

    /**
     * Holds all the current configurations. Will be injected from the JNI code.
     */
    private UsbConfiguration[] configurations = new UsbConfiguration[256];
    private byte activeConfiguration;

    private boolean closed;

    public Libusb1UsbDevice(int libusb_device_, byte busNumber, byte deviceAddress, int speed, UsbDeviceDescriptor usbDeviceDescriptor) {
        this.libusb_device = libusb_device_;
        this.busNumber = busNumber;
        this.deviceAddress = deviceAddress;
        switch (speed) {
            case 1:
                this.speed = UsbConst.DEVICE_SPEED_LOW;
                break;
            case 2:
                this.speed = UsbConst.DEVICE_SPEED_FULL;
                break;
            case 3:
                this.speed = UsbConst.DEVICE_SPEED_HIGH;
                break;
            default:
                this.speed = UsbConst.DEVICE_SPEED_UNKNOWN;
        }
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
        System.out.println("libusb_device = " + Long.toHexString(libusb_device));
        String s = nativeGetString(libusb_device, index, 1024);

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

    public Object getSpeed() {
        return speed;
    }

    public boolean isConfigured() {
        // TODO: figure out how to determine if the device is configured or not.
        // From what I can read from the libusb documentation a device always have to have
        // a configuration set.
        return true;
    }

    public boolean isUsbHub() {
        return false;
    }

    public void addUsbDeviceListener(UsbDeviceListener listener) {
        deviceListeners.add(listener);
    }

    public void removeUsbDeviceListener(UsbDeviceListener listener) {
        deviceListeners.remove(listener);
    }

    public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex) {
        return new DefaultUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
    }

    public void asyncSubmit(List<UsbControlIrp> list) {
        throw new RuntimeException("Not implemented");
    }

    public void asyncSubmit(UsbControlIrp irp) {
        throw new RuntimeException("Not implemented");
    }

    public void syncSubmit(List<UsbControlIrp> list) {
        throw new RuntimeException("Not implemented");
    }

    public void syncSubmit(UsbControlIrp irp) throws UsbException {
        internalSyncSubmit(irp);
    }

    public UsbPort getParentUsbPort() throws UsbDisconnectedException {
        throw new RuntimeException("Not implemented");
    }

    public byte getActiveUsbConfigurationNumber() {
        throw new RuntimeException("Not implemented");
    }

    public UsbConfiguration getActiveUsbConfiguration() {
        // TOOD: This is according to the specification, but what happens if
        // another process changes the configuration?
        return getUsbConfiguration(activeConfiguration);

        //try {
        //    return nativeGetActiveUsbConfiguration();
        //} catch (UsbPlatformException e) {
        //    throw new RuntimeException(e);
        //}
    }

    public boolean containsUsbConfiguration(byte number){
        return configurations[number] != null;
        //try {
        //    return nativeGetUsbConfiguration(number) != null;
        //} catch (UsbPlatformException e) {
        //    throw new RuntimeException(e);
        //}
    }

    public UsbConfiguration getUsbConfiguration(byte number) {
        if(number == 0) {
            System.err.println("Usb configuration #0 was requested.");
            return null;
        }

        return configurations[number];
        // This is not right. The configurations probably have to be cached somewhere as we can't throw the exception out.
        //try {
        //    return nativeGetUsbConfiguration(number);
        //} catch (UsbPlatformException e) {
        //    throw new RuntimeException("getUsbConfiguration", e);
        //}
    }

    public List<UsbConfiguration> getUsbConfigurations() {
        return Arrays.asList(configurations);
        //byte n = getUsbDeviceDescriptor().bNumConfigurations();
        //List<UsbConfiguration> list = new ArrayList<UsbConfiguration>(n);
        //for (byte i = 0; i < n; i++) {
        //    list.add(getUsbConfiguration(i));
        //}
        //return list;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public synchronized void close() throws IOException {
        closed = true;
        nativeClose(libusb_device);
    }

    @Override
    protected void finalize() throws Throwable {
        if(!closed) {
            close();
        }
    }

    private void internalSyncSubmit(UsbControlIrp irp) throws UsbException {
        if (irp == null) {
            throw new IllegalArgumentException("irp");
        }

        // TODO: check if this is active

        if (irp.getUsbException() != null) {
            throw new IllegalArgumentException("usbException is not null");
        }

        if (irp.isComplete()) {
            throw new IllegalArgumentException("complete == true");
        }

        libusb1.control_transfer(libusb_device, irp.bmRequestType(), irp.bRequest(), irp.wValue(), irp.wIndex(), 0);
    }

    // -----------------------------------------------------------------------
    // Native Interface
    // -----------------------------------------------------------------------

    public void _setConfiguration(UsbConfiguration configuration, byte n) {
        configurations[n] = configuration;
    }

    public void _setActiveConfiguration(byte n) {
        activeConfiguration = n;
    }

    native
    public void nativeClose(long device);

    /**
     * Returns null on failure.
     */
    native
    private String nativeGetString(long device, byte index, int length)
        throws UsbPlatformException;

//    native
//    private UsbConfiguration nativeGetActiveUsbConfiguration()
//        throws UsbPlatformException;

//    native
//    private UsbConfiguration nativeGetUsbConfiguration(byte number)
//        throws UsbPlatformException;
}
