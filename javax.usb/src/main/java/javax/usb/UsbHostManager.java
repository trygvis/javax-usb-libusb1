package javax.usb;

import java.io.*;
import java.util.*;

public final class UsbHostManager {

    public static final String JAVAX_USB_USBSERVICES_PROPERTY = "javax.usb.usbservices";

    public static final String JAVAX_USB_PROPERTIES_FILE = "javax.usb.properties";

    private static UsbServices usbServices;

    private final static Object lock = new Object();

    private UsbHostManager() {
    }

    public static UsbServices getUsbServices() throws UsbException, SecurityException {
        synchronized (lock) {
            if (usbServices == null) {
                usbServices = initialize();
            }
            return usbServices;
        }
    }

    private static UsbServices initialize() throws UsbException, SecurityException {
        Properties properties = getProperties();

        String services = properties.getProperty(JAVAX_USB_USBSERVICES_PROPERTY);

        if (services == null) {
            throw new UsbException("Missing required property '" + JAVAX_USB_USBSERVICES_PROPERTY + "' from configuration file.");
        }


        // TODO: Use the thread's current context class loader?

        Class<?> usbServicesClass;

        try {
            usbServicesClass = UsbHostManager.class.getClassLoader().loadClass(services);

            return (UsbServices) usbServicesClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new UsbPlatformException("Unable to load UsbServices class '" + services + "'.", e);
        } catch (InstantiationException e) {
            throw new UsbPlatformException("Unable to instantiate class '" + services + "'.", e);
        } catch (IllegalAccessException e) {
            throw new UsbPlatformException("Unable to instantiate class '" + services + "'.", e);
        } catch (ClassCastException e) {
            throw new UsbPlatformException("Class " + services + " is not an instance of javax.usb.UsbServices.");
        }
    }

    public static Properties getProperties() throws UsbException, SecurityException {
        InputStream stream = UsbHostManager.class.getClassLoader().
            getResourceAsStream(JAVAX_USB_PROPERTIES_FILE);

        if (stream == null) {
            throw new UsbException("Unable to load configuration file '" + JAVAX_USB_PROPERTIES_FILE + "'.");
        }

        Properties properties;
        try {
            properties = new Properties();
            properties.load(stream);
        } catch (IOException e) {
            throw new UsbPlatformException("Error while reading configuration file.", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return properties;
    }
}
