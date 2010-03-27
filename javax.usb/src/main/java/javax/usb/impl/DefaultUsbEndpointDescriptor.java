package javax.usb.impl;

import javax.usb.*;

public class DefaultUsbEndpointDescriptor extends DefaultUsbDescriptor implements UsbEndpointDescriptor {
    public final byte bEndpointAddress;
    public final byte bInterval;
    public final byte bmAttributes;
    public final short wMaxPacketSize;

    public DefaultUsbEndpointDescriptor(byte bEndpointAddress, byte bInterval, byte bmAttributes, short wMaxPacketSize) {
        super(UsbConst.DESCRIPTOR_TYPE_ENDPOINT, (byte)7);
        this.bEndpointAddress = bEndpointAddress;
        this.bInterval = bInterval;
        this.bmAttributes = bmAttributes;
        this.wMaxPacketSize = wMaxPacketSize;
    }

    public byte bEndpointAddress() {
        return bEndpointAddress;
    }

    public byte bInterval() {
        return bInterval;
    }

    public byte bmAttributes() {
        return bmAttributes;
    }

    public short wMaxPacketSize() {
        return wMaxPacketSize;
    }
}
