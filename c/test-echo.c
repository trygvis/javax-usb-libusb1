#include "usbw.h"

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <sys/time.h>
#include <unistd.h>
#include <fcntl.h>

int main(int argc, char* argv[]) {
    int trace_calls = 0;
    int libusb_debug_level = 0;
    size_t buffer_size = 1024;

    char c;
    while ((c = getopt(argc, argv, "t:l:z:")) != -1) {
        switch(c) {
            case 't':
                trace_calls = strtol(optarg, NULL, 10);
                break;
            case 'l':
                libusb_debug_level = strtol(optarg, NULL, 10);
                break;
            case 'z':
                buffer_size = strtol(optarg, NULL, 10);
                break;
        }
    }

    int ret;

    usbw_set_trace_calls(trace_calls);

    if(usbw_init(NULL)) {
        perror("libusb_init");
        return EXIT_FAILURE;
    }

    usbw_set_debug(NULL, libusb_debug_level);

    libusb_device_handle* handle = NULL;
    uint16_t vendor_id = 0x0547;
    uint16_t product_id = 0xff01;

    if((handle = usbw_open_device_with_vid_pid(NULL, vendor_id, product_id)) == NULL) {
        printf("libusb_open_device_with_vid_pid: device not found\n");
        goto done;
    }

    int interface = 0;

    if((ret = usbw_claim_interface(handle, interface))) {
        printf("usbw_claim_interface: %s\n", usbw_error_to_string(ret));
        goto done;
    }

    unsigned int timeout = 1000;
    size_t transfer_count = 0;
    size_t bytes_in_count = 0;
    size_t bytes_out_count = 0;
    struct timeval start, end;

    unsigned char* out_data;
    unsigned char out_endpoint = 0x02;
    int transferred_out;
    unsigned char in_endpoint = 0x86;
    unsigned char* in_data;
    int in_size;
    int transferred_in;

    gettimeofday(&start, NULL);
    out_data = malloc(buffer_size);
    in_data = malloc(buffer_size);

    ssize_t left;

    int fd = fcntl(STDIN_FILENO,  F_DUPFD, 0);

    while(1) {
        left = read(fd, out_data, buffer_size);
//        printf("read=%d\n", left);
        in_size = left;

        if(left == 0) {
            break;
        }

        while(left > 0) {
            if((ret = usbw_bulk_transfer(handle, out_endpoint, out_data, left, &transferred_out, timeout))) {
                printf("usbw_bulk_transfer: %s, transferred_out=%d\n", usbw_error_to_string(ret), transferred_out);
                goto done;
            }
            left -= transferred_out;
            bytes_out_count += transferred_out;
            if(left == 0) {
//                printf("DONE: transferred_out=%d\n", transferred_out);
            }
            else {
                printf("INCOMPLETE: transferred_out=%d, left=%d\n", transferred_out, left);
            }
        }

//        printf("size=%d\n", in_size);
        if((ret = usbw_bulk_transfer(handle, in_endpoint, in_data, in_size, &transferred_in, timeout))) {
            printf("usbw_bulk_transfer: %s, transferred_in=%d\n", usbw_error_to_string(ret), transferred_in);
            goto done;
        }

        bytes_in_count += transferred_in;
        transfer_count++;
        printf("transferred_in=%d\n", transferred_in);

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
    }

done:
    gettimeofday(&end, NULL);
    printf("Transfers:     %d\n", transfer_count);
    printf("Bytes out:     %d\n", bytes_out_count);
    printf("Bytes in:      %d\n", bytes_in_count);
    float total_secs = end.tv_sec-start.tv_sec;
    total_secs += ((float)end.tv_usec-start.tv_usec) / 1000000;
    printf("Time:          %.2fs\n", total_secs);
    printf("Data rate:     %.0fB/s, %.0fkB/s\n", ((float)bytes_out_count) / total_secs, ((float)bytes_out_count) / total_secs / 1024);
    printf("Transfer rate: %.2f transfers/s\n", ((float)transfer_count) / total_secs);

    usbw_close(handle);
    libusb_exit(NULL);

    return EXIT_SUCCESS;
}
