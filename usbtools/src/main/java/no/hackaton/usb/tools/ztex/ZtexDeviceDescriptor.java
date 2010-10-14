package no.hackaton.usb.tools.ztex;

import javax.usb.util.*;

public class ZtexDeviceDescriptor {
    public final byte size;
    public final byte version;
    public final String id;
    public final byte[] productId;
    public final byte fwVersion;
    public final byte intefaceVersion;
    public final byte[] intefaceCapabilities;

    public ZtexDeviceDescriptor(byte size, byte version, String id, byte[] productId, byte fwVersion, byte intefaceVersion, byte[] intefaceCapabilities) {
        this.size = size;
        this.version = version;
        this.id = id;
        this.productId = productId;
        this.fwVersion = fwVersion;
        this.intefaceVersion = intefaceVersion;
        this.intefaceCapabilities = intefaceCapabilities;
    }

    public String toString() {
        return "version=" + version + ", " +
            "id=" + id + ", " +
            "product id=" + UsbUtil.toHexString(productId[0]) + "." + UsbUtil.toHexString(productId[1]) + "." + UsbUtil.toHexString(productId[2]) + "." + UsbUtil.toHexString(productId[3]) + ", " +
            "firmware version=" + fwVersion + ", " +
            "interface version=" + intefaceVersion + ", " +
            "interface capabilities=" + UsbUtil.toHexString(intefaceCapabilities[0]) + UsbUtil.toHexString(intefaceCapabilities[1]) + UsbUtil.toHexString(intefaceCapabilities[2]) + UsbUtil.toHexString(intefaceCapabilities[3]) + UsbUtil.toHexString(intefaceCapabilities[4]) + UsbUtil.toHexString(intefaceCapabilities[5]);
    }
}
