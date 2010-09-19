package no.hackaton.usb.tools;

import static java.lang.Thread.sleep;
import static java.util.Arrays.*;
//import javalibusb1.Libusb1UsbServices;
import static javax.usb.extra.ExtraUsbUtil.*;
import static javax.usb.util.UsbUtil.*;
import static no.hackaton.usb.tools.Fx2Device.*;
import no.hackaton.usb.tools.IntelHex.*;
import static no.hackaton.usb.tools.IntelHex.*;
import static no.hackaton.usb.tools.IntelHex.RecordType.*;

import javax.usb.*;
import java.io.*;
import java.util.ArrayList;
import java.util.*;

public class Fx2Programmer {
    public static void main(String[] a) throws Exception {
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
            else {
                // TODO: Parse out --id=VVVV.PPPP[.N] and --device=[device path]
                commandPhase(args, hub, idVendor, idProduct);
                break;
            }
        }
    }

    private static void commandPhase(List<String> args, UsbHub hub, short idVendor, short idProduct) throws Exception {
        UsbDevice usbDevice = findUsbDevice(hub, idVendor, idProduct);

        if(usbDevice == null) {
            System.err.println("Could not find fx2 device.");
            return;
        }

        Fx2Device fx2 = new Fx2Device(usbDevice);

        for (String arg : args) {
            System.out.println("arg = " + arg);
            if (arg.equals("reset")) {
                doReset(fx2);
            } else if (arg.equals("run")) {
                doRun(fx2);
            } else if (arg.startsWith("prg:")) {
                File file = new File(arg.substring(4));

                if (!file.canRead()) {
                    System.out.println("Can't read file: " + file);
                    break;
                }

                doProgramDevice(file, fx2);
            } else if (arg.startsWith("delay:")) {
                doDelay(Long.parseLong(arg.substring(6)));
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
        // I think this is required by the device
        device.setReset();

        // TODO: Pack several packets from the hex file into a bigger transfer.

        for (IntelHexPacket packet : openIntelHexFile(file)) {
            if (packet.recordType.equals(DATA)) {
                device.write(packet.address, packet.data);
            } else if (packet.recordType.equals(END_OF_FILE)) {
                break;
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
