package javax.usb.util;

import javax.usb.UsbControlIrp;

public class DefaultUsbControlIrp extends DefaultUsbIrp implements UsbControlIrp {
    protected byte bmRequestType;

    protected byte bRequest;

    protected short wIndex;

    protected short wValue;

    public DefaultUsbControlIrp(byte[] data, int offset, int length, boolean acceptShortPacket, byte bmRequestType, byte bRequest, short wValue, short wIndex) {
        setData(data, offset, length);
        setAcceptShortPacket(acceptShortPacket);
        this.bmRequestType = bmRequestType;
        this.bRequest = bRequest;
        this.wIndex = wIndex;
        this.wValue = wValue;
    }

    public DefaultUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex) {
        this.bmRequestType = bmRequestType;
        this.bRequest = bRequest;
        this.wIndex = wIndex;
        this.wValue = wValue;
    }

    public byte bmRequestType() {
        return bmRequestType;
    }

    public byte bRequest() {
        return bRequest;
    }

    public short wIndex() {
        return wIndex;
    }

    public short wValue() {
        return wValue;
    }

    public short wLength() {
        return (short)length;
    }
}
