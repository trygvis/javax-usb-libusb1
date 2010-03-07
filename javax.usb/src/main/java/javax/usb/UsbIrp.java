package javax.usb;

public interface UsbIrp {
    void complete();

    boolean getAcceptShortPacket();

    int getActualLength();

    byte[] getData();

    int getLength();

    int getOffset();

    UsbException getUsbException();

    boolean isComplete();

    boolean isUsbException();

    void setAcceptShortPacket(boolean accept);

    void setActualLength(int length);

    void setComplete(boolean complete);

    void setData(byte[] data);

    void setData(byte[] data, int offset, int length);

    void setLength(int length);

    void setOffset(int offset);

    void setUsbException(UsbException usbException);

    void waitUntilComplete();

    void waitUntilComplete(long timeout);
}
