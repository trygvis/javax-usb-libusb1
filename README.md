Source Code Organization
========================

This source code base used Maven 2 for building. The root directory and each
source code module has a pom.xml file which is used by Maven.

The Maven Modules
-----------------

* *javax.usb* - My implementation of the JSR-80 API. This is a small module
  which only implements the API itself.

* *javax.usb-extra* - Some extra code that only depend on the JSR-80 API
  which I feel either should be a part of the API or is just too generic to
  put elsewhere.

* *libusb* / *libusb-git* - A Maven module that builds libusb from the
  sources checked out by git under libusb-git. Make sure to configure your
  environment to get this module to build. See below.

* *javalibusb1* - The implementation itself. Consists of some C code with some header
  files and a set of Java files.

* *usbtools* - Random tools to work with USB chips.

  Contains a library and command line tools to work with fx2 chips.

* *ftdi* - Utilities for talking to [ftdi] chips.

Technical Implementation Details
================================

The device and handle references are stored internally in the Device
and Interface classes as integers. These should be byte arrays to
be a bit more generic.

To control the debugging level from libusb set the system property
"javax.usb.libusb.debug" to the desired level:

>     -Djavax.usb.libusb.debug=3

See their documentation for the up to date values. Current values:

  * Level 0: no messages ever printed by the library (default)
  * Level 1: error messages are printed to `stderr`
  * Level 2: warning and error messages are printed to `stderr`
  * Level 3: informational messages are printed to `stdout`, warning
    and error messages are printed to `stderr`

To trace all calls to libusb, truss/strace/kdump style set the
system property "javax.usb.libusb.trace" to true

>     -Djavax.usb.libusb.trace=true

Building
--------

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

Note that on Linux "arch" is amd64 while on OSX it is x86_64.
You can check the values for your platform by running

>     mvn -version`

Note that you might have to run Maven's JVM with -d32 or -d64 to select a different bit size.

You can see the current settings with

>     mvn help:active-profiles -N -f javalibusb1/pom.xml`

Building 64-Bit Versions
------------------------

The Maven setup will automatically build 64-bit versions of libusb *and*
javalibusb1 if you are using a 64-bit JVM. Run your Maven with -d64 to run a
64-bit JVM.

Running main() Methods From Your IDE
------------------------------------

At least IntelliJ IDEA does not realize that the usbtools module depend on the
javalibusb1 because the javalibusb1 is not recognized ha a "Java" module when
it has packaging=nar in its POM.

Checking the formatted version of the README file
-------------------------------------------------

Simply run:

>     rdiscount README.markdown > README.html

Notes on Keeping Syncronized with Upstream
------------------------------------------

This is the command used to create and synchronize the upstream CVS repositories:

>     git cvsimport -d :pserver:anonymous@javax-usb.cvs.sourceforge.net:/cvsroot/javax-usb javax-usb

Notes on Building the TCK
=========================

This section has slowly bit rotted after no-one could document the required USB setup
for the firmware. Will have to invent/run my own TCK to prove compliance.

To build this software you need:

  * For the TCK: A working SDCC installation in your PATH that supports
    the "mcs51" target. I'm using v2.9.0.

The Firmware
------------

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

Building the Firmware
---------------------

Building the firmware is easy once your have all the prerequisites set up:

    cd tck/images
    make

The Build
---------

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

[fx2lib]: http://github.com/trygvis/fx2lib "fx2lib"
[ftdi]: http://www.ftdichip.com/ "FTDI"
