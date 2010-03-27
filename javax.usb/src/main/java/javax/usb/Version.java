package javax.usb;

public class Version {
    public static String getApiVersion() {
        return "1.0.1";
    }

    public static String getUsbVersion() {
        return "1.1";
    }

    public static void main(String[] args) {
        System.out.println("javax.usb API version <" + getApiVersion() + ">");
        System.out.println("USB specification version <" + getUsbVersion() + ">)");
    }
}
