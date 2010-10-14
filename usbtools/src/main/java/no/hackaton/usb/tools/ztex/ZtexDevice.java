package no.hackaton.usb.tools.ztex;

import static java.lang.System.*;
import static no.hackaton.usb.tools.ztex.ZtexFpgaState.*;

import javax.usb.*;
import javax.usb.util.*;
import java.io.*;

public class ZtexDevice {
    private final UsbDevice usbDevice;
    public final ZtexDeviceDescriptor deviceDescriptor;

    public ZtexDevice(UsbDevice usbDevice, ZtexDeviceDescriptor deviceDescriptor) {
        this.usbDevice = usbDevice;
        this.deviceDescriptor = deviceDescriptor;
    }

    public static ZtexDevice createZtexDevice(UsbDevice usbDevice) throws UsbException {
        byte[] buf = new byte[100];

        int size = vendorRequest(usbDevice, (byte) 0x22, (short) 0, (short) 0, buf);

        if (size != 40) {
            throw new ZtexException("Invalid Ztex device descriptor: expected the descriptor to be 40 bytes, was " + size + ".");
        }

        try {
            byte[] productId = new byte[4];
            arraycopy(buf, 6, productId, 0, 4);
            byte[] intefaceCapabilities = new byte[6];
            arraycopy(buf, 12, intefaceCapabilities, 0, 6);

            ZtexDeviceDescriptor deviceDescriptor = new ZtexDeviceDescriptor(buf[0],
                buf[1],
                new String(buf, 2, 4, "ascii"),
                productId,
                buf[10],
                buf[11],
                intefaceCapabilities);

            return new ZtexDevice(usbDevice, deviceDescriptor);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // FPGA Configuration
    // -----------------------------------------------------------------------

    public ZtexFpgaState getFpgaState() throws UsbException {
        byte[] buf = new byte[100];
        vendorRequest(usbDevice, (byte) 0x30, (byte) 0, (byte) 0, buf);
        return ztexFpgaStateFromBytes(buf);
    }

    public void resetFpga() throws UsbException {
        vendorCommand(usbDevice, (byte) 0x31, (short) 0, (short) 0, new byte[0]);
    }

    public void sendFpgaData(byte[] bytes, int offset, int length) throws UsbException {
        for(int i = 0; i < 10; i++) {
            try {
                vendorCommand(usbDevice, (byte) 0x32, (short) 0, (short) 0, bytes, offset, length);
                if(i > 0) {
                    System.out.println("sendFpgaData: done after " + i + " retries.");
                }
                return;
            } catch (UsbException e) {
                System.out.println("sendFpgaData: Retrying...");
            }
        }

        throw new UsbException("Unable to send FPGA data");
    }

    public static int vendorCommand(UsbDevice usbDevice, byte cmd, short value, short index, byte[] buf) throws UsbException {
        return vendorCommand(usbDevice, cmd, value, index, buf, 0, buf.length);
    }

    public static int vendorCommand(UsbDevice usbDevice, byte cmd, short value, short index, byte[] buf, int offset, int length) throws UsbException {
        DefaultUsbControlIrp irp = new DefaultUsbControlIrp(buf, offset, length, false, (byte) 0x40, cmd, value, index);
        usbDevice.syncSubmit(irp);
        return irp.getActualLength();
    }

    public static int vendorRequest(UsbDevice usbDevice, byte request, short value, short index, byte[] buf) throws UsbException {
        return vendorRequest(usbDevice, request, value, index, buf, 0, buf.length);
    }

    public static int vendorRequest(UsbDevice usbDevice, byte request, short value, short index, byte[] buf, int offset, int length) throws UsbException {
        DefaultUsbControlIrp irp = new DefaultUsbControlIrp(buf, offset, length, false, (byte) 0xc0, request, value, index);
        usbDevice.syncSubmit(irp);
        return irp.getActualLength();
    }
}
