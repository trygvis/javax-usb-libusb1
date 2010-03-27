package javax.usb.util;

import javax.usb.UsbException;
import javax.usb.UsbIrp;

public class DefaultUsbIrp implements UsbIrp {
    protected boolean acceptShortPacket;
    protected int actualLength;
    protected boolean complete;
    protected byte[] data;
    protected int length;
    protected int offset;
    protected UsbException usbException;

    private final Object lock = new Object();

    public DefaultUsbIrp() {
    }

    public DefaultUsbIrp(byte[] data) {
        setData(data);
    }

    public DefaultUsbIrp(byte[] data, int offset, int length, boolean acceptShortPacket) {
        setData(data, offset, length);
        this.acceptShortPacket = acceptShortPacket;
    }

    public boolean getAcceptShortPacket() {
        return acceptShortPacket;
    }

    public void setAcceptShortPacket(boolean acceptShortPacket) {
        this.acceptShortPacket = acceptShortPacket;
    }

    public int getActualLength() {
        return actualLength;
    }

    public void setActualLength(int actualLength) throws IllegalArgumentException{
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }

        this.actualLength = actualLength;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) throws IllegalArgumentException {
        setData(data, 0, data.length);
    }

    public void setData(byte[] data, int offset, int length) throws IllegalArgumentException {
        this.data = data;
        setOffset(offset);
        setLength(length);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) throws IllegalArgumentException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        this.offset = offset;
    }

    public UsbException getUsbException() {
        return usbException;
    }

    public void setUsbException(UsbException usbException) {
        this.usbException = usbException;
    }

    public boolean isUsbException() {
        return usbException != null;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void complete() {
        this.complete = true;

        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void waitUntilComplete() {
        while(!isComplete()){
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public void waitUntilComplete(long timeout) {
        long start = System.currentTimeMillis();

        while(!isComplete() && (System.currentTimeMillis() - start) < timeout){
            try {
                synchronized (lock) {
                    lock.wait(timeout);
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
