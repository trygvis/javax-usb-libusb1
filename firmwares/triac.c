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

void key_changed(BYTE new_state);

static BYTE current_button_states;

/**
 * This does 16 samples, checks if they're all stable and if they are calls
 * key_pressed().
 *
 * TODO: Let this be a running sample instead of reading 16, checking and starting over
 */
void timer0_callback() {
    BYTE first, i;
    __bit all_samples_equal = 1;

    buttons[sample_counter++] = ~IOA;

    if(sample_counter == sizeof(buttons)) {
        sample_counter = 0;

        // This is not very efficient
        first = buttons[0];
        for(i = 1; i < sizeof(buttons); i++) {
            if(buttons[i] != first) {
                all_samples_equal = 0;
                break;
            }
        }

        if(all_samples_equal) {
            key_changed(first);
        }
    }
}

void key_changed(BYTE new_state) {
    BYTE i = 0, j = 0;
    BYTE bm, current, new;

    if(current_button_states == new_state) {
        return;
    }

    for(i = 0; i < 8; i++) {
        bm = 1 << i;
        current = (current_button_states & bm) > 0;
        new = (new_state & bm) > 0;

        if(new != current) {
            EP8FIFOBUF[j++] = new ? 'D' : 'U';
            EP8FIFOBUF[j++] = '0' + i;
        }
    }

    current_button_states = new_state;

    EP8BCH = 0;
    EP8BCL = j;
    SYNCDELAY();
}

void init_usb() {
    REVCTL=0; // not using advanced endpoint controls

    got_sud=FALSE;
    RENUMERATE_UNCOND();

    SETCPUFREQ(CLK_48M);
    SETCPUFREQ(CLK_12M);

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
    EP2CFG = bmVALID + EPCFG_DIRECTION_IN + EPCFG_TYPE_INT + EPCFG_BUFFER_DOUBLE;
    SYNCDELAY();
}

void init_port_a() {
    PORTACFG=0x00;      // port A = IO
    OEA = 0x00;         // port A[0:7] = in
}

void init_port_b() {
    char i;

    IOB = 0x00;
    OEB = 0xff;         // port B[0:7] = out

    /*
    for(i = 0; i < 8; i++) {
        IOB = 1 << i;
        delay(100);
    }
    for(i = 0; i < 8; i++) {
        IOB = 1 << i;
        delay(100);
    }
    for(i = 0; i < 8; i++) {
        IOB = 1 << i;
        delay(100);
    }
    IOB = 0xff;
    delay(500);
    IOB = 0x00;
    delay(500);
    IOB = 0xff;
    delay(500);
    IOB = 0x00;
    */
}

void main(void)
{
    volatile WORD count = 0;
    volatile BYTE last_ioa = 0;
    volatile BYTE current_ioa = 0;
    BYTE i;
    BYTE lightIndex;

    init_usb();
    init_port_a();
    init_port_b();

    fx2_setup_timer0(1000); // fire every ms

    EA=1;

    // Arm EP2 to tell the host that we're ready to receive
    EP2BCH = 0;
    EP2BCL = 0x00;
    SYNCDELAY();

    // loop endlessly
    while(1) {
        if (got_sud) {
            handle_setupdata();
            got_sud = FALSE;
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

        if(count == 2) {
            lightIndex = '0' - EP2FIFOBUF[1];
            if(EP2FIFOBUF[0] == (BYTE)'I') { // I for Illuminated
                IOB |= (1 << lightIndex);
            }
            else if(EP2FIFOBUF[0] == (BYTE)'O') { // O for Off
                IOB &= ~(1 << lightIndex);
            }
            IOB = 0xff;
        }
//        for(i = 0; i < count; i += 2) {
//            lightIndex = '0' - EP2FIFOBUF[i + 1];
//            if(EP2FIFOBUF[i] == (BYTE)'I') { // I for Illuminated
//                IOB |= (1 << lightIndex);
//            }
//            else if(EP2FIFOBUF[i] == (BYTE)'O') { // O for Off
//                IOB &= ~(1 << lightIndex);
//            }
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
