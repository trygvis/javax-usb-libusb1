CLASSPATH=target/test-classes:target/classes:../javax.usb/target/classes

sinclude Makefile.local.$(shell uname -s)
sinclude Makefile.$(shell uname -s)

JDK_HOME ?= $(JAVA_HOME)
CC      ?= gcc
CCFLAGS += -I$(JDK_HOME)/include
CCFLAGS += -Wall
CCFLAGS += -Werror
# CCFLAGS += -m64 # Add this for 64-bit version

all: check tools lib

check: check_jvm_platform
	@./check_jvm_platform

TOOLS=usb_open_twice usb_get_config_descriptor usb_reset
tools: $(TOOLS)

lib: libjavalibusb1.$(SHREXT)

libjavalibusb1.$(SHREXT): javalibusb.lo usbw.lo
	$(LIBTOOL) --mode=link --tag=CC cc -module -shared -shrext .$(SHREXT) -o libjavalibusb1.la $^ $(LDFLAGS) -rpath /usr/lib

javalibusb.lo: javalibusb.c
	$(LIBTOOL) --mode=compile --tag=CC cc $(CCFLAGS) -g -c -shared -o $@ $<

usbw.lo: usbw.c
	$(LIBTOOL) --mode=compile --tag=CC cc $(CCFLAGS) -g -c -shared -o $@ $<

javalibusb.c: javalibusb1_libusb1.h javalibusb1_Libusb1UsbDevice.h javalibusb1_Libusb1UsbInterface.h

target/classes/javalibusb1/%.class: src/main/java/javalibusb1/libusb1.java
	mkdir -p target/classes target/test-classes
	$(JDK_HOME)/bin/javac -classpath $(CLASSPATH) -d target/classes $(shell find src/main/java -name *.java)
	$(JDK_HOME)/bin/javac -classpath $(CLASSPATH) -d target/test-classes $(shell find src/test/java -name *.java)
	rsync -a --progress src/main/resources/ target/classes

%.h:
	rm -f $@
	$(JDK_HOME)/bin/javah -classpath $(CLASSPATH) -jni $(shell echo $< | sed "s,target.classes.\(.*\).class$$,\1," | tr "/" ".")

javalibusb1_libusb1.h: target/classes/javalibusb1/libusb1.class
javalibusb1_Libusb1UsbDevice.h: target/classes/javalibusb1/Libusb1UsbDevice.class
javalibusb1_Libusb1UsbInterface.h: target/classes/javalibusb1/Libusb1UsbInterface.class

clean: clean-java clean-native

clean-java:
	rm -rf target/classes target/test-classes

clean-native:
	@rm -rf *.lo *.la *.so $(wildcard core) $(wildcard hs_err_pid*) $(wildcard *.o)
	@rm -f $(TOOLS)

test: libjavalibusb1.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs $(JVM_ARGS) javalibusb1.test.JUsbTest

lsusb: libjavalibusb1.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs $(JVM_ARGS) javalibusb1.test.lsusb

ReadSerial: libjavalibusb1.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs $(JVM_ARGS) javalibusb1.test.ReadSerial

usb_get_config_descriptor: usb_get_config_descriptor.o usbw.o
usb_open_twice: usb_open_twice.o usbw.o
usb_reset: usb_reset.o usbw.o
check_jvm_platform: check_jvm_platform.o

%.o: %.c
	@echo CC $<
	@$(CC) $(CCFLAGS) -c -g -o $@ $^

%: %.o
	@echo LD $@
	@$(CC) $(CCFLAGS) $(LDFLAGS) -o $@ $^
