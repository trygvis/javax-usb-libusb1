#include <fx2regs.h>
#include <fx2macros.h>
#include <fx2ints.h>
#include <eputils.h>
#include <delay.h>
#include <autovector.h>
#include <setupdat.h>

#define SYNCDELAY() SYNCDELAY4;

volatile __bit dosuspend=FALSE;
volatile __bit got_sud;
volatile WORD counter;

#include "fx2bits.h"
#include "fx2extra.h"
#include "fx2timer.h"

BYTE buttons[16];
BYTE sample_counter = 0;

void key_pressed(BYTE keys);

/**
 * This does 16 samples, checks if they're all stable and if they are calls
 * key_pressed().
 *
 * TODO: Let this be a running sample instead of reading 16, checking and starting over
 */
void timer0_callback() {
    BYTE first, i;
    __bit change = 1;

    PB0 = 1;
    buttons[sample_counter++] = IOA;

    if(sample_counter == sizeof(buttons)) {
        PB1 = 1;
        sample_counter = 0;

        // This is not very efficient
        first = buttons[0];
        for(i = 1; i < sizeof(buttons); i++) {
            if(buttons[i] != first) {
                change = 0;
                break;
            }
        }

//        change = 1;
        if(change) {
            key_pressed(first);
        }
        PB1 = 0;
    }

    PB0 = 0;
}

void key_pressed(BYTE keys) {
    BYTE i = 3;
    EP6FIFOBUF[i++] = keys & bmBIT0;
    EP6FIFOBUF[i++] = keys & bmBIT1;
    EP6FIFOBUF[i++] = keys & bmBIT2;
    EP6FIFOBUF[i++] = keys & bmBIT3;
    EP6FIFOBUF[i++] = keys & bmBIT4;
    EP6FIFOBUF[i++] = keys & bmBIT5;
    EP6FIFOBUF[i++] = keys & bmBIT6;
    EP6FIFOBUF[i++] = keys & bmBIT7;
    EP6FIFOBUF[i++] = IOA;

//    EP6BCL = 8;
//    SYNCDELAY();
}

void main(void)
{
    volatile WORD count = 0;

    REVCTL=0; // not using advanced endpoint controls

    got_sud=FALSE;
    RENUMERATE_UNCOND();

    SETCPUFREQ(CLK_48M);

    SETIF48MHZ();

    USE_USB_INTS();

    ENABLE_SUDAV();
    ENABLE_USBRESET();
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

    // Port A setup
    PORTACFG=0x00;      // port A = IO
    OEA = 0x00;         // port A[0:7] = in

    OEB = 0xff;         // port B[0:7] = out

    // Timer setup
    // Make timer0 a 16bit timer
    TMOD = bmBIT1;

    EA=1;

    ENABLE_TIMER0();
    // Implicitly enables timer0
    TR0 = 1;

    // Arm EP2 to tell the host that we're ready to receive
    EP2BCL = 0x00;
    SYNCDELAY();

    // loop endlessly
    while(1) {
        if (got_sud) {
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

//        for(i = 0; i < count; i++) {
//            EP6FIFOBUF[i] = EP2FIFOBUF[i];
//        }

        EP6FIFOBUF[0] = MSB(counter);
        EP6FIFOBUF[1] = LSB(counter);
        EP6FIFOBUF[2] = IOA;

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
    // silence warning
    cmd = cmd;
    return FALSE;
}

BOOL handle_get_interface(BYTE ifc, BYTE* alt_ifc) {
    if (ifc==0) {
        *alt_ifc=0;
        return TRUE;
    }
    else {
        return FALSE;
    }
}

BOOL handle_set_interface(BYTE ifc, BYTE alt_ifc) {
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
    return 1;
}

BOOL handle_set_configuration(BYTE cfg) {
    return cfg==1 ? TRUE : FALSE; // we only handle cfg 1
}

void handle_reset_ep(BYTE ep) {
    // silence warning
    ep = ep;
}

// -----------------------------------------------------------------------
// Timer
// -----------------------------------------------------------------------

void timer0_isr() __interrupt TF0_ISR {
    fx2_timer0_isr();
}

// -----------------------------------------------------------------------
//
// -----------------------------------------------------------------------

// TODO: Add sut_isr

void sudav_isr() __interrupt SUDAV_ISR {
    got_sud=TRUE;
    CLEAR_SUDAV();
}

void usbreset_isr() __interrupt USBRESET_ISR {
    handle_hispeed(FALSE);
    CLEAR_USBRESET();
}

void hispeed_isr() __interrupt HISPEED_ISR {
    handle_hispeed(TRUE);
    CLEAR_HISPEED();
}

void resume_isr() __interrupt RESUME_ISR {
    CLEAR_RESUME();
}

void suspend_isr() __interrupt SUSPEND_ISR {
    dosuspend=TRUE;
    CLEAR_SUSPEND();
}
