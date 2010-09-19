package no.hackaton.usbtools;

import org.junit.*;

public class LsusbTest {
    @Test
    public void testLsusb() throws Exception {
        System.setProperty("javax.usb.libusb.trace", "true");
        System.setProperty("javax.usb.libusb.debug", "3");

        lsusb.main(new String[0]);
    }
}
