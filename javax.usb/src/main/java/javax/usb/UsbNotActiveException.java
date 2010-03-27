package javax.usb;

public class UsbNotActiveException extends RuntimeException {
    public UsbNotActiveException() {
    }

    public UsbNotActiveException(String s) {
        super(s);
    }
}
