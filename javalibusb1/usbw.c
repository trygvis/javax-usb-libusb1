#include "usbw.h"

#include <stdio.h>
#include <stdarg.h>

/*
 * TODO: Replace "err=%s" with "ret=%s" or more appropriate message where applicable.
 *       Or maybe just "<function name>=xyz"
 */

static int trace_calls = 0;

void usbw_set_trace_calls(int on) {
    trace_calls = on;
}

void usbw_printf(const char* fmt, ...) {
    char buf[1024];
    va_list args;

    if(!trace_calls) {
        return;
    }

    va_start(args, fmt);
    vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);


    fprintf(stdout, buf);
    fflush(stdout);
}

/*************************************************************************
 * Library initialization/deinitialization
 */

void usbw_set_debug(struct libusb_context *context, int level) {
    usbw_printf("PRE: libusb_set_debug(%p, %d)\n", context, level);
    libusb_set_debug(context, level);
    usbw_printf("RET: (void)\n");
}

int usbw_init(struct libusb_context **context) {
    usbw_printf("PRE: libusb_init(%p)\n", context);
    int err = libusb_init(context);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

void usbw_exit(struct libusb_context *context) {
    usbw_printf("PRE: libusb_exit()\n");
    libusb_exit(context);
    usbw_printf("RET: (void)\n");
}

/*************************************************************************
 * Device handling and enumeration
 */

ssize_t usbw_get_device_list(struct libusb_context *context, libusb_device ***list) {
    usbw_printf("PRE: libusb_get_device_list(%p, list)\n", context);
    ssize_t size = libusb_get_device_list(context, list);
    usbw_printf("RET: size=%lu\n", size);
    return size;
}

void usbw_free_device_list(struct libusb_device **list, int unref_devices) {
    usbw_printf("PRE: libusb_free_device_list(%p, %d)\n", list, unref_devices);
    libusb_free_device_list(list, unref_devices);
    usbw_printf("RET: (void)\n");
}

uint8_t usbw_get_bus_number(struct libusb_device *device) {
    usbw_printf("PRE: libusb_get_bus_number(%p)\n", device);
    uint8_t number = libusb_get_bus_number(device);
    usbw_printf("RET: number=%u\n", number);
    return number;
}

uint8_t usbw_get_device_address(struct libusb_device *device) {
    usbw_printf("PRE: libusb_get_device_address(%p)\n", device);
    uint8_t address = libusb_get_device_address(device);
    usbw_printf("RET: address=%u\n", address);
    return address;
}

enum libusb_speed usbw_get_speed(struct libusb_device *device) {
    usbw_printf("PRE: libusb_get_speed(%p)\n", device);
    enum libusb_speed speed = libusb_get_speed(device);
    usbw_printf("RET: speed=%u\n", speed);
    return speed;
}

void usbw_unref_device(libusb_device *device) {
    usbw_printf("PRE: libusb_unref_device(%p)\n", device);
    libusb_unref_device(device);
    usbw_printf("RET: (void)\n");
}

int usbw_open(struct libusb_device *device, struct libusb_device_handle **handle) {
    usbw_printf("PRE: libusb_open(%p, %p)\n", device, handle);
    int err = libusb_open(device, handle);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

void usbw_close(struct libusb_device_handle *handle) {
    usbw_printf("PRE: libusb_close(%p)\n", handle);
    libusb_close(handle);
    usbw_printf("RET: void\n");
}

int usbw_get_configuration(struct libusb_device_handle *handle, int *config) {
    usbw_printf("PRE: libusb_get_configuration(%p, %p)\n", handle, config);
    int err = libusb_get_configuration(handle, config);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

int usbw_set_configuration(struct libusb_device_handle *handle, int configuration) {
    usbw_printf("PRE: libusb_set_configuration(%p, %d)\n", handle, configuration);
    int err = libusb_set_configuration(handle, configuration);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

int usbw_claim_interface(struct libusb_device_handle *device, int interface_number) {
    usbw_printf("PRE: libusb_claim_interface(%p, %d)\n", device, interface_number);
    int err = libusb_claim_interface(device, interface_number);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

/*************************************************************************
 * USB descriptors
 */

int usbw_get_device_descriptor(struct libusb_device *device, struct libusb_device_descriptor *descriptor) {
    usbw_printf("PRE: libusb_get_device_descriptor(%p, %p)\n", device, descriptor);
    int err = libusb_get_device_descriptor(device, descriptor);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

int usbw_get_active_config_descriptor(struct libusb_device *device, struct libusb_config_descriptor **configuration) {
    usbw_printf("PRE: libusb_get_active_config_descriptor(%p, %p)\n", device, configuration);
    int err = libusb_get_active_config_descriptor(device, configuration);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

int usbw_get_config_descriptor(struct libusb_device *device, uint8_t config_index, struct libusb_config_descriptor **configuration) {
    usbw_printf("PRE: libusb_get_config_descriptor(%p, %d, %p)\n", device, config_index, configuration);
    int err = libusb_get_config_descriptor(device, config_index, configuration);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

void usbw_free_config_descriptor(struct libusb_config_descriptor *configuration) {
    usbw_printf("PRE: libusb_free_config_descriptor(%p)\n", configuration);
    libusb_free_config_descriptor(configuration);
    usbw_printf("RET: void\n");
}

int usbw_get_string_descriptor_ascii(struct libusb_device_handle *handle, uint8_t desc_index, unsigned char *data, int length) {
    usbw_printf("PRE: libusb_get_string_descriptor_ascii(%p, %u, %p, %d)\n", handle, desc_index, data, length);
    int err = libusb_get_string_descriptor_ascii(handle, desc_index, data, length);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

/*************************************************************************
 * Synchronous device I/O
 */

int usbw_control_transfer(struct libusb_device_handle *handle, uint8_t bmRequestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, unsigned char *data, uint16_t wLength, unsigned int timeout) {
    usbw_printf("PRE: libusb_control_transfer(%p, %u, %u, %u, %u, %p, %u)\n", handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout);
    int err = libusb_control_transfer(handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

int usbw_bulk_transfer(struct libusb_device_handle *handle, unsigned char endpoint, unsigned char *data, int length, int *transferred, unsigned int timeout) {
    usbw_printf("PRE: libusb_bulk_transfer(%p, %u, %p, %d, %p, %u)\n", handle, endpoint, data, length, transferred, timeout);
    int err = libusb_bulk_transfer(handle, endpoint, data, length, transferred, timeout);
    usbw_printf("RET: err=%d\n", err);
    return err;
}

int usbw_interrupt_transfer(struct libusb_device_handle *handle, unsigned char endpoint, unsigned char *data, int length, int *transferred, unsigned int timeout);
