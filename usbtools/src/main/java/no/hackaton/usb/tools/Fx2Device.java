package no.hackaton.usb.tools;

import static java.lang.Long.*;

import javax.usb.*;
import javax.usb.extra.*;
import javax.usb.util.*;
import java.io.*;

/**
 * See TRM 2.3.11 "Firmware load" for the technical details on the USB commands.
 */
public class Fx2Device implements Closeable {
    public static final short CPUCS = (short) 0xE600;
    public static final short FX2_ID_VENDOR = 0x04b4;
    public static final short FX2_ID_PRODUCT = (short) 0x8613;

    private final UsbDevice device;

    public static Fx2Device findFx2Device(UsbHub hub) throws UsbException {
        UsbDevice device = ExtraUsbUtil.findUsbDevice(hub, FX2_ID_VENDOR, FX2_ID_PRODUCT);

        if (device == null) {
            return null;
        }

        return new Fx2Device(device/*, null*/);
    }

    public Fx2Device(UsbDevice device/*, UsbInterface usbInterface*/) {
        this.device = device;
    }

    public void close() throws IOException {
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

    public void write(int address, byte[] bytes) throws UsbException {
        write(address, bytes, 0, bytes.length);
    }

    public void write(int address, byte[] bytes, int offset, int length) throws UsbException {
        System.err.println("Fx2Device.write: address=" + toHexString(address) + ", bytes=" + bytes.length);
        byte bmRequestType = 0x40; // Vendor request, IN
        byte bRequest = (byte) 0xa0;
        short wValue = (short) address;
        short wIndex = 0;
        UsbControlIrp irp = device.createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
        irp.setData(bytes);
        irp.setOffset(offset);
        irp.setLength(length);
        device.syncSubmit(irp);
    }

    public byte[] read(int address, int count) throws UsbException {
        System.err.println("Fx2Device.read: address=" + toHexString(address) + ", count=" + count);
        byte bmRequestType = (byte) 0xc0; // Vendor request, OUT
        byte bRequest = (byte) 0xa0;
        short wValue = (short) address;
        short wIndex = 0;
        byte[] bytes = new byte[count];
        device.syncSubmit(new DefaultUsbControlIrp(bytes, 0, count, true, bmRequestType, bRequest, wValue, wIndex));

        return bytes;
    }

    public OutputStream toOutputStream() {
        return new OutputStream() {
            int address = 0;

            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
                try {
                    Fx2Device.this.write(address, bytes, offset, length);
                    address += length;
                } catch (UsbException e) {
                    throw new IOException(e.getMessage());
                }
            }

            @Override
            public void write(int b) throws IOException {
                try {
                    Fx2Device.this.write(address, new byte[]{(byte) b});
                    address++;
                } catch (UsbException e) {
                    throw new IOException(e.getMessage());
                }
            }
        };
    }
}
