package javalibusb1.impl;

import javax.usb.*;
import java.util.Arrays;
import java.util.List;

public class Libusb1UsbConfiguration implements UsbConfiguration {

    public final UsbDevice device;
    public final UsbConfigurationDescriptor configurationDescriptor;
    public final UsbInterface[][] interfaces;
    private boolean active;

    public Libusb1UsbConfiguration(UsbDevice device, UsbConfigurationDescriptor configurationDescriptor, UsbInterface[][] interfaces, boolean active) {
        this.device = device;
        this.configurationDescriptor = configurationDescriptor;
        this.interfaces = interfaces;
        this.active = active;
    }

    public boolean containsUsbInterface(byte number) {
        return interfaces[number] != null;
    }

    public String getConfigurationString() throws UsbException {
        return device.getString(configurationDescriptor.iConfiguration());
    }

    public UsbConfigurationDescriptor getUsbConfigurationDescriptor() {
        return configurationDescriptor;
    }

    public UsbDevice getUsbDevice() {
        return device;
    }

    public UsbInterface getUsbInterface(byte number) {
        if (number > interfaces.length) {
            return null;
        }

        // Return the active interface if this is the active configuration
        if (isActive()) {
            for (UsbInterface usbInterface : interfaces[number]) {
                if (usbInterface.isActive()) {
                    return usbInterface;
                }
            }
        }

        return interfaces[number][0];
    }

    public List<UsbInterface> getUsbInterfaces() {
        UsbInterface[] is = new UsbInterface[this.interfaces.length];
        if (isActive()) {
            for (int i = 0; i < interfaces.length; i++) {
                for (UsbInterface ifc : interfaces[i]) {
                    if (ifc.isActive()) {
                        is[i] = ifc;
                    }
                }
            }

            return Arrays.asList(is);
        }

        for (int i = 0; i < interfaces.length; i++) {
            is[i] = interfaces[i][0];
        }
        return Arrays.asList(is);
    }

    public boolean isActive() {
        return active;
    }
}
