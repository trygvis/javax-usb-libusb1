package no.hackaton.ftdi;

import static javax.usb.UsbConst.*;
import static no.hackaton.ftdi.Ftdi232Util.*;

import javax.usb.*;
import javax.usb.util.*;
import java.io.*;

public class Ftdi232 implements Closeable {
    private final UsbDevice device;
    private final UsbInterface usbInterface;
    private final UsbEndpoint controlEndpoint;
    private final UsbEndpoint inEndpoint;
    private final UsbEndpoint outEndpoint;

    public static final byte SET_MODEM_CONTROL_REQUEST = 1;
    public static final byte SET_BAUDRATE_REQUEST = 3;

    public Ftdi232(UsbDevice device) throws UsbException {
        this.device = device;

        // This is not implemented yet, but would be the correct way me thinks. - Trygve
         usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte)0);

//        UsbConfiguration configuration = device.getUsbConfiguration((byte) 0);
//        usbInterface = configuration.getUsbInterface((byte)0);
        usbInterface.claim();
        controlEndpoint = usbInterface.getUsbEndpoint((byte)0);
        inEndpoint = usbInterface.getUsbEndpoint((byte)0x02);
        outEndpoint = usbInterface.getUsbEndpoint((byte)0x81);
    }

    public void close() throws IOException {
        UsbUtil.close(device);
    }

    public void setBaudRate(int requestedBaudRate) throws UsbException {
        short value = (short) (calculateBaudRate(requestedBaudRate) >> 16);
        short index = 0;

        byte outRequestType = ENDPOINT_DIRECTION_OUT | REQUESTTYPE_TYPE_VENDOR | REQUESTTYPE_RECIPIENT_DEVICE;

        device.syncSubmit(device.createUsbControlIrp(outRequestType, SET_BAUDRATE_REQUEST, value, index));
    }

    public void setDtr(boolean b) throws UsbException {
        short value = (short)(0x0100 | (b ? 1 : 0));
        short index = 0;

        byte outRequestType = ENDPOINT_DIRECTION_OUT | REQUESTTYPE_TYPE_VENDOR | REQUESTTYPE_RECIPIENT_DEVICE;

        device.syncSubmit(device.createUsbControlIrp(outRequestType, SET_MODEM_CONTROL_REQUEST, value, index));

//        UsbPipe pipe = controlEndpoint.getUsbPipe();
//        try {
//            pipe.open();
//            UsbControlIrp irp = pipe.createUsbControlIrp(outRequestType, SET_MODEM_CONTROL_REQUEST, value, index);
//            irp.setData(new byte[0], 0, 0);
//            pipe.syncSubmit(irp);
//        } finally {
//            UsbUtil.close(pipe);
//        }
    }
}
