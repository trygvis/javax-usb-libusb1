package javax.usb.impl;

import javax.usb.*;

public class DefaultUsbDeviceDescriptor extends DefaultUsbDescriptor implements UsbDeviceDescriptor {

    public final short bcdUSB;

    public final byte bDeviceClass;

    public final byte bDeviceSubClass;

    public final byte bDeviceProtocol;

    public final byte bMaxPacketSize0;

    public final short idVendor;

    public final short idProduct;

    public final short bcdDevice;

    public final byte iManufacturer;

    public final byte iProduct;

    public final byte iSerialNumber;

    public final byte bNumConfigurations;

    public DefaultUsbDeviceDescriptor(short bcdUSB, byte bDeviceClass, byte bDeviceSubClass, byte bDeviceProtocol,
                                      byte bMaxPacketSize0, short idVendor, short idProduct, short bcdDevice,
                                      byte iManufacturer, byte iProduct, byte iSerialNumber, byte bNumConfigurations) {
        super(UsbConst.DESCRIPTOR_TYPE_DEVICE, (byte)18);
        this.bcdUSB = bcdUSB;
        this.bDeviceClass = bDeviceClass;
        this.bDeviceSubClass = bDeviceSubClass;
        this.bDeviceProtocol = bDeviceProtocol;
        this.bMaxPacketSize0 = bMaxPacketSize0;
        this.idVendor = idVendor;
        this.idProduct = idProduct;
        this.bcdDevice = bcdDevice;
        this.iManufacturer = iManufacturer;
        this.iProduct = iProduct;
        this.iSerialNumber = iSerialNumber;
        this.bNumConfigurations = bNumConfigurations;
    }

    // -----------------------------------------------------------------------
    // UsbDeviceDescriptor Implementation
    // -----------------------------------------------------------------------

    public short bcdUSB() {
        return bcdUSB;
    }

    public byte bDeviceClass() {
        return bDeviceClass;
    }

    public byte bDeviceSubClass() {
        return bDeviceSubClass;
    }

    public byte bDeviceProtocol() {
        return bDeviceProtocol;
    }

    public byte bMaxPacketSize0() {
        return bMaxPacketSize0;
    }

    public short idVendor() {
        return idVendor;
    }

    public short idProduct() {
        return idProduct;
    }

    public short bcdDevice() {
        return bcdDevice;
    }

    public byte iManufacturer() {
        return iManufacturer;
    }

    public byte iProduct() {
        return iProduct;
    }

    public byte iSerialNumber() {
        return iSerialNumber;
    }

    public byte bNumConfigurations() {
        return bNumConfigurations;
    }
}
