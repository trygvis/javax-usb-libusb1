CLASSPATH=target/test-classes:target/classes:../javax.usb/target/classes

sinclude Makefile.local.$(shell uname -s)
sinclude Makefile.$(shell uname -s)

JDK_HOME ?= $(JAVA_HOME)
CC      ?= gcc
CCFLAGS += -I$(JDK_HOME)/include
CCFLAGS += -Werror

libjavalibusb1.$(SHREXT): javalibusb.lo usbw.lo
	$(LIBTOOL) --mode=link --tag=CC cc -module -shared -shrext .$(SHREXT) -o libjavalibusb1.la $^ $(LDFLAGS) -rpath /usr/lib
#	$(CC) -o $@ -shared -Wl,-soname,$@ $(CCFLAGS) -lusb $<

javalibusb.lo: javalibusb.c
	$(LIBTOOL) --mode=compile --tag=CC cc -Wall -g -c -shared -o $@ $< $(CCFLAGS)

usbw.lo: usbw.c
	$(LIBTOOL) --mode=compile --tag=CC cc -Wall -g -c -shared -o $@ $< $(CCFLAGS)

javalibusb.c: javalibusb1_libusb1.h javalibusb1_impl_Libusb1UsbDevice.h

target/classes/javalibusb1/libusb1.class: src/main/java/javalibusb1/libusb1.java
	mkdir -p target/classes target/test-classes
	$(JDK_HOME)/bin/javac -classpath $(CLASSPATH) -d target/classes $(shell find src/main/java -name *.java)
	$(JDK_HOME)/bin/javac -classpath $(CLASSPATH) -d target/test-classes $(shell find src/test/java -name *.java)

javalibusb1_libusb1.h: target/classes/javalibusb1/libusb1.class
	$(JDK_HOME)/bin/javah -classpath $(CLASSPATH) -jni javalibusb1.libusb1

javalibusb1_impl_Libusb1UsbDevice.h: target/classes/javalibusb1/impl/Libusb1UsbDevice.class
	$(JDK_HOME)/bin/javah -classpath $(CLASSPATH) -jni javalibusb1.impl.Libusb1UsbDevice

clean: clean-java clean-native

clean-java:
	rm -rf target/classes target/test-classes

clean-native:
	rm -rf *.lo *.la *.so $(wildcard core) $(wildcard hs_err_pid*)

test: libjavalibusb1.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs $(JVM_ARGS) javalibusb1.test.JUsbTest

lsusb: libjavalibusb1.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs $(JVM_ARGS) javalibusb1.test.lsusb

ReadSerial: libjavalibusb1.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs $(JVM_ARGS) javalibusb1.test.ReadSerial
