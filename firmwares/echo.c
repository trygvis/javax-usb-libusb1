#include <fx2regs.h>
#include <fx2macros.h>
#include <fx2ints.h>
#include <eputils.h>
#include <delay.h>
#include <autovector.h>
#include <setupdat.h>
#include "descriptors.h"

#define SYNCDELAY() SYNCDELAY4;

volatile BYTE tmp = 0;
volatile __bit dosuspend=FALSE;
volatile __bit got_sud;

#define BUFFER_START 0x1000
__xdata char *dest=(__xdata char*)BUFFER_START;
__xdata char *BUFFER_END=(__xdata char*)BUFFER_START + 0x200;

// TODO: These (or some defines like it) should go into fx2lib
#define EPCFG_DIRECTION_IN  bmBIT6
#define EPCFG_DIRECTION_OUT 0
#define EPCFG_TYPE_ISO      bmBIT4
#define EPCFG_TYPE_BULK     bmBIT5
#define EPCFG_TYPE_INT      bmBIT5 | bmBIT4
#define EPCFG_BUFFER_QUAD   0
#define EPCFG_BUFFER_DOUBLE bmBIT1
#define EPCFG_BUFFER_TRIPLE bmBIT3 | bmBIT2

__code __at 0x3e00
struct device_descriptor dev_dscr = {
    sizeof(struct device_descriptor),
//        DSCR_DEVICE_LEN,
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

__code __at 0x3e00+sizeof(struct device_descriptor)
struct qualifier_descriptor dev_qual_dscr = {
    10, //    sizeof(struct qualifier_descriptor),
    DSCR_DEVQUAL_TYPE,
    0x0200,
    0xff,
    0xff,
    0xff,
    64,
    1,
    0
};

__code __at 0x3e00+sizeof(struct device_descriptor)+sizeof(struct qualifier_descriptor)
struct highspd_dscr_t {
    struct configuration_descriptor descriptor;
    struct interface_descriptor interface;
    struct endpoint_descriptor endpoint2;
    struct endpoint_descriptor endpoint6;
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
        2,
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
    }
};

__code __at 0x3e00+sizeof(struct device_descriptor)+sizeof(struct qualifier_descriptor)+sizeof(struct highspd_dscr_t)
struct fullspd_dscr_t {
    struct configuration_descriptor descriptor;
    struct interface_descriptor interface;
    struct endpoint_descriptor endpoint2;
    struct endpoint_descriptor endpoint6;
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
        2,
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
    }
};

// Strings are no go for now.
//#define USB_STRING(str) {sizeof(str) + 1, DSCR_STRING_TYPE, str}
//
//__code __at 0x3e00+sizeof(struct device_descriptor)+sizeof(struct qualifier_descriptor)+sizeof(struct highspd_dscr_t)+sizeof(struct fullspd_dscr_t)
//struct usb_string dev_strings[2] = {
//    {sizeof("H\0i\0!") + 1, DSCR_STRING_TYPE, "H\0i\0!"}
//    {sizeof("H\0i\0!") + 1, DSCR_STRING_TYPE, "H\0i\0!"}
////    USB_STRING("H\0i\0!\0"),
////    USB_STRING("T\0h\0e\0r\0e\0")
//};

void hello_n(BYTE i) {
}

void hello() {
}

void log(const char* src) {
}

void main(void)
{
    volatile WORD count = 0;
    WORD i;

    REVCTL=0; // not using advanced endpoint controls

    got_sud=FALSE;
    log("RENUMERATE_UNCOND");
    RENUMERATE_UNCOND();

    log("SETCPUFREQ(CLK_48M)");
    SETCPUFREQ(CLK_48M);

    log("SETIF48MHZ");
    SETIF48MHZ();

    log("USE_USB_INTS");
    USE_USB_INTS();

    log("ENABLE_SUDAV");
    ENABLE_SUDAV();
    log("ENABLE_USBRESET");
    ENABLE_USBRESET();
    log("ENABLE_HISPEED");
    ENABLE_HISPEED();

    // Endpoint 1
    EP1INCFG &= ~bmVALID;
    SYNCDELAY();

    // Endpoint 2
    // valid=1, direction=0 out, type=10 bulk, size=0 (512), reserved=0, buffering=10 (double)
//    EP2CFG = 0xA2; // 10100010
    // The device refuses to enumerate with buffering=triple or quad
    EP2CFG = bmVALID + EPCFG_DIRECTION_OUT + EPCFG_TYPE_BULK + EPCFG_BUFFER_DOUBLE;
    SYNCDELAY();

    // Endpoint 4
    EP4CFG &= ~bmVALID;
    SYNCDELAY();

    // Endpoint 6
    // valid=1, direction=1 in, type=10 bulk, size=0 (512), reserved=0, buffering=10 (double)
    EP6CFG = 0xE2; // 11100010
    SYNCDELAY();

    // Endpoint 8
    EP8CFG &= ~bmVALID;
    SYNCDELAY();

    EP2BCH = 0;
    EP2BCL = 0;

    log("EA=1");
    hello_n(7);
    EA=1;

    // Arm EP2 to tell the host that we're ready to receive
    EP2BCL = 0x00;
    SYNCDELAY();

    log("Init done!");

    // loop endlessly
    for(tmp = 0;; tmp++) {
        hello_n(7);

        if (got_sud) {
            hello_n(6);
            log("Handle setupdata");
            handle_setupdata();
            got_sud=FALSE;
        }

        // All EP2 buffers are empty, nothing to do
        if(EP2468STAT & bmEP2EMPTY) {
            continue;
        }

        // All EP6 buffers are full and committed to the USB serial engine
        if(EP2468STAT & bmEP6FULL) {
            continue;
        }

        count = MAKEWORD(EP2BCH, EP2BCL);

        for(i = 0; i < count; i++) {
            EP6FIFOBUF[i] = EP2FIFOBUF[i];
        }

        EP6BCH = EP2BCH;
        SYNCDELAY();
        EP6BCL = EP2BCL; // Arms EP6
        SYNCDELAY();

        EP2BCL=0x80; // Arms EP2
        SYNCDELAY();
    }
}

// -----------------------------------------------------------------------
//
// -----------------------------------------------------------------------

BOOL handle_vendorcommand(BYTE cmd) {
    log("handle_vendorcommand");
    return FALSE;
}

BOOL handle_get_interface(BYTE ifc, BYTE* alt_ifc) {
    log("handle_get_interface");
    if (ifc==0) {
        *alt_ifc=0;
        return TRUE;
    }
    else {
        return FALSE;
    }
}

BOOL handle_set_interface(BYTE ifc, BYTE alt_ifc) {
    log("handle_set_interface");

    if (ifc == 0 && alt_ifc == 0) {
        // SEE TRM 2.3.7
        RESETTOGGLE(0x02);
        RESETTOGGLE(0x86);
        RESETFIFO(0x02);
        EP2BCL = 0x80;
        SYNCDELAY();
        EP2BCL = 0x80;
        SYNCDELAY();
        RESETFIFO(0x86);
        return TRUE;
    }

    return FALSE;
}

BYTE handle_get_configuration() {
    log("handle_get_configuration");
    return 1;
}

BOOL handle_set_configuration(BYTE cfg) {
    log("handle_set_configuration");
    return cfg==1 ? TRUE : FALSE; // we only handle cfg 1
}

void handle_reset_ep(BYTE ep) {
    log("handle_reset_ep");
}

// -----------------------------------------------------------------------
//
// -----------------------------------------------------------------------

// TODO: Add sut_isr

void sudav_isr() __interrupt SUDAV_ISR {
    hello_n(0);
    log("sudav_isr");
    got_sud=TRUE;
    CLEAR_SUDAV();
}

void usbreset_isr() __interrupt USBRESET_ISR {
    hello_n(1);
    log("usbreset_isr");
    handle_hispeed(FALSE);
    CLEAR_USBRESET();
}

void hispeed_isr() __interrupt HISPEED_ISR {
    hello_n(2);
    log("hispeed_isr");
    handle_hispeed(TRUE);
    CLEAR_HISPEED();
}

void resume_isr() __interrupt RESUME_ISR {
    hello_n(3);
    log("resume_isr");
    CLEAR_RESUME();
}

void suspend_isr() __interrupt SUSPEND_ISR {
    hello_n(4);
    log("suspend_isr");
    dosuspend=TRUE;
    CLEAR_SUSPEND();
}
