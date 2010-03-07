package javalibusb0;

public class Libusb0Bus {
    public final long location;
    public final Libusb0Device[] devices;

    public Libusb0Bus(long location, Libusb0Device[] devices) {
        this.location = location;
        this.devices = devices;
    }
}
