package javax.usb.impl;

import javax.usb.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultUsbDescriptor implements UsbDescriptor {
    public final byte bDescriptorType;
    public final byte bLength;

    public DefaultUsbDescriptor(byte bDescriptorType, byte bLength) {
        this.bDescriptorType = bDescriptorType;
        this.bLength = bLength;
    }

    // -----------------------------------------------------------------------
    // UsbDescriptor Implementation
    // -----------------------------------------------------------------------

    public byte bDescriptorType() {
        return bDescriptorType;
    }

    public byte bLength() {
        return bLength;
    }
}
