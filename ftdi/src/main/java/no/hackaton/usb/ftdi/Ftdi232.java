package no.hackaton.usb.ftdi;

import static javax.usb.UsbConst.*;
import static no.hackaton.usb.ftdi.Ftdi232Util.*;

import java.io.*;

public class Ftdi232 implements Closeable {
    private final UsbDevice device;
    private final UsbPipe in;
    private final UsbPipe out;

    public static final byte SET_MODEM_CONTROL_REQUEST = 1;
    public static final byte SET_BAUDRATE_REQUEST = 3;

    public Ftdi232(UsbDevice device) throws UsbException {
        this.device = device;

        // This is not implemented yet, but would be the correct way me thinks. - Trygve
        UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);

//        UsbConfiguration configuration = device.getUsbConfiguration((byte) 0);
//        usbInterface = configuration.getUsbInterface((byte)0);
        usbInterface.claim();
        // This honestly mixes "in" and "out", it should always be relative to the host
        in = usbInterface.getUsbEndpoint((byte) 0x02).getUsbPipe();
        out = usbInterface.getUsbEndpoint((byte) 0x81).getUsbPipe();
        in.open();
        out.open();
    }

    public void close() throws IOException {
        UsbUtil.close(in);
        UsbUtil.close(out);
        UsbUtil.close(device);
    }

    public void setBaudRate(int requestedBaudRate) throws UsbException {
        short value = (short) (calculateBaudRate(requestedBaudRate) >> 16);
        short index = 0;

        byte bmRequestType = ENDPOINT_DIRECTION_OUT | REQUESTTYPE_TYPE_VENDOR | REQUESTTYPE_RECIPIENT_DEVICE;

        device.syncSubmit(device.createUsbControlIrp(bmRequestType, SET_BAUDRATE_REQUEST, value, index));
    }

    public void setDtr(boolean b) throws UsbException {
        short value = (short) (0x0100 | (b ? 1 : 0));
        short index = 0;

        byte bmRequestType = ENDPOINT_DIRECTION_OUT | REQUESTTYPE_TYPE_VENDOR | REQUESTTYPE_RECIPIENT_DEVICE;

        device.syncSubmit(device.createUsbControlIrp(bmRequestType, SET_MODEM_CONTROL_REQUEST, value, index));
    }

    public int read(byte[] buffer) throws UsbException {
        UsbIrp irp = out.createUsbIrp();
        irp.setData(buffer);
        out.syncSubmit(irp);
        return irp.getActualLength();
    }
}
