CPPFLAGS =-Werror
#CPPFLAGS+=-I/opt/local/include
CPPFLAGS+=-I/opt/local/include/libusb-1.0

LDFLAGS =-L/opt/local/lib
LDFLAGS+=-lftdi
LDFLAGS+=-lusb-1.0

all: serial-reader serial_read

serial-reader: serial-reader.c
	$(CC) $(CCFLAGS) $(CPPFLAGS) -I/opt/local/include/libusb-1.0 $(LDFLAGS) -o $@ $<

serial_read: serial_read.c
	$(CC) $(CCFLAGS) $(CPPFLAGS) -I/opt/local/include $(LDFLAGS) -o $@ $<

clean:
	rm -f serial_read serial-reader

.PHONY: clean
