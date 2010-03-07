package javax.usb.impl;

import javax.usb.UsbConfigurationDescriptor;

import static javax.usb.UsbConst.DESCRIPTOR_TYPE_CONFIGURATION;

public class DefaultUsbConfigurationDescriptor extends DefaultUsbDescriptor implements UsbConfigurationDescriptor {
    public final byte bConfigurationValue;
    public final byte bmAttributes;
    public final byte bMaxPower;
    public final byte bNumInterfaces;
    public final byte iConfiguration;
    public final short wTotalLength;

    public DefaultUsbConfigurationDescriptor(byte bConfigurationValue, byte bmAttributes, byte bMaxPower, byte bNumInterfaces, byte iConfiguration, short wTotalLength) {
        super(DESCRIPTOR_TYPE_CONFIGURATION, (byte) 0); // TODO: correct length
        this.bConfigurationValue = bConfigurationValue;
        this.bmAttributes = bmAttributes;
        this.bMaxPower = bMaxPower;
        this.bNumInterfaces = bNumInterfaces;
        this.iConfiguration = iConfiguration;
        this.wTotalLength = wTotalLength;
    }

    public byte bConfigurationValue() {
        return bConfigurationValue;
    }

    public byte bmAttributes() {
        return bmAttributes;
    }

    public byte bMaxPower() {
        return bMaxPower;
    }

    public byte bNumInterfaces() {
        return bNumInterfaces;
    }

    public byte iConfiguration() {
        return iConfiguration;
    }

    public short wTotalLength() {
        return wTotalLength;
    }
}
