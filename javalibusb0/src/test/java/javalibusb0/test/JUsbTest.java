package javalibusb0.test;

import javalibusb0.*;

class JUsbTest {

    public static void main(String[] args) throws Exception {
        JUsb usb = new JUsb();
        int busCount = usb.findBusses();
        int deviceCount = usb.findDevices();

        System.out.println("busCount = " + busCount);
        System.out.println("deviceCount = " + deviceCount);

        System.out.println("Busses:");
        for (Libusb0Bus usbBus : usb.getBusses()) {
            System.out.println("usbBus = " + usbBus);
            System.out.println("usbBus.location = " + usbBus.location);
            System.out.println("usbBus.#devices = " + usbBus.devices.length);
            for (Libusb0Device device : usbBus.devices) {
                System.out.println("device = " + device);
                System.out.println("device.filename = " + device.filename);
            }
        }
    }
}
