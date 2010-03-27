package javax.usb.event;

import java.util.*;

public interface UsbPipeListener extends EventListener {
    void dataEventOccurred(UsbPipeDataEvent event);

    void errorEventOccurred(UsbPipeErrorEvent event);
}
