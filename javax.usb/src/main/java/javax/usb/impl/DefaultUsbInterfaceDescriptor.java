package javax.usb.impl;

import javax.usb.UsbInterfaceDescriptor;

public class DefaultUsbInterfaceDescriptor implements UsbInterfaceDescriptor {
    public final byte bAlternateSetting;
    public final byte bInterfaceClass;
    public final byte bInterfaceNumber;
    public final byte bInterfaceProtocol;
    public final byte bInterfaceSubClass;
    public final byte bNumEndpoints;
    public final byte iInterface;

    public DefaultUsbInterfaceDescriptor(byte bAlternateSetting, byte bInterfaceClass, byte bInterfaceNumber, byte bInterfaceProtocol, byte bInterfaceSubClass, byte bNumEndpoints, byte iInterface) {
        this.bAlternateSetting = bAlternateSetting;
        this.bInterfaceClass = bInterfaceClass;
        this.bInterfaceNumber = bInterfaceNumber;
        this.bInterfaceProtocol = bInterfaceProtocol;
        this.bInterfaceSubClass = bInterfaceSubClass;
        this.bNumEndpoints = bNumEndpoints;
        this.iInterface = iInterface;
    }

    public byte bAlternateSetting() {
        return bAlternateSetting;
    }

    public byte bInterfaceClass() {
        return bInterfaceClass;
    }

    public byte bInterfaceNumber() {
        return bInterfaceNumber;
    }

    public byte bInterfaceProtocol() {
        return bInterfaceProtocol;
    }

    public byte bInterfaceSubClass() {
        return bInterfaceSubClass;
    }

    public byte bNumEndpoints() {
        return bNumEndpoints;
    }

    public byte iInterface() {
        return iInterface;
    }
}
