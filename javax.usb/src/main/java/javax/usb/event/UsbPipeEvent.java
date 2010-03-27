package javax.usb.event;

import javax.usb.*;
import java.util.*;

public class UsbPipeEvent extends EventObject {

    private UsbPipe source;
    private UsbIrp usbIrp;

    public UsbPipeEvent(UsbPipe source) {
        super(source);
        this.source = source;
    }

    public UsbPipeEvent(UsbPipe source, UsbIrp usbIrp) {
        super(source);
        this.source = source;
        this.usbIrp = usbIrp;
    }

    public UsbPipe getUsbPipe() {
        return source;
    }

    public boolean hasUsbIrp() {
        return usbIrp != null;
    }

    public UsbIrp getUsbIrp() {
        return usbIrp;
    }
}
