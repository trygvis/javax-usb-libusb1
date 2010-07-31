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

    fprintf(stderr, buf);
    fflush(stderr);
}

const char *usbw_error_to_string(enum libusb_error error)
{
    switch (error) {
    case LIBUSB_SUCCESS:
        return "LIBUSB_SUCCESS";
    case LIBUSB_ERROR_IO:
        return "LIBUSB_ERROR_IO";
    case LIBUSB_ERROR_INVALID_PARAM:
        return "LIBUSB_ERROR_INVALID_PARAM";
    case LIBUSB_ERROR_ACCESS:
        return "LIBUSB_ERROR_ACCESS";
    case LIBUSB_ERROR_NO_DEVICE:
        return "LIBUSB_ERROR_NO_DEVICE";
    case LIBUSB_ERROR_NOT_FOUND:
        return "LIBUSB_ERROR_NOT_FOUND";
    case LIBUSB_ERROR_BUSY:
        return "LIBUSB_ERROR_BUSY";
    case LIBUSB_ERROR_TIMEOUT:
        return "LIBUSB_ERROR_TIMEOUT";
    case LIBUSB_ERROR_OVERFLOW:
        return "LIBUSB_ERROR_OVERFLOW";
    case LIBUSB_ERROR_PIPE:
        return "LIBUSB_ERROR_PIPE";
    case LIBUSB_ERROR_INTERRUPTED:
        return "LIBUSB_ERROR_INTERRUPTED";
    case LIBUSB_ERROR_NO_MEM:
        return "LIBUSB_ERROR_NO_MEM";
    case LIBUSB_ERROR_NOT_SUPPORTED:
        return "LIBUSB_ERROR_NOT_SUPPORTED";
    case LIBUSB_ERROR_OTHER:
        return "LIBUSB_ERROR_OTHER";
    default:
        return "libusb_error: Unknown error";
    }
}

/*************************************************************************
 * Library initialization/deinitialization
 */

void usbw_set_debug(struct libusb_context *context, int level) {
    usbw_printf("PRE: %s(%p, %d)\n", __func__, context, level);
    libusb_set_debug(context, level);
    usbw_printf("RET: %s: (void)\n", __func__);
}

int usbw_init(struct libusb_context **context) {
    usbw_printf("PRE: %s(%p)\n", __func__, context);
    int err = libusb_init(context);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

void usbw_exit(struct libusb_context *context) {
    usbw_printf("PRE: %s()\n", __func__);
    libusb_exit(context);
    usbw_printf("RET: %s: (void)\n", __func__);
}

/*************************************************************************
 * Device handling and enumeration
 */

ssize_t usbw_get_device_list(struct libusb_context *context, libusb_device ***list) {
    usbw_printf("PRE: %s(%p, list)\n", __func__, context);
    ssize_t size = libusb_get_device_list(context, list);
    usbw_printf("RET: %s: size=%lu\n", __func__, size);
    return size;
}

void usbw_free_device_list(struct libusb_device **list, int unref_devices) {
    usbw_printf("PRE: %s(%p, %d)\n", __func__, list, unref_devices);
    libusb_free_device_list(list, unref_devices);
    usbw_printf("RET: %s: (void)\n", __func__);
}

uint8_t usbw_get_bus_number(struct libusb_device *device) {
    usbw_printf("PRE: %s(%p)\n", __func__, device);
    uint8_t number = libusb_get_bus_number(device);
    usbw_printf("RET: %s: number=%u\n", __func__, number);
    return number;
}

uint8_t usbw_get_device_address(struct libusb_device *device) {
    usbw_printf("PRE: %s(%p)\n", __func__, device);
    uint8_t address = libusb_get_device_address(device);
    usbw_printf("RET: %s: address=%u\n", __func__, address);
    return address;
}

enum libusb_speed usbw_get_speed(struct libusb_device *device) {
    usbw_printf("PRE: %s(%p)\n", __func__, device);
    enum libusb_speed speed = libusb_get_speed(device);
    usbw_printf("RET: %s: speed=%u\n", __func__, speed);
    return speed;
}

void usbw_unref_device(libusb_device *device) {
    usbw_printf("PRE: %s(%p)\n", __func__, device);
    libusb_unref_device(device);
    usbw_printf("RET: %s: (void)\n", __func__);
}

int usbw_open(struct libusb_device *device, struct libusb_device_handle **handle) {
    usbw_printf("PRE: %s(%p, %p)\n", __func__, device, handle);
    int err = libusb_open(device, handle);
    usbw_printf("RET: %s: handle=%p err=%d (%s)\n", __func__, *handle, err, usbw_error_to_string(err));
    return err;
}

struct libusb_device_handle* usbw_open_device_with_vid_pid(struct libusb_context* context, uint16_t vendor_id, uint16_t product_id) {
    usbw_printf("PRE: %s(%p, 0x%0hx, 0x%0hx)\n", __func__, context, vendor_id, product_id);
    struct libusb_device_handle* handle = libusb_open_device_with_vid_pid(context, vendor_id, product_id);
    usbw_printf("RET: %s: %s: handle=%p\n", __func__, handle);
    return handle;
}

void usbw_close(struct libusb_device_handle *handle) {
    usbw_printf("PRE: %s(%p)\n", __func__, handle);
    libusb_close(handle);
    usbw_printf("RET: %s: void\n", __func__);
}

int usbw_get_configuration(struct libusb_device_handle *handle, int *config) {
    usbw_printf("PRE: %s(%p, %p)\n", __func__, handle, config);
    int err = libusb_get_configuration(handle, config);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

int usbw_set_configuration(struct libusb_device_handle *handle, int configuration) {
    usbw_printf("PRE: %s(%p, %d)\n", __func__, handle, configuration);
    int err = libusb_set_configuration(handle, configuration);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

int usbw_claim_interface(struct libusb_device_handle *device, int interface_number) {
    usbw_printf("PRE: %s(%p, %d)\n", __func__, device, interface_number);
    int err = libusb_claim_interface(device, interface_number);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

int usbw_reset_device(struct libusb_device_handle *device) {
    usbw_printf("PRE: %s(%p)\n", __func__, device);
    int err = libusb_reset_device(device);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

/*************************************************************************
 * USB descriptors
 */

int usbw_get_device_descriptor(struct libusb_device *device, struct libusb_device_descriptor *descriptor) {
    usbw_printf("PRE: %s(%p, %p)\n", __func__, device, descriptor);
    int err = libusb_get_device_descriptor(device, descriptor);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

int usbw_get_active_config_descriptor(struct libusb_device *device, struct libusb_config_descriptor **configuration) {
    usbw_printf("PRE: %s(%p, %p)\n", __func__, device, configuration);
    int err = libusb_get_active_config_descriptor(device, configuration);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

int usbw_get_config_descriptor(struct libusb_device *device, uint8_t config_index, struct libusb_config_descriptor **configuration) {
    usbw_printf("PRE: %s(%p, %d, %p)\n", __func__, device, config_index, configuration);
    int err = libusb_get_config_descriptor(device, config_index, configuration);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

void usbw_free_config_descriptor(struct libusb_config_descriptor *configuration) {
    usbw_printf("PRE: %s(%p)\n", __func__, configuration);
    libusb_free_config_descriptor(configuration);
    usbw_printf("RET: %s: void\n", __func__);
}

int usbw_get_string_descriptor_ascii(struct libusb_device_handle *handle, uint8_t desc_index, unsigned char *data, int length) {
    usbw_printf("PRE: %s(%p, %u, %p, %d)\n", __func__, handle, desc_index, data, length);
    int err = libusb_get_string_descriptor_ascii(handle, desc_index, data, length);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

/*************************************************************************
 * Synchronous device I/O
 */

int usbw_control_transfer(struct libusb_device_handle *handle, uint8_t bmRequestType, uint8_t bRequest, uint16_t wValue, uint16_t wIndex, unsigned char *data, uint16_t wLength, unsigned int timeout) {
    usbw_printf("PRE: %s(%p, %u, %u, %u, %u, %p, %u)\n", __func__, handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout);
    int err = libusb_control_transfer(handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}

int usbw_bulk_transfer(struct libusb_device_handle *handle, unsigned char endpoint, unsigned char *data, int length, int *transferred, unsigned int timeout) {
    usbw_printf("PRE: %s(%p, %u, %p, %d, %p, %u)\n", __func__, handle, endpoint, data, length, transferred, timeout);
    int err = libusb_bulk_transfer(handle, endpoint, data, length, transferred, timeout);
    usbw_printf("RET: %s: err=%d (%s)\n", __func__, err, usbw_error_to_string(err));
    return err;
}
