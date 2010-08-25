package javalibusb1;

import javax.usb.*;
import java.io.*;
import java.util.*;

class libusb1 implements Closeable {

    // This field is used from the native code.
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final long libusb_context;

    libusb1(long libusb_context) {
        this.libusb_context = libusb_context;
    }

    public void close() {
        close(libusb_context);
    }

    public UsbDevice[] getDevices() {
        List<UsbDevice> devices = new ArrayList<UsbDevice>();

        for (Libusb1UsbDevice device : get_devices(libusb_context)) {
            if(device != null) {
                devices.add(device);
            }
        }

        return devices.toArray(new UsbDevice[devices.size()]);
    }

    public void set_debug(int level) {
        set_debug(libusb_context, level);
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
    public static int control_transfer(long libusb_device, byte bmRequestType, byte bRequest, short wValue, short wIndex, long timeout) throws UsbException;

    native
    public static int bulk_transfer(long libusb_device_handle, byte bEndpointAddress, byte[] buffer, int offest, int length);

    static {
        NarSystem.loadLibrary();
    }
}
