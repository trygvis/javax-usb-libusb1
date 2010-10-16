package no.hackaton.usb.tools;

import static javax.usb.UsbHostManager.*;
import static no.hackaton.usb.tools.UsbCliUtil.findDevice;

import javax.usb.*;

/**
 * Stream an endpoint from stdin.
 */
public class UsbWrite {
    public static final short idVendor = 0x0547;
    public static final short idProduct = (short) 0xff01;

    public static void main(String[] args) throws Exception {
        String id = null;

        if (args.length > 0) {
            id = args[0];
        }

        UsbDevice device = findDevice(getUsbServices().getRootUsbHub(), idVendor, idProduct, id);

        if(device == null) {
            return;
        }

        UsbInterface usbInterface = device.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0);
        usbInterface.claim();
        try {
            UsbEndpoint endpoint = usbInterface.getUsbEndpoint((byte) 0x04);
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
            }
        } finally {
            usbInterface.release();
        }
    }
}
