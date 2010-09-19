package no.hackaton.usb.ftdi;

import static java.lang.Thread.sleep;

public class ToggleDtr {
    public static void main(String[] args) throws Exception {
        UsbServices usbServices = UsbHostManager.getUsbServices();

        UsbDevice device = Ftdi232Util.findDevice(usbServices.getRootUsbHub());

        if(device == null) {
            System.err.println("Could not find appropriate FTDI device.");
            return;
        }

        Ftdi232 ftdi232 = new Ftdi232(device);

        System.out.println("Setting DRT");
        System.out.flush();
        ftdi232.setDtr(true);
        sleep(1000);
        System.out.println("Clearing DRT");
        System.out.flush();
        ftdi232.setDtr(false);
    }
}
