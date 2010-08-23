.module DEV_DSCR

; descriptor types
; same as setupdat.h
DSCR_DEVICE_TYPE=1
DSCR_CONFIG_TYPE=2
DSCR_STRING_TYPE=3
DSCR_INTERFACE_TYPE=4
DSCR_ENDPOINT_TYPE=5
DSCR_DEVQUAL_TYPE=6

; for the repeating interfaces
DSCR_INTERFACE_LEN=9
DSCR_ENDPOINT_LEN=7

; endpoint types
ENDPOINT_TYPE_CONTROL=0
ENDPOINT_TYPE_ISO=1
ENDPOINT_TYPE_BULK=2
ENDPOINT_TYPE_INT=3

    .globl _dev_strings, _dev_strings_end
; These need to be in code memory.  If
; they aren't you'll have to manully copy them somewhere
; in code memory otherwise SUDPTRH:L don't work right
    .area    DSCR_AREA (CODE)

.even
_dev_strings:
; sample string
_string0:
    .db     string0end-_string0     ; len
    .db     DSCR_STRING_TYPE
    .db     0x09, 0x04              ; who knows
string0end:
; add more strings here

_string1:
    .db     string1end-_string1
    .db     DSCR_STRING_TYPE
    .ascii 'H'
    .db     0
    .ascii 'i'
    .db     0
string1end:

_string2:
    .db     string2end-_string2
    .db     DSCR_STRING_TYPE
    .ascii 'T'
    .db     0
    .ascii 'h'
    .db     0
    .ascii 'e'
    .db     0
    .ascii 'r'
    .db     0
    .ascii 'e'
    .db     0
string2end:

_string3:
    .db     string3end-_string3
    .db     DSCR_STRING_TYPE
    .ascii 'i'
    .db     0
    .ascii 'F'
    .db     0
    .ascii 'a'
    .db     0
    .ascii 'c'
    .db     0
    .ascii 'e'
    .db     0
string3end:
    
_dev_strings_end:
    .dw     0x0000                  ; just in case someone passes an index higher than the end to the firmware
