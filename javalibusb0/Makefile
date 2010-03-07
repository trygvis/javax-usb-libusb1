CLASSPATH=target/test-classes:target/classes:../javax.usb/target/classes

sinclude Makefile.local.$(shell uname -s)
sinclude Makefile.$(shell uname -s)

JDK_HOME ?= $(JAVA_HOME)
CC      ?= gcc
CCFLAGS += -I$(JDK_HOME)/include
CCFLAGS += -fPIC
#CCFLAGS += /usr/sfw/include

libjavalibusb0.$(SHREXT): javalibusb0_libusb0.lo target/classes/javalibusb0/libusb0.class
	$(LIBTOOL) --mode=link --tag=CC cc -module -shared -shrext .$(SHREXT) -o libjavalibusb0.la $< $(LDFLAGS) -rpath /usr/lib
#	$(CC) -o $@ -shared -Wl,-soname,$@ $(CCFLAGS) -lusb $<

javalibusb0_libusb0.lo: javalibusb0_libusb0.c
	$(LIBTOOL) --mode=compile --tag=CC cc -g -c -shared -o $@ $< $(CCFLAGS)

javalibusb0_libusb0.c: javalibusb0_libusb0.h

target/classes/javalibusb0/libusb0.class: src/main/java/javalibusb0/libusb0.java
	mkdir -p target/classes target/test-classes
	$(JDK_HOME)/bin/javac -classpath $(CLASSPATH) -d target/classes src/main/java/javalibusb0/*.java
	$(JDK_HOME)/bin/javac -classpath $(CLASSPATH) -d target/test-classes src/test/java/javalibusb0/*.java

javalibusb0_libusb0.h: target/classes/javalibusb0/libusb0.class
	rm -f $@
	$(JDK_HOME)/bin/javah -classpath $(CLASSPATH) -jni javalibusb0.libusb0

clean: clean-java clean-native

clean-java:
	rm -rf target/classes target/test-classes

clean-native:
	rm -rf *.lo *.la *.so $(wildcard core) $(wildcard hs_err_pid*)

test: libjavalibusb0.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs javalibusb0.test.JUsbTest

lsusb: libjavalibusb0.$(SHREXT)
	$(JAVA_HOME)/bin/java -classpath $(CLASSPATH) -Xcheck:jni -Djava.library.path=.:.libs javalibusb0.test.lsusb
