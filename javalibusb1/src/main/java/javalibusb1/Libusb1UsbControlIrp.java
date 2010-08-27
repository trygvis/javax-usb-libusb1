package javalibusb1;

import javax.usb.*;

final class Libusb1UsbControlIrp extends Libusb1UsbIrp implements UsbControlIrp {
    
    private final UsbControlIrp irp;

    private Libusb1UsbControlIrp(UsbControlIrp irp) {
        super(irp);
        this.irp = irp;
    }

    public static Libusb1UsbControlIrp createControlIrp(UsbControlIrp irp) {
        if(irp instanceof Libusb1UsbControlIrp) {
            return (Libusb1UsbControlIrp) irp;
        }

        checkIrp(irp);
        checkControlIrp(irp);

        return new Libusb1UsbControlIrp(irp);
    }

    private static void checkControlIrp(UsbControlIrp irp) {
        // hm, what to check..
    }

    public byte bmRequestType() {
        return irp.bmRequestType();
    }

    public byte bRequest() {
        return irp.bRequest();
    }

    public short wIndex() {
        return irp.wIndex();
    }

    public short wValue() {
        return irp.wValue();
    }
}
