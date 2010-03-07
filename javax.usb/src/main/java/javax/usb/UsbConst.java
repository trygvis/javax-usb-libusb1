package javax.usb;

public interface UsbConst {
    byte DESCRIPTOR_TYPE_DEVICE = 1;
    byte DESCRIPTOR_TYPE_CONFIGURATION = 2;
    byte DESCRIPTOR_TYPE_STRING = 3;
    byte DESCRIPTOR_TYPE_INTERFACE = 4;
    byte DESCRIPTOR_TYPE_ENDPOINT = 5;

    /**
     * @see @{link http://www.usb.org/developers/defined_class/#BaseClass09h}
     */
    byte HUB_CLASSCODE = 9;
}
