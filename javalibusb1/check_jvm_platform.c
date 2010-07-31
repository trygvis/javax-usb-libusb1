#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <libusb.h>

int main() {

    if(sizeof(struct libusb_device *) <= sizeof(jlong)) {
        return EXIT_SUCCESS;
    }

    printf("Your platform's pointers are too long.\n");
    printf("A jlong has to be able to hold a struct libusb_device*:\n");
    printf("sizeof(jlong): %lu\n", sizeof(jlong));
    printf("sizeof(struct libusb_device *): %lu\n", sizeof(struct libusb_device *));

    return EXIT_FAILURE;
}
