package javax.usb.event;

import javax.usb.*;

public class UsbDeviceDataEvent extends UsbDeviceEvent {
    private final UsbControlIrp controlIrp;
    private final byte[] data;

    public UsbDeviceDataEvent(UsbDevice device, UsbControlIrp controlIrp) {
        super(device);
        this.controlIrp = controlIrp;

        data = null;
    }

    public byte[] getData() {
        return data;
    }

    public UsbControlIrp getUsbControlIrp() {
        return controlIrp;
    }
}
