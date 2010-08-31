#include <fx2regs.h>
#include <fx2macros.h>
#include <fx2ints.h>
#include <eputils.h>
#include <delay.h>
#include <autovector.h>
#include <setupdat.h>

#include "fx2bits.h"
#include "fx2extra.h"
#include "fx2timer.h"

void main(void)
{
    OED = 0xff;         // port B[0:7] = out

//    SETCPUFREQ(CLK_48M);
//    SETIF48MHZ();
    SETCPUFREQ(CLK_12M);
    USE_USB_INTS();

    EA=1;

    fx2_setup_timer0(10 * 1000);

    while(1) {
        PD1 = !PD1;
    }
}

void timer0_isr() __interrupt TF0_ISR {
    PD0 = 1;
    fx2_timer0_isr();
    PD0 = 0;
}

void timer0_callback() {
    PD0 = 1;
    PD0 = 0;
}
