package javalibusb0;

import javalibusb0.impl.*;

import javax.usb.*;
import javax.usb.UsbDevice;
import javax.usb.impl.*;
import java.util.*;

public class Libusb0UsbServices implements UsbServices {
    UsbDeviceDescriptor rootUsbDeviceDescriptorInstance = new DefaultUsbDeviceDescriptor(
            (short) 0x0200,         // USB 2.0
            UsbConst.HUB_CLASSCODE,
            (byte) 0,               // See http://www.usb.org/developers/defined_class/#BaseClass09h
            (byte) 2,               // See http://www.usb.org/developers/defined_class/#BaseClass09h
            (byte) 8,               // Shouldn't really matter what we say here, any transfer will fail
            (short) 0x6666,         // Supposedly "experimental", see https://usb-ids.gowdy.us/read/UD/6666
            (short) 0,
            (short) 0x100,          // 1.0
            (byte) 0,
            (byte) 0,
            (byte) 0,
            (byte) 1);

    public Libusb0UsbServices() {
        libusb0.init();
    }

    public String getApiVersion() {
        return "1.0.1";
    }

    public String getImplDescription() {
        return "Usb for Java";
    }

    public String getImplVersion() {
        // TODO: Load from Maven artifact
        return "1.0-SNAPSHOT";
    }

    public UsbHub getRootUsbHub() {
        List<UsbDevice> usbDevices = new LinkedList<UsbDevice>();
        Libusb0Bus[] busses = libusb0.jusb_get_busses();
        for (Libusb0Bus bus : busses) {
            for (Libusb0Device usbDevice : bus.devices) {
                usbDevices.add(new Libusb0UsbDevice(usbDevice));
            }
        }

        return new LibUsb0RootUsbHub(Collections.unmodifiableList(usbDevices));
    }

    private class LibUsb0RootUsbHub extends AbstractRootUsbHub {
        private List<UsbDevice> usbDevices;

        public LibUsb0RootUsbHub(List<UsbDevice> usbDevices) {
            super("Virtual Root", null, null, Libusb0UsbServices.this.rootUsbDeviceDescriptorInstance);

            this.usbDevices = usbDevices;
        }

        public List<UsbDevice> getAttachedUsbDevices() {
            return usbDevices;
        }
    }
}
