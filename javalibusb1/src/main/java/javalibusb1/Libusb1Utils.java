package javalibusb1;

import static javax.usb.UsbHostManager.*;

import javax.usb.*;
import java.io.*;
import java.util.*;

public class Libusb1Utils {
    private static final Properties javaxUsbProperties = new Properties();

    static {
        InputStream stream = UsbHostManager.class.getClassLoader().
            getResourceAsStream(JAVAX_USB_PROPERTIES_FILE);

        try {
            if (stream != null) {
                javaxUsbProperties.load(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading configuration file.", e);
        } finally {
            closeSilently(stream);
        }
    }

    public static String loadProperty(String propertyKey, String envKey) {
        String s = javaxUsbProperties.getProperty(propertyKey);

        if (s != null) {
            return s;
        }

        try {
            s = System.getProperty(propertyKey);
        } catch (SecurityException e) {
            // ignore
        }

        if (s != null) {
            return s;
        }

        try {
            return System.getenv(envKey);
        } catch (SecurityException e) {
            // ignore
            return null;
        }
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
