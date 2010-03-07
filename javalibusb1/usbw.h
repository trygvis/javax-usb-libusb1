#ifndef USB_WRAPPER_H
#define USB_WRAPPER_H

#include <libusb.h>

void usbw_set_trace_calls(int on);

/*************************************************************************
 * Library initialization/deinitialization
 */

void usbw_set_debug(struct libusb_context *context, int level);
int usbw_init(struct libusb_context **context);
void usbw_exit(struct libusb_context *context);

/*************************************************************************
 * Device handling and enumeration
 */

ssize_t usbw_get_device_list(struct libusb_context *context, libusb_device ***list);
void usbw_free_device_list(struct libusb_device **list, int unref_devices);
uint8_t usbw_get_bus_number(struct libusb_device *device);
uint8_t usbw_get_device_address(struct libusb_device *device);
void usbw_unref_device(struct libusb_device *device);
int usbw_open(struct libusb_device *device, struct libusb_device_handle **handle);
void usbw_close(struct libusb_device_handle *handle);
int usbw_get_configuration(struct libusb_device_handle *handle, int *config);
int usbw_set_configuration(struct libusb_device_handle *handle, int configuration);
int usbw_claim_interface(struct libusb_device_handle *device, int interface_number);

/*************************************************************************
 * USB descriptors
 */

int usbw_get_device_descriptor(struct libusb_device *device, struct libusb_device_descriptor *descriptor);
int usbw_get_active_config_descriptor(struct libusb_device *device, struct libusb_config_descriptor **configuration);
int usbw_get_config_descriptor(struct libusb_device *device, uint8_t config_index, struct libusb_config_descriptor **configuration);
void usbw_free_config_descriptor(struct libusb_config_descriptor *configuration);
int usbw_get_string_descriptor_ascii(struct libusb_device_handle *handle, uint8_t desc_index, unsigned char *data, int length);

#endif
