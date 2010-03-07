package javalibusb0;

class libusb0 {
    /**
     * Does both libusb init and JNI initialization.
     */
    native static void init();

    /**
     * @return the number of changes since previous call to this function (total of new busses and busses removed).
     */
    native static int jusb_find_busses();

    /**
     * @return the number of changes since previous call to this function (total of new busses and busses removed).
     */
    native static int jusb_find_devices();

//    native String usb_get_string(usb_dev_handle *dev, int index, int langid, char *buf, size_t buflen);

    native static Libusb0Bus[] jusb_get_busses();

    static {
        System.loadLibrary("javalibusb1");
    }
}
