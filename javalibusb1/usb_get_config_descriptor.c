#include "usbw.h"
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
    printf("vendor = 0%04x\n", vendor_id);

    uint8_t config_index = atoi(argv[3]);

    usbw_set_trace_calls(1);

    if(usbw_init(&ctx)) {
        perror("libusb_init");
        exit(EXIT_FAILURE);
    }

    usbw_set_debug(ctx, 0);

    if((device_handle = libusb_open_device_with_vid_pid(ctx, vendor_id, product_id)) == NULL) {
        perror("Unable to open device.");
        exit(EXIT_FAILURE);
    }

    libusb_device* device = libusb_get_device(device_handle);

    fprintf(stderr, "Getting config descriptor %d\n", config_index);

    struct libusb_config_descriptor *config;
    ret = usbw_get_config_descriptor(device, config_index, &config);

    libusb_free_config_descriptor(config);

    usbw_close(device_handle);

    usbw_exit(ctx);

    return EXIT_SUCCESS;
}
