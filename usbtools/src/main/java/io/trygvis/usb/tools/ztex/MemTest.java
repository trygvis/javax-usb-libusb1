package io.trygvis.usb.tools.ztex;

import static java.util.Arrays.asList;
import static javax.usb.extra.ExtraUsbUtil.findUsbDevice;
import static io.trygvis.usb.tools.UsbCliUtil.listDevices;

import javax.usb.*;
import java.util.*;

public class MemTest {
    public static final short idVendor = (short)0x221a;
    public static final short idProduct = (short)0x0100;

    public static void main(String[] a) throws UsbException {
        UsbServices usbServices = UsbHostManager.getUsbServices();
        UsbHub hub = usbServices.getRootUsbHub();

        System.out.println("MemTest");

        List<String> args = new ArrayList<String>(asList(a));
        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String arg = it.next();
            if (arg.equals("--list")) {
                listDevices(hub, idVendor, idProduct);
                break;
            } else {
                UsbDevice usbDevice = findUsbDevice(hub, idVendor, idProduct);
                if (usbDevice == null) {
                    return;
                }
                commandPhase(usbDevice);
                break;
            }
        }
    }

    private static void commandPhase(UsbDevice usbDevice) {
        
    }
}
