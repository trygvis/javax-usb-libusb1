include ../javalibusb1/Makefile.local.$(shell uname -s)
include ../javalibusb1/Makefile.$(shell uname -s)

CCFLAGS+=-Werror
CCFLAGS+=-std=c99
CCFLAGS+=-I../javalibusb1/src/main/include

LDFLAGS+=-L/opt/local/lib
LDFLAGS+=-lftdi
LDFLAGS+=-lusb-1.0

BINS=serial-reader serial_read test-echo

all: $(BINS)

serial-reader: serial-reader.c
	@echo CC $<
	@$(CC) $(CCFLAGS) $(CCFLAGS) -I/opt/local/include/libusb-1.0 $(LDFLAGS) -o $@ $<

serial_read: serial_read.c
	@echo CC $<
	@$(CC) $(CCFLAGS) $(CCFLAGS) -I/opt/local/include $(LDFLAGS) -o $@ $<

test-echo: test-echo.c usbw.o
	@echo CC $<
	$(CC) $(CCFLAGS) $(CCFLAGS) $(LDFLAGS) -o $@ $^

usbw.o: ../javalibusb1/src/main/c/usbw.c ../javalibusb1/src/main/include/usbw.h
	@echo CC $<
	$(CC) $(CCFLAGS) $(CCFLAGS) -c -o $@ $<

clean:
	rm -f $(BINS) $(wildcard *.o)

.PHONY: clean
