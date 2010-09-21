package no.hackaton.usb.tools;

import static java.lang.Byte.parseByte;
import static java.lang.Integer.parseInt;
import static javax.usb.UsbHostManager.*;
import static no.hackaton.usb.tools.HexFormatter.writeBytes;
import static no.hackaton.usb.tools.UsbCliUtil.findDevice;

import javax.usb.*;

/**
 * Stream stdin to and endpoint and an endpoint to stdout.
 *
 * For now it required that the enpoints have the same packet size and that
 * the device sends one packet for each received.
 */
public class UsbEcho {
    public static final short idVendor = 0x0547;
    public static final short idProduct = (short) 0xff01;

    public static void main(String[] args) throws Exception {
        String id = null;

        byte configuration = 1;
        byte interfaceId = 0;

        Byte inEp = null;
        Byte outEp = null;

        for (String arg : args) {
            if(arg.startsWith("--id=")) {
                id = arg.substring(5);
            }
            else if(arg.startsWith("--in=")) {
                inEp = parseByte(arg.substring(5));
            }
            else if(arg.startsWith("--out=")) {
                outEp = parseByte(arg.substring(6));
            }
        }

        if(inEp == null || outEp == null) {
            System.err.println("Usage: --in=<in ep> --out=<out ep> [--id=VVVV:PPPP | --id=path]");
            return;
        }

        UsbDevice device = findDevice(getUsbServices().getRootUsbHub(), idVendor, idProduct, id);
        if(device == null) {
            return;
        }

        UsbInterface usbInterface = device.getUsbConfiguration(configuration).getUsbInterface(interfaceId);
        usbInterface.claim();
        try {
            UsbEndpoint outEndpoint = usbInterface.getUsbEndpoint(outEp);
            UsbEndpointDescriptor outED = outEndpoint.getUsbEndpointDescriptor();

            UsbEndpoint inEndpoint = usbInterface.getUsbEndpoint((byte) (0x80 | inEp));
            UsbEndpointDescriptor inED = inEndpoint.getUsbEndpointDescriptor();

            if(outED.wMaxPacketSize() != inED.wMaxPacketSize()) {
                System.err.println("The max packet size of out and in endpoint has to be equal.");
                return;
            }

            byte[] data = new byte[outED.wMaxPacketSize()];

            UsbPipe outPipe = outEndpoint.getUsbPipe();
            outPipe.open();

            UsbPipe inPipe = inEndpoint.getUsbPipe();
            inPipe.open();

            int read = System.in.read(data);

            while (read != -1) {
                System.out.println("Sending " + read + " bytes...");
                writeBytes(System.out, data, 0, read);

                UsbIrp irp = outPipe.createUsbIrp();
                irp.setData(data, 0, read);
                outPipe.syncSubmit(irp);

                if(irp.getActualLength() != read) {
                    // This is probably not an error condition.
                    System.out.println("WARNING: Of a packet with " + read + " bytes, the device only accepted " + irp.getActualLength() + " bytes.");
                }

                int r = inPipe.syncSubmit(data);
                System.out.println("Read " + r + " bytes...");
                writeBytes(System.out, data, 0, r);

                read = System.in.read(data);
            }
        } finally {
            usbInterface.release();
        }
    }
}
