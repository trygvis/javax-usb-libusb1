package javalibusb1;

import javax.usb.*;
import java.io.*;

class libusb1 implements Closeable {

    // This field is used from the native code.
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final int usb_services_context;

    libusb1(int usb_services_context) {
        this.usb_services_context = usb_services_context;
    }

    /**
     * Enable tracing of calls to libusb.
     */
    native
    public static void set_trace_calls(boolean on);

    native
    public static libusb1 create();

    native
    public void close();

    native
    public void set_debug(int level);

    native
    public Libusb1UsbDevice[] get_devices();

    native
    public static int control_transfer(int device, byte bmRequestType, byte bRequest, short wValue, short wIndex, long timeout) throws UsbException;

    native
    public static int bulk_transfer(int handle, byte bEndpointAddress, byte[] buffer, int offest, int length);

    static {
        System.loadLibrary("javalibusb1");
    }
}
