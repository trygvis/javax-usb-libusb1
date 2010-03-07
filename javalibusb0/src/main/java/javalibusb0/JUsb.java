package javalibusb0;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static javalibusb0.libusb0.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JUsb {

    public JUsb() {
//        simple_test();
        init();
//        init2(Libusb0Bus.class, Libusb0Bus[].class, Libusb0Device.class, Libusb0Device[].class);
    }

    public Iterable<Libusb0Bus> getBusses() {
        return unmodifiableList(asList(jusb_get_busses()));
    }

    public int findBusses() {
        return jusb_find_busses();
    }

    public int findDevices() {
        return jusb_find_devices();
    }
}
