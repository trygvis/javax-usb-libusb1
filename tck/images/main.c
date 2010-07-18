#include <fx2regs.h>
#include <fx2macros.h>
#include <fx2ints.h>
#include <delay.h>
#include <autovector.h>
#include <setupdat.h>

extern void main_loop();
extern void main_init();

volatile BYTE tmp = 0;
volatile bit dosuspend=FALSE;
volatile bit got_sud;

#define BUFFER_START 0x1000
xdata char *dest=(xdata char*)BUFFER_START;

void log(const char* src) {
    const char* separator = "§";

    // Check for buffer overflow.
    // TODO: Figure out how much to buffer
    if(dest > (BUFFER_START + 0x100)) {
        return;
    }

    while(*src)
    {
        *dest++ = *src++;
    }

    while(*separator)
    {
        *dest++ = *separator++;
    }
}

void main(void)
{
    log("Hello world!");

    // set up interrupts.
    USE_USB_INTS();

//    ENABLE_SUDAV();
//    ENABLE_USBRESET();
    ENABLE_HISPEED();
//    ENABLE_SUSPEND();
//    ENABLE_RESUME();
//
    EA=1;
//
//    RENUMERATE();
    RENUMERATE_UNCOND();

    log("Init done!");

    PORTACFG=0x00; // port A = IO
    OEA = 0xFF; // port A[0:7] = out

//    dest=(xdata char*)0x1000;
    // loop endlessly
    for(tmp = 0;; tmp++) {
       IOA = tmp;

        if (got_sud) {
            log("Handle setupdata");
            handle_setupdata();
            got_sud=FALSE;
        }
//        else {
//            log("poop");
//        }
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

BOOL handle_set_interface(BYTE ifc,BYTE alt_ifc) {
    log("handle_set_interface");
    return TRUE;
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

void sudav_isr() interrupt SUDAV_ISR {
    got_sud=TRUE;
    CLEAR_SUDAV();
}

void usbreset_isr() interrupt USBRESET_ISR {
    handle_hispeed(FALSE);
    CLEAR_USBRESET();
}

void hispeed_isr() interrupt HISPEED_ISR {
    handle_hispeed(TRUE);
    CLEAR_HISPEED();
}

void resume_isr() interrupt RESUME_ISR {
    CLEAR_RESUME();
}

void suspend_isr() interrupt SUSPEND_ISR {
    dosuspend=TRUE;
    CLEAR_SUSPEND();
}
