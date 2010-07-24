#include <libusb.h>
#include <stdlib.h>
#include <stdio.h>

int main(int argc, char* argv[]) {
    uint16_t vendor_id;
    uint16_t product_id;
    libusb_context *ctx;
    libusb_device_handle* device_handle;
    int ret;

    unsigned int tmp;
    sscanf(argv[1], "%x", &tmp);
    vendor_id = tmp;
    sscanf(argv[2], "%x", &tmp);
    product_id = tmp;
    printf("vendor = 0%04x", vendor_id);

    if(libusb_init(&ctx)) {
        perror("libusb_init");
        exit(EXIT_FAILURE);
    }

    libusb_set_debug(ctx, 3);

    if((device_handle = libusb_open_device_with_vid_pid(ctx, vendor_id, product_id)) == NULL) {
        perror("Unable to open device.");
        exit(EXIT_FAILURE);
    }

    fprintf(stderr, "Resetting device...\n");

    ret = libusb_reset_device(device_handle);

    fprintf(stderr, "Device reset, return: %d\n", ret);

    libusb_close(device_handle);

    libusb_exit(ctx);

    return EXIT_SUCCESS;
}
