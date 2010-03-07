package javax.usb;

import javax.usb.event.UsbPipeListener;
import java.util.List;

public interface UsbPipe {
    void abortAllSubmissions();

    void addUsbPipeListener(UsbPipeListener listener);

    UsbIrp asyncSubmit(byte[] data);

    void asyncSubmit(List list);

    void asyncSubmit(UsbIrp irp);

    void close();

    UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex);

    UsbIrp createUsbIrp();

    UsbEndpoint getUsbEndpoint();

    boolean isActive();

    boolean isOpen();

    void open();

    void removeUsbPipeListener(UsbPipeListener listener);

    int syncSubmit(byte[] data);

    void syncSubmit(List list);

    void syncSubmit(UsbIrp irp);
}
