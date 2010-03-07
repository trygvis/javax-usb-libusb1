package javax.usb;

import java.util.List;

public interface UsbDevice {
    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    String getManufacturerString() throws UsbException;

    String getSerialNumberString() throws UsbException;

    String getProductString() throws UsbException;

    UsbStringDescriptor getUsbStringDescriptor(byte index);

    String getString(byte index) throws UsbException;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    UsbDeviceDescriptor getUsbDeviceDescriptor();

    boolean isUsbHub();

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

//    void addUsbDeviceListener(UsbDeviceListener listener);
//
//    void removeUsbDeviceListener(UsbDeviceListener listener);

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

//    UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex);
//
//    void asyncSubmit(List<UsbControlIrp> list);
//
//    void asyncSubmit(UsbControlIrp irp);
//
//    void syncSubmit(List<UsbControlIrp> list);
//
//    void syncSubmit(UsbControlIrp irp);
//
//    UsbPort getParentUsbPort();
//

    UsbConfiguration getActiveUsbConfiguration() throws UsbPlatformException;

    boolean containsUsbConfiguration(byte number) throws UsbPlatformException;

    UsbConfiguration getUsbConfiguration(byte number) throws UsbPlatformException;

    List<UsbConfiguration> getUsbConfigurations() throws UsbPlatformException;
}
