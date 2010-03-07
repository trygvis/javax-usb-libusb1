package javax.usb.event;

public interface UsbPipeListener {
    void dataEventOccurred(UsbPipeDataEvent event);

    void errorEventOccurred(UsbPipeErrorEvent event);
}
