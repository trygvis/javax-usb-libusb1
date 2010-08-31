#include "fx2timer.h"
#include "fx2macros.h"
#include "fx2ints.h"
#include "fx2bits.h"

// TODO: check the doxygen syntax in the comments

/**
CKCON (SFR 0x8E) Timer Rate Control Bits:

CKCON.5 T2M - Timer 2 clock select.
When T2M = 0, Timer 2 uses CLKOUT/12 (for compatibility with standard 8051);
when T2M = 1, Timer 2 uses CLKOUT/4. This bit has no effect when Timer 2 is
configured for baud rate generation.

CKCON.4 T1M - Timer 1 clock select.
When T1M = 0, Timer 1 uses CLKOUT/12 (for compatibility with standard 8051);
when T1M = 1, Timer 1 uses CLKOUT/4.

CKCON.3 T0M - Timer 0 clock select.
When T0M = 0, Timer 0 uses CLKOUT/12 (for compatibility with standard 8051);
when T0M = 1, Timer 0 uses CLKOUT/4.

 */

/**
 * Sets up timer0 to call timer0_callback() at the specified interval. It
 * looks at the currently configured CPU frequency and adjust accordingly.
 *
 * Highest values:
 * \verbatim
 *
 * CPU Freq Interval
 *    12MHz 1023
 *    24MHz 511
 *    48MHz 255
 * \endverbatim
 *
 * It it possible to improve the size of intervals supported with more
 * accounting work.
 *
 * Do not give to small values here as the precision will significantly
 * degrade because of the amount of time the CPU and C code spends in
 * handling interrupts.
 *
 * To properly use this remember to call fx2_timer0_isr() in your ISR for timer 0.
 *
 * Example code:
 * \code
 * void timer0_isr() __interrupt TF0_ISR {
 *     fx2_timer0_isr();
 *     other stuff..
 * }
 * \endcode
 *
 * \param us Number of micro seconds between each callback.
 */
#define bmTMOD_TIMER0_MODE      (bmBIT1 | bmBIT0)
#define bmTMOD_TIMER0_COUNTER   bmBIT2
#define bmTMOD_TIMER0_GATE      bmBIT3

#define bmTMOD_TIMER1_MODE      (bmBIT5 | bmBIT4)
#define bmTMOD_TIMER1_COUNTER   bmBIT6
#define bmTMOD_TIMER1_GATE      bmBIT7

// Mode 0: 13-bit counter
#define TMOD_TIMER0_MODE_0 0x00
// Mode 1: 16-bit counter
#define TMOD_TIMER0_MODE_1 0x01
// Mode 2: 8-Bit Counter with Auto-Reload
#define TMOD_TIMER0_MODE_2 0x02
// Mode 3: Two 8-bit counters
#define TMOD_TIMER0_MODE_3 0x03

#define bmCKCON_T0 bmBIT3

static WORD timer0us;
static BYTE timer0us_tl;
static BYTE timer0us_th;

void fx2_setup_timer0(WORD us) {

/*
Duty cycle:
 12MHz = 83.33ns, 12MHz / 4 = 333.3ns, 12MHz / 12 = 1us
 48MHz = 20.83ns, 48MHz / 4 = 83.33ns, 48MHz / 12 = 250ns

Freq  | Divisor | 1us in cycles | 8bit in us |   16bit in us
48MHz |    4    |       12      | 21.3 ~= 21 |  5461.3 ~=  5461
48MHz |   12    |        4      |         64 |            16384
12MHz |    4    |        3      |         85 | 21845.3 ~= 21845
12MHz |   12    |        1      |        256 |            65536

 */

    timer0us = us;
    // Clear old flags
    TMOD = TMOD & ~(bmTMOD_TIMER0_MODE | bmTMOD_TIMER0_COUNTER | bmTMOD_TIMER0_GATE);

    switch(CPUFREQ) {
        case CLK_12M:
            if(us >= 256) {
                TMOD |= TMOD_TIMER0_MODE_1;
                us = 65536 - us;
                timer0us_tl = LSB(us);
                timer0us_th = MSB(us);

                TL0 = timer0us_tl;
                TH0 = timer0us_th;
                CKCON &= ~bmCKCON_T0;       // div = 12
            }
            else {
                TMOD |= TMOD_TIMER0_MODE_2;
                TL0 = 0;
                if(us >= 86) {
                    TH0 = 256 - us;
                    CKCON &= ~bmCKCON_T0;       // div = 12
                }
                else {
                    TH0 = 256 - (us * 3);
                    CKCON |= bmCKCON_T0;        // div = 4
                }
            }
            TR0 = 1;
            ENABLE_TIMER0();
            break;
    }
}

void fx2_timer0_isr() {
    switch(CPUFREQ) {
        case CLK_12M:
            if(timer0us >= 256) {
                TL0 = timer0us_tl;
                TH0 = timer0us_th;
            }
    }
    timer0_callback();
}
