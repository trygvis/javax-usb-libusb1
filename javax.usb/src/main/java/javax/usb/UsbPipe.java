package javax.usb;

import javax.usb.event.UsbPipeListener;
import java.util.List;

@SuppressWarnings({"DuplicateThrows"})
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

    void open() throws UsbException, UsbNotActiveException, UsbNotClaimedException, UsbDisconnectedException;

    void removeUsbPipeListener(UsbPipeListener listener);

    int syncSubmit(byte[] data) throws UsbException, UsbNotActiveException, UsbNotOpenException, java.lang.IllegalArgumentException;

    void syncSubmit(List<UsbIrp> list) throws UsbException, UsbNotActiveException, UsbNotOpenException, java.lang.IllegalArgumentException;

    void syncSubmit(UsbIrp irp) throws UsbException, UsbNotActiveException, UsbNotOpenException, java.lang.IllegalArgumentException;
}
