package no.hackaton.usbtools;

import static java.lang.Long.toHexString;
import static javax.usb.util.UsbUtil.unsignedInt;

import javax.usb.*;
import javax.usb.extra.*;
import javax.usb.util.*;
import java.io.*;

/**
 * See TRM 2.3.11 "Firmware load" for the technical details on the USB commands.
 */
public class Fx2Device implements Closeable {
    public static final short CPUCS = (short) 0xE600;

    private final UsbDevice device;
//    private final UsbInterface usbInterface;

    public static Fx2Device findFx2Device(UsbHub hub) throws UsbException {
        short idVendor = 0x04b4;
        short idProduct = (short) 0x8613;

        UsbDevice device = ExtraUsbUtil.findDevice(hub, idVendor, idProduct);

        if (device == null) {
            return null;
        }

//        UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);
//        usbInterface.claim();

        return new Fx2Device(device/*, null*/);
    }

    public Fx2Device(UsbDevice device/*, UsbInterface usbInterface*/) {
        this.device = device;
//        this.usbInterface = usbInterface;
    }

    public void close() throws IOException {
//        try {
//            usbInterface.release();
//        } catch (UsbException e) {
//            throw new IOException(e.getMessage());
//        }
    }

    public void setReset() throws UsbException {
        System.err.println("Fx2Device.setReset");
        // TODO: Read the value first before manipulating the value
        write(CPUCS, new byte[]{0x01});
    }

    public void releaseReset() throws UsbException {
        // TODO: Read the value first before manipulating the value
        write(CPUCS, new byte[]{0x00});
    }

    public void write(short address, byte[] bytes) throws UsbException {
        System.err.println("Fx2Device.write: address=" + toHexString(unsignedInt(address)) + ", bytes=" + bytes.length);
        byte bmRequestType = 0x40; // Vendor request, IN
        byte bRequest = (byte) 0xa0;
        short wValue = address;
        short wIndex = 0;
        UsbControlIrp irp = device.createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
        irp.setData(bytes);
        device.syncSubmit(irp);
    }

    public byte[] read(short address, int count) throws UsbException {
        System.err.println("Fx2Device.read: address=" + toHexString(unsignedInt(address)) + ", count=" + count);
        byte bmRequestType = (byte) 0xc0; // Vendor request, OUT
        byte bRequest = (byte) 0xa0;
        short wValue = address;
        short wIndex = 0;
        byte[] bytes = new byte[count];
        device.syncSubmit(new DefaultUsbControlIrp(bytes, 0, count, true, bmRequestType, bRequest, wValue, wIndex));

        return bytes;
    }
}
