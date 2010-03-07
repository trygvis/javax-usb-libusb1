package javax.usb.event;

import javax.usb.UsbIrp;
import javax.usb.UsbPipe;

public class UsbPipeEvent {

    private UsbPipe source;
    private UsbIrp usbIrp;

    public UsbPipeEvent(UsbPipe source) {
        this.source = source;
    }

    public UsbPipeEvent(UsbPipe source, UsbIrp usbIrp) {
        this.source = source;
        this.usbIrp = usbIrp;
    }
}
