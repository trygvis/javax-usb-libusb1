package io.trygvis.usb.ftdi;

import javax.usb.*;

public class ReadSerial {
    public static void main(String[] args) throws Exception {
        UsbServices usbServices = UsbHostManager.getUsbServices();

        UsbDevice device = Ftdi232Util.findDevice(usbServices.getRootUsbHub());

        if(device == null) {
            System.err.println("Could not find appropriate FTDI device.");
            return;
        }

        Ftdi232 ftdi232 = new Ftdi232(device);

        ftdi232.setBaudRate(2400);
        ftdi232.setDtr(true);

        byte[] buffer = new byte[30];

        int i = 0;
        while(i++ < 100) {
            int count = ftdi232.read(buffer);
            System.out.println("Read " + count + " bytes.");

            for(int j = 0; j < count; j++) {
                char c = (char)buffer[j];
                if(Character.isDigit(c))
                    System.out.print(c);
            }
            System.out.println();
        }
        ftdi232.setDtr(false);
    }
}
