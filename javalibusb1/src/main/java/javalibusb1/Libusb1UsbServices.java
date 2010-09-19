package javalibusb1;

import static java.lang.Boolean.*;
import static java.lang.Integer.*;
import static javax.usb.UsbConst.*;

import javax.usb.*;
import javax.usb.event.*;
import javax.usb.impl.*;
import java.util.*;

/**
 * The starting point of the implementation.
 * <p>
 * This class is instantiated by the {@link UsbHostManager}. On instantiation it will
 * <ol>
 * <li>Load the settings</li>
 * <li>Find and load the native <code>javalibusb1</code> library for the current platform</li>
 * <li>Create an libusb context</li>
 * </ol>
 * <h2>Settings loading</h2>
 * The library has three settings:
 * <ul>
 * <li>
 * Trace: set to true if all calls to libusb should be logged. A line will
 * be written to stderr before and after each call.
 * </li>
 * <li>
 * Libusb debug level: Passed on directly to libusb. Currenctly a value
 * from 0 to 3 with 0 giving no output and 3 is the most verbose.
 * </li>
 * <li>
 * Path to the javalibusb1 library. Set this to disable the automagic method.
 * </li>
 * </ul>
 * The values for the settings are loaded from three places, first one wins:
 * <ul>
 * <li>The javax.usb.properties configuration file.</li>
 * <li>System properties with <code>System.getProperty()</code>.</li>
 * <li>System environment with <code>System.getenv()</code>.</li>
 * </ul>
 * The keys used are:
 * <ul>
 * <li>Trace:
 * <ul>
 * <li>In javax.usb.properties: {@link #JAVAX_USB_LIBUSB_TRACE_PROPERTY}</li>
 * <li>System property: {@link #JAVAX_USB_LIBUSB_TRACE_PROPERTY}</li>
 * <li>System environment: {@link #JAVAX_USB_LIBUSB_TRACE_ENV}</li>
 * </ul>
 * </li>
 * <li>Debug:
 * <ul>
 * <li>In javax.usb.properties: {@link #JAVAX_USB_LIBUSB_DEBUG_PROPERTY}</li>
 * <li>System property: {@link #JAVAX_USB_LIBUSB_DEBUG_PROPERTY}</li>
 * <li>System environment: {@link #JAVAX_USB_LIBUSB_DEBUG_ENV}</li>
 * </ul>
 * </li>
 * <li>Library path:
 * <ul>
 * <li>In javax.usb.properties: {@link #JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH_PROPERTY}</li>
 * <li>System property: {@link #JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH_PROPERTY}</li>
 * <li>System environment: {@link #JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH_ENV}</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <h2>Load the library</h2>
 * <p>
 * A bit of magic is applied when the native library is loaded.
 * <ol>
 * <li>
 * If the library path is configured that will be used.
 * </li>
 * <li>
 * If there is a resource called <code>lib/<i>aol</i>/jni/<i>name</i></code>
 * where AOL is the identifier for your platform (i386-Linux-gpp,
 * x86_64-MacOSX-gpp, etc) and name is the platform-specific library name
 * (<code>libjavalibusb1.so</code> on Linux,
 * <code>libjavalibusb1.jnilib</code> on OSX/Darwin,
 * <code>javalibusb.dll</code> on Windows, etc).
 * </li>
 * <li>
 * The normal <code>System.loadLibrary("javalibusb1")</code> mechanism.
 * </li>
 * </ol>
 * Note that if any of the mechanisms fail the next one will be tried.
 * </p>
 * <h2>Creating an libusb context</h2>
 * <p>
 * After the library has been successfully loaded it will initialize a libusb context.
 * </p>
 *
 * @see javax.usb.UsbHostManager
 */
public class Libusb1UsbServices implements UsbServices {

    // TODO: Load from Maven artifact or NarSystem
    public static final String VERSION = "1.0.1-1-SNAPSHOT";

    /**
     * The name of the property that will be looked up from the javax.usb.properties file or the system property.
     */
    public static final String JAVAX_USB_LIBUSB_TRACE_PROPERTY = "javax.usb.libusb.trace";

    public static final String JAVAX_USB_LIBUSB_DEBUG_PROPERTY = "javax.usb.libusb.debug";

    public static final String JAVAX_USB_LIBUSB_DEBUG_ENV = "JAVAX_USB_LIBUSB_DEBUG";

    public static final String JAVAX_USB_LIBUSB_TRACE_ENV = "JAVAX_USB_LIBUSB_TRACE";

    /**
     * The system property use to look up the path to the shared library that the implementation will use.
     */
    public static final String JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH_PROPERTY = "javax.usb.libusb.javalibusb1.path";

    public static final String JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH_ENV = "JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH";

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
        boolean trace;
        int debug_level = 0;

        trace = parseBoolean(Libusb1Utils.loadProperty(JAVAX_USB_LIBUSB_TRACE_PROPERTY, JAVAX_USB_LIBUSB_TRACE_ENV));

        String s = Libusb1Utils.loadProperty(JAVAX_USB_LIBUSB_DEBUG_PROPERTY, JAVAX_USB_LIBUSB_DEBUG_ENV);
        if (s != null) {
            debug_level = parseDebugLevel(s);
        }

        libusb1.set_trace_calls(trace);

        // TODO: Parse the debug settings for the native code before loading
        // the library so that the libusb_set_trace and usbw debugging is
        // enabled as soon as possible.
        libusb = libusb1.create(debug_level);
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
        return VERSION;
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

    private static int parseDebugLevel(String s) {
        try {
            return parseInt(s);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Not a valid debug level: '" + s + "'.");
        }
    }
}
