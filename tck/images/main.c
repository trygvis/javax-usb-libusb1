#include <fx2regs.h>
#include <fx2macros.h>
#include <fx2ints.h>
#include <delay.h>
#include <autovector.h>
#include <setupdat.h>

#define SYNCDELAY() SYNCDELAY4;

volatile BYTE tmp = 0;
volatile __bit dosuspend=FALSE;
volatile __bit got_sud;

#define BUFFER_START 0x1000
__xdata char *dest=(__xdata char*)BUFFER_START;
__xdata char *BUFFER_END=(__xdata char*)BUFFER_START + 0x200;

void hello_n(int i) {
    IOA = IOA | (1 << i);
    IOA = IOA & ~(1 << i);
}

void hello() {
    IOA = 0xff;
    IOA = 0x00;
}

void log(const char* src) {
    const char* separator = "-";

//    hello();

    // Check for buffer overflow.
    // TODO: Figure out how much to buffer
    if(dest > BUFFER_END)
    {
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
    PORTACFG=0x00; // port A = IO
    OEA = 0xFF; // port A[0:7] = out

    hello();
    hello();
    hello();
    hello();

    log("Hello world!");

    log("SETCPUFREQ(CLK_48M)");
    SETCPUFREQ(CLK_48M);

    log("USE_USB_INTS");
    USE_USB_INTS();

    log("ENABLE_SUDAV");
    ENABLE_SUDAV();
    log("ENABLE_USBRESET");
    ENABLE_USBRESET();
    log("ENABLE_HISPEED");
    ENABLE_HISPEED();
    log("ENABLE_SUSPEND");
    ENABLE_SUSPEND();
    log("ENABLE_RESUME");
    ENABLE_RESUME();

    log("EA=1");
    hello_n(7);
    EA=1;
    hello_n(7);

    log("RENUMERATE_UNCOND");
    RENUMERATE_UNCOND();

    log("Init done!");

    // loop endlessly
    for(tmp = 0;; tmp++) {
//        IOA = tmp & 0x0f;

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


void sutok_isr() __interrupt SUTOK_ISR {hello_n(5);}
void ep0ack_isr() __interrupt EP0ACK_ISR {hello_n(5);}
void ep0in_isr() __interrupt EP0IN_ISR {hello_n(5);}
void ep0out_isr() __interrupt EP0OUT_ISR {hello_n(5);}
void ep1in_isr() __interrupt EP1IN_ISR {hello_n(5);}
void ep1out_isr() __interrupt EP1OUT_ISR {hello_n(5);}
void ep2_isr() __interrupt EP2_ISR {hello_n(5);}
void ep4_isr() __interrupt EP4_ISR {hello_n(5);}
void ep6_isr() __interrupt EP6_ISR {hello_n(5);}
void ep8_isr() __interrupt EP8_ISR {hello_n(5);}
void ibn_isr() __interrupt IBN_ISR {hello_n(5);}
void ep0ping_isr() __interrupt EP0PING_ISR {hello_n(5);}
void ep1ping_isr() __interrupt EP1PING_ISR {hello_n(5);}
void ep2ping_isr() __interrupt EP2PING_ISR {hello_n(5);}
void ep4ping_isr() __interrupt EP4PING_ISR {hello_n(5);}
void ep6ping_isr() __interrupt EP6PING_ISR {hello_n(5);}
void ep8ping_isr() __interrupt EP8PING_ISR {hello_n(5);}
void errlimit_isr() __interrupt ERRLIMIT_ISR {hello_n(5);}
void ep2isoerr_isr() __interrupt EP2ISOERR_ISR {hello_n(5);}
void ep4isoerr_isr() __interrupt EP4ISOERR_ISR {hello_n(5);}
void ep6isoerr_isr() __interrupt EP6ISOERR_ISR {hello_n(5);}
void ep8isoerr_isr() __interrupt EP8ISOERR_ISR {hello_n(5);}
void spare_isr() __interrupt RESERVED_ISR {hello_n(5);}
void ep2pf_isr() __interrupt EP2PF_ISR{hello_n(5);}
void ep4pf_isr() __interrupt EP4PF_ISR{hello_n(5);}
void ep6pf_isr() __interrupt EP6PF_ISR{hello_n(5);}
void ep8pf_isr() __interrupt EP8PF_ISR{hello_n(5);}
void ep2ef_isr() __interrupt EP2EF_ISR{hello_n(5);}
void ep4ef_isr() __interrupt EP4EF_ISR{hello_n(5);}
void ep6ef_isr() __interrupt EP6EF_ISR{hello_n(5);}
void ep8ef_isr() __interrupt EP8EF_ISR{hello_n(5);}
void ep2ff_isr() __interrupt EP2FF_ISR{hello_n(5);}
void ep4ff_isr() __interrupt EP4FF_ISR{hello_n(5);}
void ep6ff_isr() __interrupt EP6FF_ISR{hello_n(5);}
void ep8ff_isr() __interrupt EP8FF_ISR{hello_n(5);}
void gpifdone_isr() __interrupt GPIFDONE_ISR{hello_n(5);}
void gpifwf_isr() __interrupt GPIFWF_ISR{hello_n(5);}
