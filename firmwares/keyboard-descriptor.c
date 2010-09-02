#include <setupdat.h>
#include "descriptors.h"

#pragma constseg DSCR_AREA

#define bNumEndpoints 3

__code struct device_descriptor dev_dscr = {
    sizeof(struct device_descriptor),
    DSCR_DEVICE_TYPE,
    0x0200,
    0xff,
    0xff,
    0xff,
    64,
    0x0547,
    0xff01,
    0x0001,
    2,
    1,
    0,
    1
};

__code struct qualifier_descriptor dev_qual_dscr = {
    sizeof(struct qualifier_descriptor),
    DSCR_DEVQUAL_TYPE,
    0x0200,
    0xff,
    0xff,
    0xff,
    64,
    1,
    0
};

__code struct highspd_dscr_t {
    struct configuration_descriptor descriptor;
    struct interface_descriptor interface;
    struct endpoint_descriptor endpoint2;
    struct endpoint_descriptor endpoint6;
    struct endpoint_descriptor endpoint8;
} highspd_dscr = {
    {
        sizeof(struct configuration_descriptor),
        DSCR_CONFIG_TYPE,
        sizeof(struct highspd_dscr_t),
        1,
        1,
        0,
        0x80,
        0x32
    },
    {
        sizeof(struct interface_descriptor),
        DSCR_INTERFACE_TYPE,
        0,
        0,
        bNumEndpoints,
        0xff,
        0xff,
        0xff,
        3
    },
    {
        sizeof(struct endpoint_descriptor),
        DSCR_ENDPOINT_TYPE,
        0x02,
        ENDPOINT_TYPE_BULK,
        512,
        0x00
    },
    {
        sizeof(struct endpoint_descriptor),
        DSCR_ENDPOINT_TYPE,
        0x86,
        ENDPOINT_TYPE_BULK,
        512,
        0x00
    },
    {
        sizeof(struct endpoint_descriptor),
        DSCR_ENDPOINT_TYPE,
        0x88,
        ENDPOINT_TYPE_INT,
        64,
        6           // 2^6 * 125us polling interval = 8ms
    }
};

__code struct fullspd_dscr_t {
    struct configuration_descriptor descriptor;
    struct interface_descriptor interface;
    struct endpoint_descriptor endpoint2;
    struct endpoint_descriptor endpoint6;
    struct endpoint_descriptor endpoint8;
} fullspd_dscr = {
    {
        sizeof(struct configuration_descriptor),
        DSCR_CONFIG_TYPE,
        sizeof(struct fullspd_dscr_t),
        1,
        1,
        0,
        0x80,
        0x32
    },
    {
        sizeof(struct interface_descriptor),
        DSCR_INTERFACE_TYPE,
        0,
        0,
        bNumEndpoints,
        0xff,
        0xff,
        0xff,
        3
    },
    {
        sizeof(struct endpoint_descriptor),
        DSCR_ENDPOINT_TYPE,
        0x02,
        ENDPOINT_TYPE_BULK,
        64,
        0x00
    },
    {
        sizeof(struct endpoint_descriptor),
        DSCR_ENDPOINT_TYPE,
        0x86,
        ENDPOINT_TYPE_BULK,
        64,
        0x00
    },
    {
        sizeof(struct endpoint_descriptor),
        DSCR_ENDPOINT_TYPE,
        0x88,
        ENDPOINT_TYPE_INT,
        64,
        12          // 12 * 1ms polling interval = 12ms
    }
};

// Strings are no go for now, need to adjust setupdat.c
//#define USB_STRING(str) {sizeof(str) + 1, DSCR_STRING_TYPE, str}
//
//__code __at 0x3e00+sizeof(struct device_descriptor)+sizeof(struct qualifier_descriptor)+sizeof(struct highspd_dscr_t)+sizeof(struct fullspd_dscr_t)
//__code
//struct usb_string dev_strings[2] = {
//    {sizeof("H\0i\0!") + 1, DSCR_STRING_TYPE, "H\0i\0!"}
//    {sizeof("H\0i\0!") + 1, DSCR_STRING_TYPE, "H\0i\0!"}
//    USB_STRING("H\0i\0!\0"),
//    USB_STRING("T\0h\0e\0r\0e\0")
//};
