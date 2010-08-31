#ifndef FX2_EXTRA_H
#define FX2_EXTRA_H

// TODO: These (or some defines like it) should go into fx2lib
#define EPCFG_DIRECTION_IN  bmBIT6
#define EPCFG_DIRECTION_OUT 0
#define EPCFG_TYPE_ISO      bmBIT4
#define EPCFG_TYPE_BULK     bmBIT5
#define EPCFG_TYPE_INT      bmBIT5 | bmBIT4
#define EPCFG_BUFFER_QUAD   0
#define EPCFG_BUFFER_DOUBLE bmBIT1
#define EPCFG_BUFFER_TRIPLE bmBIT3 | bmBIT2

#endif
