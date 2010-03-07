package javax.usb.impl;

import javax.usb.*;
import java.util.Collections;
import java.util.List;

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

    public boolean isUsbHub() {
        return true;
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
        return Collections.emptyList();
    }

    // -----------------------------------------------------------------------
    // UsbHub Implementation
    // -----------------------------------------------------------------------

    public boolean isRootHub() {
        return true;
    }
}
