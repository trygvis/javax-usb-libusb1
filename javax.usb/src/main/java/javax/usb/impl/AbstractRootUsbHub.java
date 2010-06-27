package javax.usb.impl;

import static java.util.Collections.*;

import javax.usb.*;
import javax.usb.event.*;
import java.util.*;

public abstract class AbstractRootUsbHub implements UsbHub {

    public final String manufacturerString;
    public final String serialNumberString;
    public final String productString;
    public final UsbDeviceDescriptor usbDeviceDescriptorInstance;

    protected AbstractRootUsbHub(String manufacturerString, String serialNumberString, String productString, UsbDeviceDescriptor usbDeviceDescriptorInstance) {
        this.manufacturerString = manufacturerString;
        this.serialNumberString = serialNumberString;
        this.productString = productString;
        this.usbDeviceDescriptorInstance = usbDeviceDescriptorInstance;
    }

    public String getManufacturerString() {
        return manufacturerString;
    }

    public String getSerialNumberString() {
        return serialNumberString;
    }

    public String getProductString() {
        return productString;
    }

    public UsbStringDescriptor getUsbStringDescriptor(byte index) {
        return null;
    }

    public String getString(byte index) {
        return null;
    }

    public UsbDeviceDescriptor getUsbDeviceDescriptor() {
        return usbDeviceDescriptorInstance;
    }

    public Object getSpeed() {
        return UsbConst.DEVICE_SPEED_UNKNOWN;
    }

    public boolean isConfigured() {
        return true;
    }

    public boolean isUsbHub() {
        return true;
    }

    public void addUsbDeviceListener(UsbDeviceListener listener) {
    }

    public void removeUsbDeviceListener(UsbDeviceListener listener) {
    }

    public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex) {
        throw new IllegalArgumentException("Not allowed on the root hub");
    }

    public void asyncSubmit(List<UsbControlIrp> list) throws UsbException, IllegalArgumentException, UsbDisconnectedException {
        throw new IllegalArgumentException("Not allowed on the root hub");
    }

    public void asyncSubmit(UsbControlIrp irp) throws UsbException, IllegalArgumentException, UsbDisconnectedException {
        throw new IllegalArgumentException("Not allowed on the root hub");
    }

    public void syncSubmit(List<UsbControlIrp> list) throws UsbException, IllegalArgumentException, UsbDisconnectedException {
        throw new IllegalArgumentException("Not allowed on the root hub");
    }

    public void syncSubmit(UsbControlIrp irp) throws UsbException, IllegalArgumentException, UsbDisconnectedException {
        throw new IllegalArgumentException("Not allowed on the root hub");
    }

    public UsbPort getParentUsbPort() throws UsbDisconnectedException {
        return null;
    }

    public UsbConfiguration getActiveUsbConfiguration() {
        return null; // TODO: This is not entirely correct
    }

    public boolean containsUsbConfiguration(byte number) {
        return false;
    }

    public UsbConfiguration getUsbConfiguration(byte number) {
        return null; // TODO: This is not entirely correct
    }

    public List<UsbConfiguration> getUsbConfigurations() {
        return emptyList();
    }

    // -----------------------------------------------------------------------
    // UsbHub Implementation
    // -----------------------------------------------------------------------

    public boolean isRootUsbHub() {
        return true;
    }

    public byte getNumberOfPorts() {
        return (byte) getUsbPorts().size();
    }

    public UsbPort getUsbPort(byte number) {
        return getUsbPorts().get(number);
    }

    public List<UsbPort> getUsbPorts() {
        List<UsbPort> ports = new ArrayList<UsbPort>();
        for (int i = 0; i < getAttachedUsbDevices().size(); i++) {
            final UsbDevice device = getAttachedUsbDevices().get(i);
            final byte index = (byte) i;
            ports.add(new UsbPort() {
                public byte getPortNumber() {
                    return index;
                }

                public UsbHub getUsbHub() {
                    return AbstractRootUsbHub.this;
                }

                public UsbDevice getUsbDevice() {
                    return device;
                }

                public boolean isUsbDeviceAttached() {
                    return true; // We know this as we're using the device :)
                }
            });
        }
        return ports;
    }
}
