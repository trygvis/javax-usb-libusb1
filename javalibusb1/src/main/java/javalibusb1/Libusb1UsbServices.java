package javalibusb1;

import static javax.usb.UsbConst.*;
import static javax.usb.UsbHostManager.*;

import javax.usb.*;
import javax.usb.event.*;
import javax.usb.impl.*;
import java.io.*;
import java.util.*;

public class Libusb1UsbServices implements UsbServices {

    public static final String JAVAX_USB_LIBUSB_TRACE_PROPERTY = "javax.usb.libusb.trace";
    public static final String JAVAX_USB_LIBUSB_DEBUG_PROPERTY = "javax.usb.libusb.debug";

    /**
     * The path to the shared library that the implementation will use.
     */
    public static final String JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH = "javax.usb.libusb.javalibusb1.path";

    UsbDeviceDescriptor rootUsbDeviceDescriptorInstance = new DefaultUsbDeviceDescriptor(
        (short) 0x0200,         // USB 2.0
        HUB_CLASSCODE,
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

    private libusb1 libusb;
    private List<UsbDevice> devices;

    public Libusb1UsbServices() throws UsbException {
        // TODO: Parse the debug settings for the native code before loading
        // the library so that the libusb_set_trace and usbw debugging is
        // enabled as soon as possible.
        libusb = libusb1.create();

        boolean trace = false;
        Integer debug_level = null;

        InputStream stream = UsbHostManager.class.getClassLoader().
            getResourceAsStream(JAVAX_USB_PROPERTIES_FILE);

        try {
            Properties properties = new Properties();

            if (stream != null) {
                properties.load(stream);
                trace = Boolean.parseBoolean(properties.getProperty(JAVAX_USB_LIBUSB_TRACE_PROPERTY, "false"));

                String s = properties.getProperty(JAVAX_USB_LIBUSB_DEBUG_PROPERTY);
                if (s != null) {
                    debug_level = Integer.parseInt(s);
                }
            }
        } catch (IOException e) {
            throw new UsbPlatformException("Error while reading configuration file.", e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

        trace = Boolean.getBoolean(JAVAX_USB_LIBUSB_TRACE_PROPERTY) || trace;
        libusb1.set_trace_calls(trace);

        debug_level = Integer.getInteger(JAVAX_USB_LIBUSB_DEBUG_PROPERTY, debug_level);

        if (debug_level != null) {
            libusb.set_debug(debug_level);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        libusb.close();
    }

    public String getApiVersion() {
        return "1.0.1";
    }

    public String getImpDescription() {
        return "Usb for Java with libusb-1.0";
    }

    public String getImpVersion() {
        // TODO: Load from Maven artifact
        return "1.0-SNAPSHOT";
    }

    public void addUsbServicesListener(UsbServicesListener listener) {
    }

    public void removeUsbServicesListener(UsbServicesListener listener) {
    }

    public synchronized UsbHub getRootUsbHub() {
        // HOTPLUG
        if (devices == null) {
            devices = Arrays.asList(libusb.getDevices());
        }

        return new LibUsb1RootUsbHub(devices);
    }

    private class LibUsb1RootUsbHub extends AbstractRootUsbHub {
        private List<UsbDevice> usbDevices;

        public LibUsb1RootUsbHub(List<UsbDevice> usbDevices) {
            super("Virtual Root", null, null, Libusb1UsbServices.this.rootUsbDeviceDescriptorInstance);

            this.usbDevices = usbDevices;
        }

        public List<UsbDevice> getAttachedUsbDevices() {
            return usbDevices;
        }
    }
}
