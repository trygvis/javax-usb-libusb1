Technical Implementation Details
================================

The device and handle references are stored internally in the Device
and Interface classes as integers. These should be byte arrays to
be a bit more generic.

To control the debugging level from libusb set the system property
"javax.usb.libusb.debug" to the desired level:

>     -Djavax.usb.libusb.debug=3`

See their documentation for the up to date values. Current values:

  * Level 0: no messages ever printed by the library (default)
  * Level 1: error messages are printed to `stderr`
  * Level 2: warning and error messages are printed to `stderr`
  * Level 3: informational messages are printed to `stdout`, warning
    and error messages are printed to `stderr`

To trace all calls to libusb, truss/strace/kdump style set the
system property "javax.usb.libusb.trace" to true

>     -Djavax.usb.libusb.trace=true`

Notes on Building
-----------------

To build this software you need:

  * A working SDCC installation in your PATH that supports the "mcs51"
    target.  I'm using v2.9.0.

  * My libusb repository to get the required libusb_get_speed methods.
    You can get it with this command:

        git clone git://git.libusb.org/libusb-trygvis.git

    Online repository: http://git.libusb.org/?p=libusb-trygvis.git;a=summary

This is the command used to create and synchronize the upstream CVS repositories:

>     git cvsimport -d :pserver:anonymous@javax-usb.cvs.sourceforge.net:/cvsroot/javax-usb javax-usb

### The Firmware

The original firmware images are not used as it's not clear which
images they represent. In addition I can't use any tools that work
on OS X/Linux to upload the firmware.

The sources depend on [fx2lib] which is a "Library routines for
creating firmware for the Cypress FX2 (CY7C68013 and variants) with
SDCC". Fx2lib includes its own build system which the Makefile calls out to.

To be able to build the images you need to create a Makefile.local
with references to your fx2 library and add `cycfx2prog` to your
PATH. For example:

    FX2LIBDIR=/Users/trygvis/dev/com.github/mulicheng/fx2lib/
    PATH:=/Users/trygvis/src/cycfx2prog-0.47:$(PATH)

#### Building the Firmware

Building the firmware is easy once your have all the prerequisites set up:

    cd tck/images
    make

#### The Build 

### Loading the Firmware

To load the firmware:

 1. Make sure that the device is available. You can do this with `make list`:

        $ make list
        cycfx2prog --list
        Bus 004 Device 001: ID 05ac:8005
        Bus 004 Device 002: ID 05ac:0237
        Bus 004 Device 003: ID 05ac:8242
        Bus 036 Device 001: ID 05ac:8006
        Bus 036 Device 002: ID 05ac:8507
        Bus 006 Device 001: ID 05ac:8005
        Bus 038 Device 001: ID 05ac:8006
        Bus 038 Device 002: ID 04b4:8613 (unconfigured FX2)

    Here you can see that the device was disovered and as long as
    there's only one device on the system it will be used directly.

 1. Program the device with the wanted image using the `program-%`
    target where the percentage sign is replaced with either
    `topology`, `bulk` or `iso`.

        $ make program-topology
        make topology
        make -f Makefile.topology
        make[2]: Nothing to be done for `ihx'.
        cycfx2prog  prg:build/topology.ihx run
        Using ID 04b4:8613 on 038.002.
        Putting 8051 into reset.
        Programming 8051 using "build/topology.ihx".
        Putting 8051 out of reset.

 1. The device should be ready for the TCK now.

    TODO: Add output of `make list`.

Notes
=====

Understand this firmware:
http://github.com/Dopi/JetKernel/blob/fe56cc9237ef9409e25fefca446b277263879c5e/firmware/keyspan_pda/keyspan_pda.S
Investigate autovectoring.

[fx2lib]: http://github.com/mulicheng/fx2lib "fx2lib"
