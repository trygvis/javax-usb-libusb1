package javax.usb;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UsbDisconnectedException extends UsbException {
    public UsbDisconnectedException() {
    }

    public UsbDisconnectedException(String message) {
        super(message);
    }
}
