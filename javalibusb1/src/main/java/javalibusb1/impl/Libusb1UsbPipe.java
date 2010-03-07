package javalibusb1.impl;

import javax.usb.UsbControlIrp;
import javax.usb.UsbEndpoint;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.event.UsbPipeListener;
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
        throw new RuntimeException("Not implemented");
    }

    public UsbIrp createUsbIrp() {
        throw new RuntimeException("Not implemented");
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
        throw new RuntimeException("Not implemented");
    }

    public void syncSubmit(List list) {
        throw new RuntimeException("Not implemented");
    }

    public void syncSubmit(UsbIrp irp) {
        throw new RuntimeException("Not implemented");
    }
}
