package javax.usb;

public class UsbNotClaimedException extends RuntimeException {
    public UsbNotClaimedException() {
    }

    public UsbNotClaimedException(String s) {
        super(s);
    }
}
