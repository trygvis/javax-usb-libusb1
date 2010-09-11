package no.hackaton.usbtools;

import static javax.usb.extra.ExtraUsbUtil.*;

import javax.usb.*;
import javax.usb.extra.*;

/**
 * Stream an endpoint to stdout.
 */
public class UsbRead {
    public static void main(String[] args) throws Exception {
//        System.setProperty(Libusb1UsbServices.JAVAX_USB_LIBUSB_TRACE_PROPERTY, "true");
//        System.setProperty(Libusb1UsbServices.JAVAX_USB_LIBUSB_DEBUG_PROPERTY, "3");
        UsbHub hub = UsbHostManager.getUsbServices().getRootUsbHub();

        short idVendor = 0x0547;
        short idProduct = (short) 0xff01;
        UsbDevice device = ExtraUsbUtil.findUsbDevice(hub, idVendor, idProduct);

        if (device == null) {
            System.out.println("Cound not find device " + deviceIdToString(idVendor, idProduct) + ".");
            return;
        }

        UsbInterface usbInterface = device.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0);
        usbInterface.claim();
        try {
            UsbEndpoint endpoint = usbInterface.getUsbEndpoint((byte) 0x88);
            UsbEndpointDescriptor endpointDescriptor = endpoint.getUsbEndpointDescriptor();

            System.out.println("endpointDescriptor.wMaxPacketSize() = " + endpointDescriptor.wMaxPacketSize());
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
