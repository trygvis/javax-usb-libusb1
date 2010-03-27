package javax.usb.event;

import javax.usb.*;

public class UsbDeviceErrorEvent extends UsbDeviceEvent {
    private final UsbException usbException;
    private final UsbControlIrp usbControlIrp;

    public UsbDeviceErrorEvent(UsbDevice usbDevice, UsbControlIrp usbControlIrp) {
        super(usbDevice);
        usbException = null;
        this.usbControlIrp = usbControlIrp;
    }

    public UsbException getUsbException() {
        return usbException;
    }

    public UsbControlIrp getUsbControlIrp() {
        return usbControlIrp;
    }
}
