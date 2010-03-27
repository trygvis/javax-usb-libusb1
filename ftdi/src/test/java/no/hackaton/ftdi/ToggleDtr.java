package no.hackaton.ftdi;

import static java.lang.Thread.sleep;

import javax.usb.*;

public class ToggleDtr {
    public static void main(String[] args) throws Exception {
        UsbServices usbServices = UsbHostManager.getUsbServices();

        UsbDevice device = Ftdi232Util.findDevice(usbServices.getRootUsbHub());

        if(device == null) {
            System.err.println("Could not find appropriate FTDI device.");
            return;
        }

        Ftdi232 ftdi232 = new Ftdi232(device);

        ftdi232.setDtr(true);
        sleep(1000);
        ftdi232.setDtr(false);
    }
}
