#include <libusb.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

const int vendor_id = 0x0403;
const int product_id = 0x6001;
const int interface_no = 0;

// in ep = 0x02, out ep = 0x81

libusb_context *context;
libusb_device *dev; 
libusb_device_handle *handle; 
struct libusb_config_descriptor *config;
struct libusb_device_descriptor desc;

void assert_libusb_p(void *err) {
    if(err == NULL) {
        fprintf(stderr, "ptr == NULL\n");
        exit(1);
    }
}

void assert_libusb(int err) {
    switch(err) {
        case 0: return;
        case LIBUSB_ERROR_IO: fprintf(stderr, "LIBUSB_ERROR_IO\n"); break;
        case LIBUSB_ERROR_INVALID_PARAM: fprintf(stderr, "LIBUSB_ERROR_INVALID_PARAM\n"); break;
        case LIBUSB_ERROR_ACCESS: fprintf(stderr, "LIBUSB_ERROR_ACCESS\n"); break;
        case LIBUSB_ERROR_NO_DEVICE: fprintf(stderr, "LIBUSB_ERROR_NO_DEVICE\n"); break;
        case LIBUSB_ERROR_NOT_FOUND: fprintf(stderr, "LIBUSB_ERROR_NOT_FOUND\n"); break;
        case LIBUSB_ERROR_BUSY: fprintf(stderr, "LIBUSB_ERROR_BUSY\n"); break;
        case LIBUSB_ERROR_TIMEOUT: fprintf(stderr, "LIBUSB_ERROR_TIMEOUT\n"); break;
        case LIBUSB_ERROR_OVERFLOW: fprintf(stderr, "LIBUSB_ERROR_OVERFLOW\n"); break;
        case LIBUSB_ERROR_PIPE: fprintf(stderr, "LIBUSB_ERROR_PIPE\n"); break;
        case LIBUSB_ERROR_INTERRUPTED: fprintf(stderr, "LIBUSB_ERROR_INTERRUPTED\n"); break;
        case LIBUSB_ERROR_NO_MEM: fprintf(stderr, "LIBUSB_ERROR_NO_MEM\n"); break;
        case LIBUSB_ERROR_NOT_SUPPORTED: fprintf(stderr, "LIBUSB_ERROR_NOT_SUPPORTED\n"); break;
        case LIBUSB_ERROR_OTHER: fprintf(stderr, "LIBUSB_ERROR_OTHER\n"); break;
        default: fprintf(stderr, "Unknown error: %d\n", err);
    }
    exit(1);
}

enum ftdi_chip_type { TYPE_AM=0, TYPE_BM=1, TYPE_2232C=2, TYPE_R=3, TYPE_2232H=4, TYPE_4232H=5 };

enum ftdi_chip_type type;
int ftdi_index = 1;

static int ftdi_convert_baudrate(int baudrate, unsigned short *value, unsigned short *index)
{
    static const char am_adjust_up[8] = {0, 0, 0, 1, 0, 3, 2, 1};
    static const char am_adjust_dn[8] = {0, 0, 0, 1, 0, 1, 2, 3};
    static const char frac_code[8] = {0, 3, 2, 4, 1, 5, 6, 7};
    int divisor, best_divisor, best_baud, best_baud_diff;
    unsigned long encoded_divisor;
    int i;

    if (baudrate <= 0)
    {
        // Return error
        return -1;
    }

    divisor = 24000000 / baudrate;

    if (type == TYPE_AM)
    {
        // Round down to supported fraction (AM only)
        divisor -= am_adjust_dn[divisor & 7];
    }

    // Try this divisor and the one above it (because division rounds down)
    best_divisor = 0;
    best_baud = 0;
    best_baud_diff = 0;
    for (i = 0; i < 2; i++)
    {
        int try_divisor = divisor + i;
        int baud_estimate;
        int baud_diff;

        // Round up to supported divisor value
        if (try_divisor <= 8)
        {
            // Round up to minimum supported divisor
            try_divisor = 8;
        }
        else if (type != TYPE_AM && try_divisor < 12)
        {
            // BM doesn't support divisors 9 through 11 inclusive
            try_divisor = 12;
        }
        else if (divisor < 16)
        {
            // AM doesn't support divisors 9 through 15 inclusive
            try_divisor = 16;
        }
        else
        {
            if (type == TYPE_AM)
            {
                // Round up to supported fraction (AM only)
                try_divisor += am_adjust_up[try_divisor & 7];
                if (try_divisor > 0x1FFF8)
                {
                    // Round down to maximum supported divisor value (for AM)
                    try_divisor = 0x1FFF8;
                }
            }
            else
            {
                if (try_divisor > 0x1FFFF)
                {
                    // Round down to maximum supported divisor value (for BM)
                    try_divisor = 0x1FFFF;
                }
            }
        }
        // Get estimated baud rate (to nearest integer)
        baud_estimate = (24000000 + (try_divisor / 2)) / try_divisor;
        // Get absolute difference from requested baud rate
        if (baud_estimate < baudrate)
        {
            baud_diff = baudrate - baud_estimate;
        }
        else
        {
            baud_diff = baud_estimate - baudrate;
        }
        if (i == 0 || baud_diff < best_baud_diff)
        {
            // Closest to requested baud rate so far
            best_divisor = try_divisor;
            best_baud = baud_estimate;
            best_baud_diff = baud_diff;
            if (baud_diff == 0)
            {
                // Spot on! No point trying
                break;
            }
        }
    }
    // Encode the best divisor value
    encoded_divisor = (best_divisor >> 3) | (frac_code[best_divisor & 7] << 14);
    // Deal with special cases for encoded value
    if (encoded_divisor == 1)
    {
        encoded_divisor = 0;    // 3000000 baud
    }
    else if (encoded_divisor == 0x4001)
    {
        encoded_divisor = 1;    // 2000000 baud (BM only)
    }
    // Split into "value" and "index" values
    *value = (unsigned short)(encoded_divisor & 0xFFFF);
    if (type == TYPE_2232C || type == TYPE_2232H || type == TYPE_4232H)
    {
        *index = (unsigned short)(encoded_divisor >> 8);
        *index &= 0xFF00;
        *index |= ftdi_index;
    }
    else
        *index = (unsigned short)(encoded_divisor >> 16);

    // Return the nearest baud rate
    return best_baud;
}

int main() {
    int i, j;
    const struct libusb_interface_descriptor *interface;
    int libusb_err;

    assert_libusb(libusb_init(&context));

    libusb_set_debug(context, 3);

    assert_libusb_p(handle = libusb_open_device_with_vid_pid(context, vendor_id, product_id));
    dev = libusb_get_device(handle);

    fprintf(stderr, "libusb_get_device_descriptor\n");
    assert_libusb(libusb_get_device_descriptor(dev, &desc));

    unsigned char data[100];
    fprintf(stderr, "libusb_get_string_descriptor_ascii\n");
    int x = libusb_get_string_descriptor_ascii(handle, desc.iSerialNumber, data, 99);
    if(x < 0) assert_libusb(x);
    fprintf(stderr, "data: %s\n", data);

    /* This doesn't work on OSX (not supported by libusb)
    if(libusb_kernel_driver_active(handle, interface_no)) {
        fprintf(stderr, "kernel driver active\n");
        assert_libusb(libusb_detach_kernel_driver(handle, interface_no));
    }
    else {
        fprintf(stderr, "kernel driver not active\n");
    }
    */

    assert_libusb(libusb_claim_interface(handle, interface_no));

    // This make something funny happen to the device so that it doesn't
    // respond properly. Have to dig into more source code to figure that out
    // - trygve
    //
    //fprintf(stderr, "Resetting device\n");
    //assert_libusb(libusb_reset_device(handle));

    unsigned short value, index;
    int actual_baudrate;

    actual_baudrate = ftdi_convert_baudrate(2400, &value, &index);
    fprintf(stderr, "value: %u\n", value);
    fprintf(stderr, "index: %u\n", index);
    fprintf(stderr, "actual_baudrate: %u\n", actual_baudrate);

    uint8_t SIO_SET_MODEM_CONTROL_REQUEST = 1;
    uint8_t SIO_SET_BAUDRATE_REQUEST = 3;
    uint8_t outRequestType = LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE;
    unsigned int usbTimeout = 1000;

    assert_libusb(libusb_control_transfer(handle, outRequestType, SIO_SET_BAUDRATE_REQUEST, value, index, NULL, 0, usbTimeout));

    // Set DTR
    uint16_t SIO_SET_DTR_HIGH = 0x0101;
    uint16_t SIO_SET_DTR_LOW = 0x0100;
    uint16_t SIO_SET_RTS_HIGH = 0x0202;
    uint16_t SIO_SET_RTS_LOW = 0x0200;

    value = SIO_SET_DTR_HIGH;
    assert_libusb(libusb_control_transfer(handle, outRequestType, SIO_SET_MODEM_CONTROL_REQUEST, value, index, NULL, 0, usbTimeout));

    i = 0;
    unsigned char buffer[30];
    int transferred;
    int timeout = 0;
    while(i < 100) {
        assert_libusb(libusb_bulk_transfer(handle, 0x81, buffer, sizeof(buffer), &transferred, timeout));
        fprintf(stderr, "transferred %d bytes\n", transferred);
    }

    value = SIO_SET_DTR_LOW;
    assert_libusb(libusb_control_transfer(handle, outRequestType, SIO_SET_MODEM_CONTROL_REQUEST, value, index, NULL, 0, usbTimeout));

    libusb_release_interface(handle, interface_no);

    libusb_close(handle);

    libusb_exit(context);
}
