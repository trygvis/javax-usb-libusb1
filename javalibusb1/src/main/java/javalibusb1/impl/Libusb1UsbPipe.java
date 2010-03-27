package javalibusb1.impl;

import javax.usb.UsbControlIrp;
import javax.usb.UsbEndpoint;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.event.UsbPipeListener;
import javax.usb.util.DefaultUsbControlIrp;
import javax.usb.util.DefaultUsbIrp;
import java.util.List;

public class Libusb1UsbPipe implements UsbPipe {

    private final Libusb1UsbEndpoint endpoint;
    private boolean open;

    public Libusb1UsbPipe(Libusb1UsbEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    // -----------------------------------------------------------------------
    // UsbPipe Implementation
    // -----------------------------------------------------------------------

    public void abortAllSubmissions() {
        throw new RuntimeException("Not implemented");
    }

    public void addUsbPipeListener(UsbPipeListener listener) {
        throw new RuntimeException("Not implemented");
    }

    public UsbIrp asyncSubmit(byte[] data) {
        throw new RuntimeException("Not implemented");
    }

    public void asyncSubmit(List list) {
        throw new RuntimeException("Not implemented");
    }

    public void asyncSubmit(UsbIrp irp) {
        throw new RuntimeException("Not implemented");
    }

    public void close() {
        open = false;
    }

    public UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex) {
        return new DefaultUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);
    }

    public UsbIrp createUsbIrp() {
        return new DefaultUsbIrp();
    }

    public UsbEndpoint getUsbEndpoint() {
        return endpoint;
    }

    public boolean isActive() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void removeUsbPipeListener(UsbPipeListener listener) {
        throw new RuntimeException("Not implemented");
    }

    public int syncSubmit(byte[] data) {
        UsbIrp irp = new DefaultUsbIrp();
        irp.setData(data);
        return internalSyncSubmit(irp);
    }

    public void syncSubmit(List<UsbIrp> list) {
        for (UsbIrp usbIrp : list) {
            syncSubmit(usbIrp);
        }
    }

    public void syncSubmit(UsbIrp irp) {
        internalSyncSubmit(irp);
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private int internalSyncSubmit(UsbIrp irp) {
        if(irp.getData() == null) {
            throw new IllegalArgumentException("data == null");
        }

        if(irp.getUsbException() != null) {
            throw new IllegalArgumentException("usbException is not null");
        }

        if(irp.isComplete()) {
            throw new IllegalArgumentException("complete == true");
        }

        throw new RuntimeException("Not implemented");
    }
}
