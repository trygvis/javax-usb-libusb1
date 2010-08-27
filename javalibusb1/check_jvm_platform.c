#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <libusb.h>

int main() {
    if(sizeof(struct libusb_device *) > sizeof(jlong)) {
        printf("Your platform's pointers are too long.\n");
        printf("A jlong has to be able to hold a struct libusb_device*:\n");
        printf("sizeof(jlong): %lu\n", sizeof(jlong));
        printf("sizeof(struct libusb_device *): %lu\n", sizeof(struct libusb_device *));

        return EXIT_FAILURE;
    }

    if(sizeof(jbyte) != sizeof(uint8_t)) {
        printf("sizeof(jbyte) != sizeof(uint8_t)\n");

        return EXIT_FAILURE;
    }

    if(sizeof(jshort) != sizeof(uint16_t)) {
        printf("sizeof(jshort) != sizeof(uint16_t)\n");

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
