package javax.usb;

import javax.usb.event.*;
import java.util.*;

public interface UsbPipe {
    void abortAllSubmissions() throws UsbNotActiveException, UsbNotOpenException, UsbDisconnectedException;

    void addUsbPipeListener(UsbPipeListener listener);

    UsbIrp asyncSubmit(byte[] data) throws UsbException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, UsbDisconnectedException;

    void asyncSubmit(List list) throws UsbException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, UsbDisconnectedException;

    void asyncSubmit(UsbIrp irp) throws UsbException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, UsbDisconnectedException;

    void close() throws UsbException, UsbNotActiveException, UsbNotOpenException, UsbDisconnectedException;

    UsbControlIrp createUsbControlIrp(byte bmRequestType, byte bRequest, short wValue, short wIndex);

    UsbIrp createUsbIrp();

    UsbEndpoint getUsbEndpoint();

    boolean isActive();

    boolean isOpen();

    void open() throws UsbException, UsbNotActiveException, UsbNotClaimedException, UsbDisconnectedException;

    void removeUsbPipeListener(UsbPipeListener listener);

    int syncSubmit(byte[] data) throws UsbException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, UsbDisconnectedException;

    void syncSubmit(List<UsbIrp> list) throws UsbException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, UsbDisconnectedException;

    void syncSubmit(UsbIrp irp) throws UsbException, UsbNotActiveException, UsbNotOpenException, IllegalArgumentException, UsbDisconnectedException;
}
