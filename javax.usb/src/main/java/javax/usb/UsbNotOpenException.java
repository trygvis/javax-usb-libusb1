package javax.usb;

public class UsbNotOpenException extends RuntimeException {
    public UsbNotOpenException() {
    }

    public UsbNotOpenException(String s) {
        super(s);
    }
}