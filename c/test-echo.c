#include "usbw.h"

#include <stdio.h>
#include <stdlib.h>

int main() {
    int ret;

    usbw_set_trace_calls(1);

    if(usbw_init(NULL)) {
        perror("libusb_init");
        return EXIT_FAILURE;
    }

    usbw_set_debug(NULL, 3);

    libusb_device_handle* handle = NULL;
    uint16_t vendor_id = 0x0547;
    uint16_t product_id = 0xff01;

    if((handle = usbw_open_device_with_vid_pid(NULL, vendor_id, product_id)) == NULL) {
        printf("libusb_open_device_with_vid_pid: device not found\n");
        goto exit;
    }

    int interface = 0;

    if((ret = usbw_claim_interface(handle, interface))) {
        printf("usbw_claim_interface: %s\n", usbw_error_to_string(ret));
        goto exit;
    }

    unsigned int timeout = 10000;

    unsigned char out_data[] = {1, 2, 3};
    unsigned char out_endpoint = 0x02;
    int transferred_out;
    printf("size=%d\n", sizeof(out_data));
    if((ret = usbw_bulk_transfer(handle, out_endpoint, out_data, sizeof(out_data), &transferred_out, timeout))) {
        printf("usbw_bulk_transfer: %s\n", usbw_error_to_string(ret));
        goto exit;
    }
    printf("transferred=%d\n", transferred_out);

    unsigned char in_endpoint = 0x86;
    unsigned char in_data[100 * 1000];
    int transferred_in;
    printf("size=%d\n", sizeof(in_data));
    if((ret = usbw_bulk_transfer(handle, in_endpoint, in_data, sizeof(in_data), &transferred_in, timeout))) {
        printf("usbw_bulk_transfer: %s. transferred_in=%d\n", usbw_error_to_string(ret), transferred_in);
        goto exit;
    }
    printf("transferred=%d\n", transferred_in);
    for(int i = 0; i < transferred_in; i++) {

        if(i == 0) {
            printf("%02x ", in_data[i]);
            continue;
        }

        if((i % 16) == 0) {
            printf("\n");
        } else if((i % 8) == 0) {
            printf("   ");
        }

        printf("%02x ", in_data[i]);
    }
    printf("\n");

exit:
    usbw_close(handle);
    libusb_exit(NULL);

    return EXIT_SUCCESS;
}
