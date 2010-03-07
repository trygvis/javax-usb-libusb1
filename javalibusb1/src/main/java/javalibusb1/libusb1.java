package javalibusb1;

import javalibusb1.impl.Libusb1UsbDevice;

import java.io.Closeable;

class libusb1 implements Closeable {

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

    static {
        System.loadLibrary("javalibusb1");
    }
}
