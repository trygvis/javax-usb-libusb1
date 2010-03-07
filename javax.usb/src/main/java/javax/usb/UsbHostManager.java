package javax.usb;

import java.io.*;
import java.util.*;

public class UsbHostManager {

    public static final String JAVAX_USB_USBSERVICES_PROPERTY = "javax.usb.usbservices";

    public static final String JAVAX_USB_PROPERTIES_FILE = "javax.usb.properties";

    private static UsbServices usbServices;

    public static synchronized UsbServices getUsbServices() throws UsbException {
        if (usbServices != null) {
            return usbServices;
        }

        InputStream stream = UsbHostManager.class.getClassLoader().
                getResourceAsStream(JAVAX_USB_PROPERTIES_FILE);

        if (stream == null) {
            throw new UsbException("Unable to load configuration file '" + JAVAX_USB_PROPERTIES_FILE + "'.");
        }

        String services;

        try {
            Properties properties = new Properties();
            properties.load(stream);
            services = properties.getProperty(JAVAX_USB_USBSERVICES_PROPERTY);

            if (services == null) {
                throw new UsbException("Missing required property '" + JAVAX_USB_USBSERVICES_PROPERTY + "' from configuration file.");
            }
        } catch (IOException e) {
            throw new UsbException("Error while reading configuration file.", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }

        // TODO: Use the thread's current context class loader?

        Class<?> usbServicesClass;

        try {
            usbServicesClass = UsbHostManager.class.getClassLoader().loadClass(services);

            usbServices = (UsbServices) usbServicesClass.newInstance();
            return usbServices;
        } catch (ClassNotFoundException e) {
            throw new UsbException("Unable to load UsbServices class '" + services + "'.", e);
        } catch (InstantiationException e) {
            throw new UsbException("Unable to instantiate class '" + services + "'.", e);
        } catch (IllegalAccessException e) {
            throw new UsbException("Unable to instantiate class '" + services + "'.", e);
        } catch (ClassCastException e) {
            throw new UsbException("Class " + services + " is not an instance of javax.usb.UsbServices.");
        }
    }
}
