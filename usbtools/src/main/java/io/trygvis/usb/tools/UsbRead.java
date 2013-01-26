package io.trygvis.usb.tools;

import static javax.usb.UsbHostManager.*;

import javax.usb.*;

/**
 * Stream an endpoint to stdout.
 */
public class UsbRead {
    public static final short idVendor = 0x0547;
    public static final short idProduct = (short) 0xff01;

    public static void main(String[] args) throws Exception {
        String id = null;

        if (args.length > 0) {
            id = args[0];
        }

        UsbDevice device = UsbCliUtil.findDevice(getUsbServices().getRootUsbHub(), idVendor, idProduct, id);

        UsbInterface usbInterface = device.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0);
        usbInterface.claim();
        try {
            UsbEndpoint endpoint = usbInterface.getUsbEndpoint((byte) 0x88);
            UsbEndpointDescriptor endpointDescriptor = endpoint.getUsbEndpointDescriptor();

            byte[] data = new byte[endpointDescriptor.wMaxPacketSize()];
            UsbPipe pipe = endpoint.getUsbPipe();
            pipe.open();

            while (true) {
                int i = pipe.syncSubmit(data);
                HexFormatter.writeBytes(System.out, data, 0, i);
            }
        } finally {
            usbInterface.release();
        }
    }
}
