package javax.usb.event;

import javax.usb.UsbException;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;

public class UsbPipeErrorEvent extends UsbPipeEvent {
    private UsbException usbException;

    public UsbPipeErrorEvent(UsbPipe source, UsbException usbException) {
        super(source);
        this.usbException = usbException;
    }

    public UsbPipeErrorEvent(UsbPipe source, UsbIrp usbIrp) {
        super(source, usbIrp);
    }

    public UsbException getUsbException() {
        return usbException;
    }
}
