package no.hackaton.usb.tools;

import static java.lang.Thread.*;
import static java.util.Arrays.*;
import static no.hackaton.usb.tools.Fx2Device.*;
import no.hackaton.usb.tools.IntelHex.*;
import static no.hackaton.usb.tools.IntelHex.RecordType.*;
import static no.hackaton.usb.tools.IntelHex.openIntelHexFile;
import static no.hackaton.usb.tools.UsbCliUtil.*;

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
        String id = null;

        List<String> args = new ArrayList<String>(asList(a));
        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String arg = it.next();
            if (arg.equals("--list")) {
                listDevices(hub, idVendor, idProduct);
                break;
            } else if (arg.startsWith("--id=")) {
                id = arg.substring(5);
            } else {
                UsbDevice usbDevice = findDevice(hub, idVendor, idProduct, id);
                if (usbDevice == null) {
                    return;
                }
                commandPhase(usbDevice, args);
                break;
            }
        }
    }

    private static void commandPhase(UsbDevice usbDevice, List<String> args) throws Exception {

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
        long startT = System.currentTimeMillis();
        // I think this is required by the device
        device.setReset();

        // TODO: Pack several packets from the hex file into a bigger transfer.

        Byte[] buf = new Byte[65536];

        int packetCount = 0;
        for (IntelHexPacket packet : openIntelHexFile(file)) {
            if (packet.recordType.equals(DATA)) {
//                device.write(packet.address, packet.data);

                for(int i = 0; i < packet.data.length; i++) {
                    buf[packet.address + i] = packet.data[i];
                }
                packetCount++;
            } else if (packet.recordType.equals(END_OF_FILE)) {
                break;
            } else {
                System.out.println("Unknown packet type: " + packet.recordType + ".");
            }
        }

        // Send packets
        int writeCount = 0;
        byte[] bytes = new byte[buf.length];
        for (int i = 0; i < bytes.length;) {
            // Search for next byte
            if(buf[i] == null) {
                i++;
                continue;
            }

            int start = i;
            int length = 0;
            while(i < bytes.length) {
                bytes[length++] = buf[i];
                i++;
                if(length == 1024 || buf[i] == null) {
                    break;
                }
            }

            device.write(start, bytes, 0, length);
            writeCount++;
        }

        System.out.println("IHX file had " + packetCount + " packets, did " + writeCount + " writes");
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - startT));
    }

    private static void doDelay(long delay) throws InterruptedException {
        sleep(delay);
    }
}
