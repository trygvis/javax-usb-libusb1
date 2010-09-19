package no.hackaton.usb.tools;

import static java.lang.Thread.sleep;
import static java.util.Arrays.*;
//import javalibusb1.Libusb1UsbServices;
import static javax.usb.extra.ExtraUsbUtil.*;
import static javax.usb.util.UsbUtil.*;
import static no.hackaton.usb.tools.Fx2Device.*;
import no.hackaton.usb.tools.IntelHex.*;
import static no.hackaton.usb.tools.IntelHex.RecordType.*;
import static no.hackaton.usb.tools.IntelHex.*;

import javax.usb.*;
import java.io.*;
import java.util.ArrayList;
import java.util.*;

public class Fx2Programmer {
    public static void main(String[] a) throws Exception {
//        System.setProperty(Libusb1UsbServices.JAVAX_USB_LIBUSB_TRACE_PROPERTY, "true");
        UsbServices usbServices = UsbHostManager.getUsbServices();
        UsbHub hub = usbServices.getRootUsbHub();

        short idVendor = FX2_ID_VENDOR;
        short idProduct = FX2_ID_PRODUCT;

        List<String> args = new ArrayList<String>();
        args.addAll(asList(a));

        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String arg = it.next();
            if (arg.equals("--list")) {
                doList("0", hub, idVendor, idProduct);
                break;
            }
            // TODO: Parse out --id=VVVV.PPPP[.N] and --device=[device path]
            else {
                commandPhase(args, hub, idVendor, idProduct);
            }
        }
    }

    private static void commandPhase(List<String> args, UsbHub hub, short idVendor, short idProduct) throws Exception {
        Fx2Device device = new Fx2Device(findUsbDevice(hub, idVendor, idProduct));

        for (String arg : args) {
            if (arg.equals("reset")) {
                doReset(device);
            } else if (arg.equals("run")) {
                doRun(device);
            } else if (arg.startsWith("prg:")) {
                File file = new File(arg.substring(4));

                if (!file.canRead()) {
                    System.out.println("Can't read file: " + file);
                    break;
                }

                doProgramDevice(file, device);
            } else if (arg.startsWith("delay:")) {
                long delay = Long.parseLong(arg.substring(6));
                doDelay(delay);
            } else {
                System.out.println("Unknown command: " + arg);
                break;
            }
        }
    }

    private static void doReset(Fx2Device device) throws UsbException {
        device.setReset();
    }

    private static void doRun(Fx2Device device) throws UsbException {
        device.releaseReset();
    }

    private static void doProgramDevice(File file, Fx2Device device) throws IOException, UsbException {
        device.setReset(); // I think this is required
        for (IntelHexPacket packet : openIntelHexFile(file)) {
            if (packet.recordType.equals(DATA)) {
                device.write(packet.address, packet.data);
            } else {
                System.out.println("Unknown packet type: " + packet.recordType + ".");
            }
        }
    }

    private static void doDelay(long delay) throws InterruptedException {
        sleep(delay);
    }

    private static void doList(String prefix, UsbHub hub, short idVendor, short idProduct) {
        List<UsbDevice> list = hub.getAttachedUsbDevices();
        for (int i = 0; i < list.size(); i++) {
            UsbDevice device = list.get(i);
            if (device.isUsbHub()) {
                doList(prefix + "." + i, (UsbHub) device, idVendor, idProduct);
            } else {
                UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();

                System.err.print(prefix + "." + i + " " + toHexString(deviceDescriptor.idVendor()) + ":" + toHexString(deviceDescriptor.idProduct()));

                if (isUsbDevice(deviceDescriptor, idVendor, idProduct)) {
                    System.err.print(" (Unconfigured FX2)");
                }
                System.err.println();
            }
        }
    }
}
