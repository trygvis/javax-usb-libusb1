package javax.usb;

import java.io.*;

public interface UsbStringDescriptor extends UsbDescriptor {
    byte[] bString();

    String getString() throws UnsupportedEncodingException;
}
