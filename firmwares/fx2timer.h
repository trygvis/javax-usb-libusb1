#ifndef FX2_TIMER_H
#define FX2_TIMER_H

#include "fx2types.h"

void fx2_timer0_isr();
void fx2_setup_timer0(WORD us);

extern void timer0_callback();

#endif
