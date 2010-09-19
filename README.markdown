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

### Building

To build the main software a `mvn install` will be sufficient.

If you want to make life easy for you, you want build and use the libusb
referenced from as a git module too. To do that simply run
`(cd libusb; mvn install)`. Like with the javalibusb1 library, the build is
set to build 64-bit versions if the JVM is running in 64-bit mode.

If you do not use the referenced libusb module, you have to build it manually and
point libusb.home to it.

Example settings.xml:

    <settings>
      <profiles>
        <profile>
          <id>libusb-32</id>
          <activation>
            <os>
              <arch>i386</arch>
            </os>
          </activation>
          <properties>
            <libusb.home>${user.home}/opt/libusb-git-32</libusb.home>
            <libusb.cflags>-m32</libusb.cflags>
          </properties>
        </profile>
        <profile>
          <id>libusb-64</id>
          <activation>
            <os>
              <arch>x86_64</arch>
            </os>
          </activation>
          <properties>
            <libusb.home>${user.home}/opt/libusb-git-64</libusb.home>
            <libusb.cflags>-m64</libusb.cflags>
          </properties>
        </profile>
      </profiles>
    </settings>

### Building 64-Bit Versions

The Maven setup will automatically build 64-bit versions of libusb *and*
javalibusb1 if you are using a 64-bit JVM. Run your Maven with -d64 to run a
64-bit JVM.

Notes on Keeping Syncronized with Upstream
------------------------------------------

This is the command used to create and synchronize the upstream CVS repositories:

>     git cvsimport -d :pserver:anonymous@javax-usb.cvs.sourceforge.net:/cvsroot/javax-usb javax-usb

Notes on Building the TCK
-------------------------

This section has slowly bit rotted after no-one could document the required USB setup
for the firmware. Will have to invent/run my own TCK to prove compliance.

To build this software you need:

  * For the TCK: A working SDCC installation in your PATH that supports
    the "mcs51" target. I'm using v2.9.0.

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

[fx2lib]: http://github.com/mulicheng/fx2lib "fx2lib"
