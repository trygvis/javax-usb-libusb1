package javax.usb.event;

import javax.usb.UsbIrp;
import javax.usb.UsbPipe;

public class UsbPipeDataEvent extends UsbPipeEvent {

    private byte[] data;
    private int actualLength;

    public UsbPipeDataEvent(UsbPipe source, byte[] data, int actualLength) {
        super(source);
        this.data = data;
        this.actualLength = actualLength;
    }

    public UsbPipeDataEvent(UsbPipe source, UsbIrp usbIrp) {
        super(source, usbIrp);
    }

    public byte[] getData() {
        return data;
    }

    public int getActualLength() {
        return actualLength;
    }
}
