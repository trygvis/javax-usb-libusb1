package javalibusb1;

import static javalibusb1.Libusb1UsbServices.*;

import javax.usb.*;
import java.io.*;
import java.net.*;
import java.util.*;

class libusb1 implements Closeable {

    // This field is used from the native code.
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final long libusb_context_ptr;

    libusb1(long libusb_context_ptr) {
        this.libusb_context_ptr = libusb_context_ptr;
    }

    public void close() {
        close(libusb_context_ptr);
    }

    public UsbDevice[] getDevices() {
        List<UsbDevice> devices = new ArrayList<UsbDevice>();

        for (Libusb1UsbDevice device : get_devices(libusb_context_ptr)) {
            if (device != null) {
                devices.add(device);
            }
        }

        return devices.toArray(new UsbDevice[devices.size()]);
    }

    public void set_debug(int level) {
        set_debug(libusb_context_ptr, level);
    }

    /**
     * Enable tracing of calls to libusb.
     */
    native
    public static void set_trace_calls(boolean on);

    /**
     * Creates a new libusb context. Each instance of {@link libusb1} wraps a
     * <tt>struct libusb_context*</tt>.
     */
    native
    public static libusb1 create();

    native
    public void close(long libusb_context);

    native
    private void set_debug(long libusb_context, int level);

    /**
     * Returns an array with all devices. The array might contain NULL entries which
     * indicate some form of non-critical error when looking up a device. The device
     * itself is not available, but the rest of the system is working.
     */
    native
    private Libusb1UsbDevice[] get_devices(long libusb_context);

    native
    public static int control_transfer(long libusb_device_ptr, byte bmRequestType, byte bRequest, short wValue, short wIndex, long timeout,
                                       byte[] bytes, int offset, short length) throws UsbException;

    native
    public static int bulk_transfer(long libusb_device_handle, byte bEndpointAddress, byte[] buffer, int offest, int length, long timeout);

    native
    public static int interrupt_transfer(long libusb_device_handle, byte bEndpointAddress, byte[] buffer, int offest, int length, long timeout);

    static {
        String path = System.getProperty(JAVAX_USB_LIBUSB_JAVALIBUSB1_PATH);
        String aol = getAol();

        if (path != null && loadFromPath(path)) {
        } else if (aol != null && loadLibraryFromAol(aol)) {
        } else {
            // Couldn't find an AOL for this platform, fall back to the default of using System.loadLibrary.
            NarSystem.loadLibrary();
        }
    }

    private static boolean loadFromPath(String path) {
        try {
            System.load(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean loadLibraryFromAol(String aol) {
        String name = System.mapLibraryName("javalibusb1-1.0.1-1-SNAPSHOT");

        String p = "lib/" + aol + "/jni/" + name;

        System.out.println("p=" + p);

        URL url = libusb1.class.getClassLoader().getResource(p);
        System.out.println("url = " + url);

        if (url == null) {
            return false;
        }

        File file = null;

        // TODO: only copy the file if it is not a file

        OutputStream os = null;
        InputStream is = null;
        try {
            file = File.createTempFile("javalibusb1", "");
            System.out.println("file = " + file);

            is = url.openStream();
            os = new FileOutputStream(file);

            file.deleteOnExit();

            byte buffer[] = new byte[128 * 1024];

            int read = is.read(buffer);

            while (read > 0) {
                os.write(buffer, 0, read);
                read = is.read(buffer);
            }

            closeSilently(os);
            closeSilently(is);

            System.load(file.getAbsolutePath());

            return true;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load native library.", e);
        } finally {
            if(file != null) {
                file.delete();
            }
            closeSilently(os);
            closeSilently(is);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public static String getAol() {
        String osArch = System.getProperty("os.arch");

        String osName = System.getProperty("os.name");

        System.out.println("osName = " + osName);
        if (osName.equals("Mac OS X")) {
            osName = "MacOSX";
        } else if (osName.equals("Linux")) {
            osName = "Linux";
        } else {
            return null; // unsupported os.name
        }

        String linker = "gpp";

        return osArch + "-" + osName + "-" + linker;
    }
}
