package no.hackaton.usb.tools;

import static javax.usb.util.UsbUtil.*;

import javax.usb.*;
import javax.usb.util.*;
import java.util.*;

public class lsusb {
    public static void main(String[] args) throws UsbException {
        try {
            UsbServices usbServices = UsbHostManager.getUsbServices();

            dump(usbServices.getRootUsbHub(), args);
        } catch (UsbPlatformException e) {
            System.out.println("Platform error code: " + e.getErrorCode() + ", libusb: " + decodeLibusbError(e.getErrorCode()));
            e.printStackTrace();
            if (e.getPlatformException() != null) {
                System.out.println("Platform exception");
                e.getPlatformException().printStackTrace();
            }
        }
    }

    private static void dump(UsbHub usbHub, String[] args) throws UsbException {
        if (args.length > 0) {
            for (String id : args) {
                UsbDevice device = UsbCliUtil.findDevice(usbHub, null, null, id);

                if(device == null) {
                    System.err.println("Could not find device '" + id + "'.");
                }
                else {
                    dump(device);
                }
            }
        } else {
            System.out.println("Usb hub");

            for (UsbDevice usbDevice : usbHub.getAttachedUsbDevices()) {
                System.out.println("Usb device:");
                dump(usbDevice);
                close(usbDevice);
            }

        }
    }

    private static void dump(UsbDevice usbDevice) {
        UsbDeviceDescriptor descriptor = usbDevice.getUsbDeviceDescriptor();

        System.out.println(String.format(" %-18s %10s", "Configured:", usbDevice.isConfigured()));
        System.out.println(String.format(" %-18s %10s", "bdcUSB", twoDigitBdc(descriptor.bcdUSB())));
        System.out.println(String.format(" %-18s %10s", "bDeviceClass: ", unsignedInt(descriptor.bDeviceClass())));
        System.out.println(String.format(" %-18s %10s", "bDeviceSubClass", unsignedInt(descriptor.bDeviceSubClass())));
        System.out.println(String.format(" %-18s %10s", "bDeviceProtocol", unsignedInt(descriptor.bDeviceProtocol())));
        System.out.println(String.format(" %-18s %10s", "bMaxPacketSize", unsignedInt(descriptor.bMaxPacketSize0())));
        System.out.println(String.format(" %-18s %10s", "idVendor", toHexString(descriptor.idVendor())));
        System.out.println(String.format(" %-18s %10s", "idProduct", toHexString(descriptor.idProduct())));
        System.out.println(String.format(" %-18s %10s", "bcdDevice", twoDigitBdc(descriptor.bcdDevice())));
        System.out.println(String.format(" %-18s %10s %s", "iManufacturer", descriptor.iManufacturer(), getString(usbDevice, descriptor.iManufacturer())));
        System.out.println(String.format(" %-18s %10s %s", "iProduct", descriptor.iProduct(), getString(usbDevice, descriptor.iProduct())));
        System.out.println(String.format(" %-18s %10s %s", "iSerialNumber", descriptor.iSerialNumber(), getString(usbDevice, descriptor.iSerialNumber())));
        System.out.println(String.format(" %-18s %10s", "bNumConfigurations", descriptor.bNumConfigurations()));

//        List<UsbConfiguration> list = usbDevice.getUsbConfigurations();
        byte numConfigurations = usbDevice.getUsbDeviceDescriptor().bNumConfigurations();

        for (int i = 0; i < numConfigurations; i++) {
            UsbConfiguration usbConfiguration;
            try {
                usbConfiguration = usbDevice.getUsbConfiguration((byte) (i + 1));
            } catch (Exception e) {
                System.out.println(" Configuration #" + i + " - unable to read from device");
                continue;
            }

            if (usbConfiguration == null) {
                System.out.println("Unable to read configuration #" + i + ". This is a violation of the USB spec.");
                continue;
            }

            UsbConfigurationDescriptor configurationDescriptor = usbConfiguration.getUsbConfigurationDescriptor();
            System.out.println(" Configuration #" + i);
            System.out.println(String.format("  %-17s %8s", "Active", usbConfiguration.isActive()));
            System.out.println(String.format("  %-17s %8s", "bConfigurationValue", configurationDescriptor.bConfigurationValue()));
            System.out.println(String.format("  %-17s %10s", "bmAttributes", "0x" + toHexString(configurationDescriptor.bmAttributes())));
            System.out.println(String.format("  %-17s %10s", "bMaxPower", configurationDescriptor.bMaxPower()));
            System.out.println(String.format("  %-17s %10s", "bNumInterfaces", unsignedInt(configurationDescriptor.bNumInterfaces())));
            System.out.println(String.format("  %-17s %10s %s", "iConfiguration", unsignedInt(configurationDescriptor.iConfiguration()), getString(usbDevice, configurationDescriptor.iConfiguration())));
            System.out.println(String.format("  %-17s %10s", "wTotalLength", unsignedInt(configurationDescriptor.wTotalLength())));

            List<UsbInterface> interfaces = usbConfiguration.getUsbInterfaces();
            for (int j = 0; j < interfaces.size(); j++) {
                UsbInterface usbInterface = interfaces.get(j);
                UsbInterfaceDescriptor interfaceDescriptor = usbInterface.getUsbInterfaceDescriptor();

                System.out.println("  Interfaces #" + j);
                System.out.println(String.format("   %-16s %9s", "bAlternateSetting", unsignedInt(interfaceDescriptor.bAlternateSetting())));
                System.out.println(String.format("   %-16s %10s", "bInterfaceClass", unsignedInt(interfaceDescriptor.bInterfaceClass())));
                System.out.println(String.format("   %-16s %10s", "bInterfaceNumber", unsignedInt(interfaceDescriptor.bInterfaceNumber())));
                System.out.println(String.format("   %-16s %8s", "bInterfaceProtocol", unsignedInt(interfaceDescriptor.bInterfaceProtocol())));
                System.out.println(String.format("   %-16s %8s", "bInterfaceSubClass", unsignedInt(interfaceDescriptor.bInterfaceSubClass())));
                System.out.println(String.format("   %-16s %10s", "bNumEndpoints", unsignedInt(interfaceDescriptor.bNumEndpoints())));
                System.out.println(String.format("   %-16s %10s %s", "iInterface", unsignedInt(interfaceDescriptor.iInterface()), getString(usbDevice, interfaceDescriptor.iInterface())));

                List<UsbEndpoint> endpoints = usbInterface.getUsbEndpoints();
                for (int k = 0; k < endpoints.size(); k++) {
                    UsbEndpoint endpoint = endpoints.get(k);
                    UsbEndpointDescriptor endpointDescriptor = endpoint.getUsbEndpointDescriptor();
                    System.out.println("   Endpoint #" + k);
                    System.out.println(String.format("    %-15s %10s", "Direction", endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN ? "in" : "out"));
                    System.out.println(String.format("    %-15s %10s", "Type", endpoint.getType()));
                    System.out.println(String.format("    %-15s %9s", "bEndpointAddress", "0x" + toHexString(endpointDescriptor.bEndpointAddress())));
                    System.out.println(String.format("    %-15s %10s", "bInterval", unsignedInt(endpointDescriptor.bInterval())));
                    System.out.println(String.format("    %-15s %10s", "bmAttributes", endpointDescriptor.bmAttributes()));
                    System.out.println(String.format("    %-15s %10s", "wMaxPacketSize", endpointDescriptor.wMaxPacketSize()));
                }
            }
        }
    }

    private static String getString(UsbDevice device, byte index) {
        if (index == 0) {
            return "";
        }

        try {
            return device.getString(index);
        } catch (UsbException e) {
            if (e instanceof UsbPlatformException) {
                UsbPlatformException p = (UsbPlatformException) e;
                return "Unable to get string #" + index + ", libusb: " + UsbUtil.decodeLibusbError(p.getErrorCode());
            }
            return "Unable to get string #" + index;
        }
    }
}
