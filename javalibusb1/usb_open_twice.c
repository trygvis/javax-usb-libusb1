#include "usbw.h"
#include <stdlib.h>
#include <stdio.h>

int main(int argc, char* argv[]) {
    uint16_t vendor_id;
    uint16_t product_id;
    libusb_context *ctx;

    unsigned int tmp;
    sscanf(argv[1], "%x", &tmp);
    vendor_id = tmp;
    sscanf(argv[2], "%x", &tmp);
    product_id = tmp;

    if(libusb_init(&ctx)) {
        perror("libusb_init");
        exit(EXIT_FAILURE);
    }

    libusb_set_debug(ctx, 3);

    libusb_device **list;

    ssize_t count = usbw_get_device_list(ctx, &list);

    printf("count=%zd\n", count);

    int i;
    for(i = 0; i < count; i++) {
        struct libusb_device *device = list[i];
        struct libusb_device_descriptor desc;
        int ret;
        if((ret = usbw_get_device_descriptor(device, &desc)) != LIBUSB_SUCCESS) {
            printf("bad device %d, ret=%s\n", i, usbw_error_to_string(ret));
            continue;
        }

        printf("%04x:%04x\n", desc.idVendor, desc.idProduct);

        if(desc.idVendor != vendor_id || desc.idProduct != product_id) {
            continue;
        }

        printf("Opening...\n");

        struct libusb_device_handle *handle1;
        if((ret = usbw_open(device, &handle1)) != LIBUSB_SUCCESS) {
            printf("Unable to open device: %s\n", usbw_error_to_string(ret));
            continue;
        }

        printf("handle=%p\n", handle1);

        printf("Opening again...\n");

        struct libusb_device_handle *handle2;
        if((ret = usbw_open(device, &handle2)) != LIBUSB_SUCCESS) {
            printf("Unable to open device: %s\n", usbw_error_to_string(ret));
            continue;
        }

        printf("handle=%p\n", handle2);

        usbw_close(handle1);
        usbw_close(handle2);
    }

    printf("freeing list\n");
    usbw_free_device_list(list, 1);

    /*
    libusb_device_handle* device_handle;
    if((device_handle = usbw_open_device_with_vid_pid(ctx, vendor_id, product_id)) == NULL) {
        perror("Unable to open device.");
        exit(EXIT_FAILURE);
    }

    usbw_close(device_handle);
    */

    libusb_exit(ctx);

    return EXIT_SUCCESS;
}
