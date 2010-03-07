package javax.usb;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UsbPlatformException extends UsbException {
    private int errorCode;
    private Exception platformException;

    public UsbPlatformException() {
    }

    public UsbPlatformException(Exception platformException) {
        this.platformException = platformException;
    }

    public UsbPlatformException(int errorCode) {
        this.errorCode = errorCode;
    }

    public UsbPlatformException(int errorCode, Exception platformException) {
        this.errorCode = errorCode;
        this.platformException = platformException;
    }

    public UsbPlatformException(String message) {
        super(message);
    }

    public UsbPlatformException(String message, Exception platformException) {
        super(message);
        this.platformException = platformException;
    }

    public UsbPlatformException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UsbPlatformException(String message, int errorCode, Exception platformException) {
        super(message);
        this.errorCode = errorCode;
        this.platformException = platformException;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Exception getPlatformException() {
        return platformException;
    }
}
