#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <libusb.h>

int main() {
    printf("sizeof(jbyte)=%d\n", sizeof(jbyte));
    printf("sizeof(jshort)=%d\n", sizeof(jshort));
    printf("sizeof(jint)=%d\n", sizeof(jint));
    printf("sizeof(jlong)=%d\n", sizeof(jlong));
    printf("sizeof(void *)=%d\n", sizeof(void *));
    printf("sizeof(uint8_t)=%d\n", sizeof(uint8_t));
    printf("sizeof(uint16_t)=%d\n", sizeof(uint16_t));
    printf("sizeof(int)=%d\n", sizeof(int));
    printf("sizeof(long)=%d\n", sizeof(long));
    printf("sizeof(long long)=%d\n", sizeof(long long));

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
