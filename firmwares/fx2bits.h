#ifndef FX2_BITS_H
#define FX2_BITS_H

__xdata __at 0x0088 union {
    struct {
        unsigned char IT0:1;
        unsigned char IE0:1;
        unsigned char IT1:1;
        unsigned char IE1:1;
        unsigned char TR0:1;
        unsigned char TF0:1;
        unsigned char TR1:1;
        unsigned char TF1:1;
    };
} bTCON;

__xdata __at 0x0089 union {
    struct {
        unsigned char M_0:2;
        unsigned char C_T0:1;
        unsigned char GATE0:1;
        unsigned char M_1:2;
        unsigned char C_T1:1;
        unsigned char GATE1:1;
    };
} bTMOD;

__xdata __at 0x008E union {
    struct {
        unsigned char MD:3;
        unsigned char T0M:3;
        unsigned char T1M:3;
        unsigned char T2M:3;
    };
} bCKCON;

__xdata __at 0xE600 union {
    struct {
        unsigned char :1;
        unsigned char CLKOE:1;
        unsigned char CLKINV:1;
        unsigned char CLKSPD:2;
        unsigned char PORTCSTB:1;
        unsigned char :1;
        unsigned char :1;
    };
} bCPUCS;

#endif
