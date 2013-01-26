package io.trygvis.usb.tools;

import static java.lang.Byte.parseByte;
import static java.lang.Integer.parseInt;
import static javax.usb.UsbHostManager.*;
import static io.trygvis.usb.tools.UsbCliUtil.findDevice;

import javax.usb.*;

/**
 * Stream an endpoint from stdin.
 */
public class UsbWrite {
    public static final short idVendor = 0x0547;
    public static final short idProduct = (short) 0xff01;

    public static void main(String[] args) throws Exception {
        String id = null;
        Byte endpointId = null;

        System.setProperty("javax.usb.libusb.trace", "true");

        for (String arg : args) {
            if(arg.startsWith("--id=")) {
                id = arg.substring(5);
            }
            else if(arg.startsWith("--ep=")) {
                endpointId = parseByte(arg.substring(5));
            }
        }

        if(id == null || endpointId == null) {
            System.err.println("Usage: --ep=<ep> --id=VVVV:PPPP");
            return;
        }

        UsbDevice device = findDevice(getUsbServices().getRootUsbHub(), idVendor, idProduct, id);

        if(device == null) {
            return;
        }

        UsbInterface usbInterface = device.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0);
        usbInterface.claim();
        try {
            UsbEndpoint endpoint = usbInterface.getUsbEndpoint(endpointId);
            if(endpoint == null) {
                System.err.println("No such endpoint " + endpointId + ".");
                return;
            }
            UsbEndpointDescriptor endpointDescriptor = endpoint.getUsbEndpointDescriptor();
            System.out.println("endpointDescriptor.wMaxPacketSize() = " + endpointDescriptor.wMaxPacketSize());

            byte[] data = new byte[endpointDescriptor.wMaxPacketSize()];
            UsbPipe pipe = endpoint.getUsbPipe();
            pipe.open();

            while (true) {
                int i = System.in.read(data);
                if(i == -1) {
                    break;
                }

                HexFormatter.writeBytes(System.out, data, 0, i);

                UsbIrp irp = pipe.createUsbIrp();
                irp.setData(data, 0, i);
                System.out.println("Writing");
                pipe.syncSubmit(irp);
                System.out.println("Done");
            }
        } finally {
            usbInterface.release();
        }
    }
}
