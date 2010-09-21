package javalibusb1;

import javax.usb.*;

public class Libusb1UsbIrp implements UsbIrp {
    private final UsbIrp irp;

    protected Libusb1UsbIrp(UsbIrp irp) {
        this.irp = irp;
    }

    public static Libusb1UsbIrp createIrp(UsbIrp irp) {
        if(irp instanceof Libusb1UsbIrp) {
            return (Libusb1UsbIrp) irp;
        }

        checkIrp(irp);

        return new Libusb1UsbIrp(irp);
    }

    public static void checkIrp(UsbIrp irp) {
        if (irp == null) {
            throw new IllegalArgumentException("irp");
        }

        if (irp.getData() == null) {
            throw new IllegalArgumentException("data == null");
        }

        if (irp.getUsbException() != null) {
            throw new IllegalArgumentException("usbException is not null");
        }

        if (irp.isComplete()) {
            throw new IllegalArgumentException("complete == true");
        }

        if(irp.getLength() >= 65536) {
            throw new IllegalArgumentException("irp.length > 64k");
        }
    }

    public void complete() {
        irp.complete();
    }

    public boolean getAcceptShortPacket() {
        return irp.getAcceptShortPacket();
    }

    public int getActualLength() {
        return irp.getActualLength();
    }

    public byte[] getData() {
        return irp.getData();
    }

    public int getLength() {
        return irp.getLength();
    }

    public int getOffset() {
        return irp.getOffset();
    }

    public UsbException getUsbException() {
        return irp.getUsbException();
    }

    public boolean isComplete() {
        return irp.isComplete();
    }

    public boolean isUsbException() {
        return irp.isUsbException();
    }

    public void setAcceptShortPacket(boolean accept) {
        irp.setAcceptShortPacket(accept);
    }

    public void setActualLength(int length) {
        irp.setActualLength(length);
    }

    public void setComplete(boolean complete) {
        irp.setComplete(complete);
    }

    public void setData(byte[] data) {
        irp.setData(data);
    }

    public void setData(byte[] data, int offset, int length) {
        irp.setData(data, offset, length);
    }

    public void setLength(int length) {
        irp.setLength(length);
    }

    public void setOffset(int offset) {
        irp.setOffset(offset);
    }

    public void setUsbException(UsbException usbException) {
        irp.setUsbException(usbException);
    }

    public void waitUntilComplete() {
        irp.waitUntilComplete();
    }

    public void waitUntilComplete(long timeout) {
        irp.waitUntilComplete(timeout);
    }
}
