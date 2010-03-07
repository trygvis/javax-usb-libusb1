#include "javalibusb1_libusb1.h"
#include "usbw.h"
#include <stdlib.h>

/*
 * This code has an implicit assumption on that sizeof(int) == sizeof(void*).
 */

/* javalibusb1.Libusb1UsbServices */
jfieldID usb_services_context_field;

/* javalibusb1.impl.Libusb1UsbDevice */
jclass libusb1UsbDeviceClass = NULL;
jmethodID libusb1UsbDeviceConstructor = NULL;
jfieldID device_libusb_device_field;

/* javalibusb1.impl.Libusb1UsbConfiguration */
jclass libusb1UsbConfigurationClass = NULL;
jmethodID libusb1UsbConfigurationConstructor = NULL;

/* javalibusb1.impl.Libusb1UsbInterface */
jclass libusb1UsbInterfaceClass = NULL;
jmethodID libusb1UsbInterfaceConstructor = NULL;
jfieldID interface_libusb_device_field;

/* javalibusb1.impl.Libusb1UsbEndpoint */
jclass libusb1UsbEndpointClass = NULL;
jmethodID libusb1UsbEndpointConstructor = NULL;

/* javax.usb.UsbDeviceDescriptor */
jclass usbDeviceDescriptorClass = NULL;

/* javax.usb.UsbConfiguration */
jclass usbConfigurationClass = NULL;

/* javax.usb.UsbInterface */
jclass usbInterfaceClass = NULL;
jclass usbInterfaceArrayClass = NULL;

/* javax.usb.UsbInterfaceDescriptor */
jclass usbInterfaceDescriptorClass = NULL;

/* javax.usb.UsbEndpoint */
jclass usbEndpointClass = NULL;

/* javax.usb.UsbEndpointDescriptor */
jclass usbEndpointDescriptorClass = NULL;

/* javax.usb.UsbPlatformException */
jclass usbPlatformExceptionClass = NULL;
jmethodID usbPlatformExceptionConstructorMsgCode = NULL;

/* javax.usb.UsbDisconnectedException */
jclass usbDisconnectedExceptionClass = NULL;

/* javax.usb.impl.DefaultUsbDeviceDescriptor */
jclass defaultUsbDeviceDescriptorClass = NULL;
jmethodID defaultUsbDeviceDescriptorConstructor = NULL;

/* javax.usb.impl.DefaultUsbConfigurationDescriptor */
jclass defaultUsbConfigurationDescriptorClass = NULL;
jmethodID defaultUsbConfigurationDescriptorConstructor = NULL;

/* javax.usb.impl.DefaultUsbInterfaceDescriptor */
jclass defaultUsbInterfaceDescriptorClass = NULL;
jmethodID defaultUsbInterfaceDescriptorConstructor = NULL;

/* javax.usb.impl.DefaultUsbEndpointDescriptor */
jclass defaultUsbEndpointDescriptorClass = NULL;
jmethodID defaultUsbEndpointDescriptorConstructor = NULL;

struct usb_services_context {
    struct libusb_context* libusb_context;
};

/*
TODO: Make a construct() method that looks up and add a reference to the class objects and a
destroy() method that releases them again. These should be called by the constructor so that the JVM
will basically all of the reference counting.

usb_init() should be called as a part of the static initializer.
*/

static jclass findAndReferenceClass(JNIEnv *env, const char* name) {
    printf("Loading class %s\n", name);
    jclass klass = (*env)->FindClass(env, name);

    if(klass == NULL) {
        (*env)->ExceptionClear(env);
        printf("Error finding class %s\n", name);
        (*env)->FatalError(env, name);
        return NULL;
    }

    klass = (jclass) (*env)->NewGlobalRef(env, klass);

    if(klass == NULL) {
        (*env)->ExceptionClear(env);
        printf("Error adding reference to class %s\n", name);
        (*env)->FatalError(env, name);
        return NULL;
    }

    return klass;
}

static void unreferenceClass(JNIEnv *env, jclass* klass) {
    (*env)->DeleteGlobalRef(env, *klass);
    *klass = NULL;
}

static void throwPlatformException(JNIEnv *env, const char *message)
{
    (*env)->ThrowNew(env, usbPlatformExceptionClass, message);
}

static void throwPlatformExceptionMsgCode(JNIEnv *env, const char *message, int errorCode)
{
    if(errorCode == LIBUSB_ERROR_NO_DEVICE) {
        (*env)->ThrowNew(env, usbDisconnectedExceptionClass, message);
        return;
    }

    jstring s = (*env)->NewStringUTF(env, message);
    jobject e = (*env)->NewObject(env, usbPlatformExceptionClass, usbPlatformExceptionConstructorMsgCode, s, (jint)errorCode);
    (*env)->Throw(env, e);
}

static jobject config_descriptor2java(JNIEnv *env, const struct libusb_config_descriptor* config_descriptor) {
    return (*env)->NewObject(env, defaultUsbConfigurationDescriptorClass, defaultUsbConfigurationDescriptorConstructor,
        config_descriptor->bConfigurationValue,
        config_descriptor->bmAttributes,
        config_descriptor->MaxPower,
        config_descriptor->bNumInterfaces,
        config_descriptor->iConfiguration,
        config_descriptor->wTotalLength);
}

static jobject interface_descriptor2java(JNIEnv *env, const struct libusb_interface_descriptor *interface_descriptor) {
    return (*env)->NewObject(env, defaultUsbInterfaceDescriptorClass, defaultUsbInterfaceDescriptorConstructor,
        interface_descriptor->bAlternateSetting,
        interface_descriptor->bInterfaceClass,
        interface_descriptor->bInterfaceNumber,
        interface_descriptor->bInterfaceProtocol,
        interface_descriptor->bInterfaceSubClass,
        interface_descriptor->bNumEndpoints,
        interface_descriptor->iInterface);
}

static jobject endpoint_descriptor2java(JNIEnv *env, const struct libusb_endpoint_descriptor *endpoint_descriptor) {
    return (*env)->NewObject(env, defaultUsbEndpointDescriptorClass, defaultUsbEndpointDescriptorConstructor,
        endpoint_descriptor->bEndpointAddress,
        endpoint_descriptor->bInterval,
        endpoint_descriptor->bmAttributes,
        endpoint_descriptor->wMaxPacketSize);
}

static jobject config_descriptor2usbConfiguration(JNIEnv *env, jobject usbDevice, const struct libusb_config_descriptor* config_descriptor, jboolean known_active, int config_value) {
    const struct libusb_interface *interface = NULL;
    const struct libusb_interface_descriptor *interface_descriptor;
    const struct libusb_endpoint_descriptor *endpoint_descriptor;
    jobject usbConfiguration;
    jobject usbConfigurationDescriptor;
    jobjectArray interfacesArrayArray, interfacesArray;
    jobject usbInterface;
    jboolean interface_active;
    jobject usbInterfaceDescriptor;
    jobject usbEndpoint;
    jobject usbEndpointDescriptor;
    jobjectArray endpoints;
    int i, j, k;

    if((usbConfigurationDescriptor = config_descriptor2java(env, config_descriptor)) == NULL) {
        return NULL;
    }

    if((interfacesArrayArray = (*env)->NewObjectArray(env, config_descriptor->bNumInterfaces, usbInterfaceArrayClass, NULL)) == NULL) {
        return NULL;
    }


    // If the device is not known to be active, but the bConfigurationValue matches the currently
    // active configuration value, then it's active
    if(!known_active && config_value == config_descriptor->bConfigurationValue) {
        known_active = JNI_TRUE;
    }

    usbConfiguration = (*env)->NewObject(env, libusb1UsbConfigurationClass, libusb1UsbConfigurationConstructor,
        usbDevice, usbConfigurationDescriptor, interfacesArrayArray, known_active);
    if(usbConfiguration == NULL) {
        return NULL;
    }

    for(i = 0; i < config_descriptor->bNumInterfaces; i++) {
        interface = &config_descriptor->interface[i];

        if((interfacesArray = (*env)->NewObjectArray(env, interface->num_altsetting, usbInterfaceClass, NULL)) == NULL) {
            return NULL;
        }

        (*env)->SetObjectArrayElement(env, interfacesArrayArray, i, interfacesArray);
        if((*env)->ExceptionCheck(env)) {
            return NULL;
        }

        for(j = 0; j < interface->num_altsetting; j++) {
            interface_descriptor = &interface->altsetting[j];

            usbInterfaceDescriptor = interface_descriptor2java(env, interface_descriptor);
            if(usbInterfaceDescriptor == NULL) {
                return NULL;
            }

            if((endpoints = (*env)->NewObjectArray(env, interface_descriptor->bNumEndpoints, usbEndpointClass, NULL)) == NULL) {
                return NULL;
            }

            // I'm not sure if this is a correct assumption, but right now it just sets
            // the first interface to be the active one.
            interface_active = j == 0;

            usbInterface = (*env)->NewObject(env, libusb1UsbInterfaceClass, libusb1UsbInterfaceConstructor,
                usbConfiguration, usbInterfaceDescriptor, endpoints, interface_active);
            if(usbInterface == NULL) {
                return NULL;
            }

            for(k = 0; k < interface_descriptor->bNumEndpoints; k++) {
                endpoint_descriptor = &interface_descriptor->endpoint[k];
                usbEndpointDescriptor = endpoint_descriptor2java(env, endpoint_descriptor);
                if(usbEndpointDescriptor == NULL) {
                    return NULL;
                }

                usbEndpoint = (*env)->NewObject(env, libusb1UsbEndpointClass, libusb1UsbEndpointConstructor,
                    usbInterface, endpoint_descriptor->bDescriptorType, usbEndpointDescriptor);

                (*env)->SetObjectArrayElement(env, endpoints, k, usbEndpoint);
                if((*env)->ExceptionCheck(env)) {
                    return NULL;
                }
            }

            (*env)->SetObjectArrayElement(env, interfacesArray, j, usbInterface);
            if((*env)->ExceptionCheck(env)) {
                return NULL;
            }
        }
    }

    return usbConfiguration;
}

JNIEXPORT void JNICALL Java_javalibusb1_libusb1_set_1trace_1calls
  (JNIEnv *env, jclass klass, jboolean on)
{
    usbw_set_trace_calls(on);
}

JNIEXPORT jobject JNICALL Java_javalibusb1_libusb1_create
  (JNIEnv *env, jclass klass)
{
    struct usb_services_context *context = NULL;

    /* Lookups */
    if((usb_services_context_field = (*env)->GetFieldID(env, klass, "usb_services_context", "I")) == NULL) {
        goto fail;
    }

    if((libusb1UsbDeviceClass = findAndReferenceClass(env, "javalibusb1/impl/Libusb1UsbDevice")) == NULL) {
        return NULL;
    }
    if((libusb1UsbDeviceConstructor = (*env)->GetMethodID(env, libusb1UsbDeviceClass, "<init>", "(IBBLjavax/usb/UsbDeviceDescriptor;)V")) == NULL) {
        return NULL;
    }
    if((device_libusb_device_field = (*env)->GetFieldID(env, libusb1UsbDeviceClass, "libusb_device", "I")) == NULL) {
        goto fail;
    }

    if((libusb1UsbConfigurationClass = findAndReferenceClass(env, "javalibusb1/impl/Libusb1UsbConfiguration")) == NULL) {
        return NULL;
    }
    if((libusb1UsbConfigurationConstructor = (*env)->GetMethodID(env, libusb1UsbConfigurationClass, "<init>", "(Ljavalibusb1/impl/Libusb1UsbDevice;Ljavax/usb/UsbConfigurationDescriptor;[[Ljavax/usb/UsbInterface;Z)V")) == NULL) {
        return NULL;
    }

    if((libusb1UsbInterfaceClass = findAndReferenceClass(env, "javalibusb1/impl/Libusb1UsbInterface")) == NULL) {
        return NULL;
    }
    if((libusb1UsbInterfaceConstructor = (*env)->GetMethodID(env, libusb1UsbInterfaceClass, "<init>", "(Ljavalibusb1/impl/Libusb1UsbConfiguration;Ljavax/usb/UsbInterfaceDescriptor;[Ljavax/usb/UsbEndpoint;Z)V")) == NULL) {
        return NULL;
    }
    if((interface_libusb_device_field = (*env)->GetFieldID(env, libusb1UsbInterfaceClass, "libusb_device", "I")) == NULL) {
        goto fail;
    }

    if((libusb1UsbEndpointClass = findAndReferenceClass(env, "javalibusb1/impl/Libusb1UsbEndpoint")) == NULL) {
        return NULL;
    }
    if((libusb1UsbEndpointConstructor = (*env)->GetMethodID(env, libusb1UsbEndpointClass, "<init>", "(Ljavalibusb1/impl/Libusb1UsbInterface;BLjavax/usb/UsbEndpointDescriptor;)V")) == NULL) {
        return NULL;
    }

    if((usbDeviceDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbDeviceDescriptor")) == NULL) {
        return NULL;
    }

    if((usbConfigurationClass = findAndReferenceClass(env, "javax/usb/UsbConfiguration")) == NULL) {
        return NULL;
    }

    if((usbInterfaceClass = findAndReferenceClass(env, "javax/usb/UsbInterface")) == NULL) {
        return NULL;
    }
    if((usbInterfaceArrayClass = findAndReferenceClass(env, "[Ljavax/usb/UsbInterface;")) == NULL) {
        return NULL;
    }

    if((usbInterfaceDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbInterfaceDescriptor")) == NULL) {
        return NULL;
    }

    if((usbEndpointClass = findAndReferenceClass(env, "javax/usb/UsbEndpoint")) == NULL) {
        return NULL;
    }

    if((usbEndpointDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbEndpointDescriptor")) == NULL) {
        return NULL;
    }

    if((usbPlatformExceptionClass = findAndReferenceClass(env, "javax/usb/UsbPlatformException")) == NULL) {
        return NULL;
    }
    if((usbPlatformExceptionConstructorMsgCode = (*env)->GetMethodID(env, usbPlatformExceptionClass, "<init>", "(Ljava/lang/String;I)V")) == NULL) {
        return NULL;
    }

    if((usbDisconnectedExceptionClass = findAndReferenceClass(env, "javax/usb/UsbDisconnectedException")) == NULL) {
        return NULL;
    }

    if((defaultUsbDeviceDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbDeviceDescriptor")) == NULL) {
        return NULL;
    }
    if((defaultUsbDeviceDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbDeviceDescriptorClass, "<init>", "(SBBBBSSSBBBB)V")) == NULL) {
        return NULL;
    }

    if((defaultUsbConfigurationDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbConfigurationDescriptor")) == NULL) {
        return NULL;
    }
    if((defaultUsbConfigurationDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbConfigurationDescriptorClass, "<init>", "(BBBBBS)V")) == NULL) {
        return NULL;
    }

    if((defaultUsbInterfaceDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbInterfaceDescriptor")) == NULL) {
        return NULL;
    }
    if((defaultUsbInterfaceDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbInterfaceDescriptorClass, "<init>", "(BBBBBBB)V")) == NULL) {
        return NULL;
    }

    if((defaultUsbEndpointDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbEndpointDescriptor")) == NULL) {
        return NULL;
    }
    if((defaultUsbEndpointDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbEndpointDescriptorClass, "<init>", "(BBBS)V")) == NULL) {
        return NULL;
    }

    /* Initialization */
    context = malloc(sizeof(struct usb_services_context));

    if(context == NULL) {
        (*env)->FatalError(env, "Unable to allocate context.");
        goto fail;
    }

    if(usbw_init(&context->libusb_context)) {
        throwPlatformException(env, "Unable to initialize libusb");
        goto fail;
    }

    jmethodID c = (*env)->GetMethodID(env, klass, "<init>", "(I)V");

    return (*env)->NewObject(env, klass, c, context);

fail:
    free(context);
    return NULL;
}

JNIEXPORT void JNICALL Java_javalibusb1_libusb1_close
    (JNIEnv *env, jobject obj)
{
    struct usb_services_context *context = (struct usb_services_context*)(*env)->GetIntField(env, obj, usb_services_context_field);

    unreferenceClass(env, &libusb1UsbDeviceClass);
    unreferenceClass(env, &libusb1UsbConfigurationClass);
    unreferenceClass(env, &libusb1UsbInterfaceClass);
    unreferenceClass(env, &libusb1UsbEndpointClass);
    unreferenceClass(env, &usbDeviceDescriptorClass);
    unreferenceClass(env, &usbConfigurationClass);
    unreferenceClass(env, &usbInterfaceClass);
    unreferenceClass(env, &usbInterfaceArrayClass);
    unreferenceClass(env, &usbInterfaceDescriptorClass);
    unreferenceClass(env, &usbEndpointClass);
    unreferenceClass(env, &usbEndpointDescriptorClass);
    unreferenceClass(env, &usbPlatformExceptionClass);
    unreferenceClass(env, &usbDisconnectedExceptionClass);
    unreferenceClass(env, &defaultUsbDeviceDescriptorClass);
    unreferenceClass(env, &defaultUsbConfigurationDescriptorClass);
    unreferenceClass(env, &defaultUsbInterfaceDescriptorClass);
    unreferenceClass(env, &defaultUsbEndpointDescriptorClass);

    usbw_exit(context->libusb_context);
    free(context);
}

JNIEXPORT void JNICALL Java_javalibusb1_libusb1_set_1debug
    (JNIEnv *env, jobject obj, jint level)
{
    struct usb_services_context* context;

    context = (struct usb_services_context*)(*env)->GetIntField(env, obj, usb_services_context_field);

    usbw_set_debug(context->libusb_context, level);
}

JNIEXPORT jobjectArray JNICALL Java_javalibusb1_libusb1_get_1devices
    (JNIEnv *env, jobject obj)
{
    struct libusb_device **devices;
    struct libusb_device *d;
    struct libusb_device_descriptor descriptor;
    struct usb_services_context *context;
    int i;
    ssize_t size;
    jobject usbDeviceDescriptor;
    jobject usbDevice;
    uint8_t busNumber, deviceAddress;

    context = (struct usb_services_context*)(*env)->GetIntField(env, obj, usb_services_context_field);

    size = usbw_get_device_list(context->libusb_context, &devices);

    jobjectArray usbDevices = (*env)->NewObjectArray(env, size, libusb1UsbDeviceClass, NULL);
    for(i = 0; i < size; i++) {
        d = devices[i];

        busNumber = usbw_get_bus_number(d);
        deviceAddress = usbw_get_device_address(d);

        if(usbw_get_device_descriptor(d, &descriptor)) {
            throwPlatformException(env, "libusb_get_device_descriptor()");
            goto fail;
        }

        usbDeviceDescriptor = (*env)->NewObject(env, defaultUsbDeviceDescriptorClass, defaultUsbDeviceDescriptorConstructor,
            (jshort) descriptor.bcdUSB,
            (jbyte) descriptor.bDeviceClass,
            (jbyte) descriptor.bDeviceSubClass,
            (jbyte) descriptor.bDeviceProtocol,
            (jbyte) descriptor.bMaxPacketSize0,
            (jshort) descriptor.idVendor,
            (jshort) descriptor.idProduct,
            (jshort) descriptor.bcdDevice,
            (jbyte) descriptor.iManufacturer,
            (jbyte) descriptor.iProduct,
            (jbyte) descriptor.iSerialNumber,
            (jbyte) descriptor.bNumConfigurations);

        usbDevice = (*env)->NewObject(env, libusb1UsbDeviceClass, libusb1UsbDeviceConstructor, d, busNumber, deviceAddress, usbDeviceDescriptor);

        (*env)->SetObjectArrayElement(env, usbDevices, i, usbDevice);
        if((*env)->ExceptionCheck(env)) {
            goto fail;
        }
    }

    usbw_free_device_list(devices, 0);
    return usbDevices;

fail:
    usbw_free_device_list(devices, 1);
    return NULL;
}

/*****************************************************************************
 * javalibusb1_impl_Libusb1UsbDevice
 *****************************************************************************/

JNIEXPORT void JNICALL Java_javalibusb1_impl_Libusb1UsbDevice_closeNative
    (JNIEnv *env, jobject obj)
{
    struct libusb_device *device;

    device = (struct libusb_device*)(*env)->GetIntField(env, obj, device_libusb_device_field);

    if(device == 0) {
        return;
    }

    (*env)->SetIntField(env, obj, device_libusb_device_field, 0);

    usbw_unref_device(device);
}

JNIEXPORT jstring JNICALL Java_javalibusb1_impl_Libusb1UsbDevice_getStringNative
  (JNIEnv *env, jobject obj, jbyte index, jint length)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    unsigned char *data = NULL;
    jstring s = NULL;
    int err;

    device = (struct libusb_device*)(*env)->GetIntField(env, obj, device_libusb_device_field);

    data = malloc(sizeof(unsigned char) * length);

    if(data == NULL) {
        throwPlatformException(env, "Unable to allocate buffer.");
        return s;
    }

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, "libusb_open()", err);
        goto fail;
    }

    if((err = usbw_get_string_descriptor_ascii(handle, index, data, length)) <= 0) {
        throwPlatformExceptionMsgCode(env, "libusb_get_string_descriptor_ascii()", err);
        goto fail;
    }

    usbw_close(handle);

    s = (*env)->NewStringUTF(env, (const char*)data);

fail:
    free(data);

    return s;
}

JNIEXPORT jobject JNICALL Java_javalibusb1_impl_Libusb1UsbDevice_nativeGetActiveUsbConfiguration
  (JNIEnv *env, jobject usbDevice)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    struct libusb_config_descriptor *config_descriptor = NULL;
    jobject usbConfiguration;
    int err;

    device = (struct libusb_device*)(*env)->GetIntField(env, usbDevice, device_libusb_device_field);

    /* On Darwin the device has to be open while querying for the descriptor */

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, "libusb_open()", err);
        goto fail;
    }

    if((err = usbw_get_active_config_descriptor(device, &config_descriptor))) {
        throwPlatformExceptionMsgCode(env, "libusb_get_active_config_descriptor()", err);
        goto fail;
    }

    usbConfiguration = config_descriptor2usbConfiguration(env, usbDevice, config_descriptor, JNI_TRUE, 0);

fail:
    usbw_close(handle);
    usbw_free_config_descriptor(config_descriptor);

    return usbConfiguration;
}

JNIEXPORT jobject JNICALL Java_javalibusb1_impl_Libusb1UsbDevice_nativeGetUsbConfiguration
  (JNIEnv *env, jobject usbDevice, jbyte index)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle = NULL;
    struct libusb_config_descriptor *config_descriptor = NULL;
    jobject usbConfiguration;
    int config;
    int err;

    device = (struct libusb_device*)(*env)->GetIntField(env, usbDevice, device_libusb_device_field);

    /* On Darwin the device has to be open while querying for the descriptor */

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, "libusb_open()", err);
        goto fail;
    }

    if((err = usbw_get_config_descriptor(device, index, &config_descriptor))) {
        throwPlatformExceptionMsgCode(env, "libusb_get_config_descriptor()", err);
        goto fail;
    }

    if((err = usbw_get_configuration(handle, &config))) {
        throwPlatformExceptionMsgCode(env, "libusb_get_configuration()", err);
        goto fail;
    }

    usbConfiguration = config_descriptor2usbConfiguration(env, usbDevice, config_descriptor, JNI_FALSE, config);

fail:
    usbw_close(handle);
    usbw_free_config_descriptor(config_descriptor);

    return usbConfiguration;
}

// -----------------------------------------------------------------------
//
// -----------------------------------------------------------------------

JNIEXPORT void JNICALL Java_javalibusb1_impl_Libusb1UsbInterface_nativeSetConfiguration
  (JNIEnv *env, jobject obj, jint configuration)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    int err;

    device = (struct libusb_device*)(*env)->GetIntField(env, obj, interface_libusb_device_field);

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, "libusb_open()", err);
        return;
    }

    if((err = usbw_set_configuration(handle, configuration))) {
        throwPlatformExceptionMsgCode(env, "libusb_set_configuration()", err);
        goto fail;
    };

fail:
    usbw_close(handle);
}

JNIEXPORT jint JNICALL Java_javalibusb1_impl_Libusb1UsbInterface_nativeClaimInterface
  (JNIEnv *env, jobject obj, jint bInterfaceNumber)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    int err;

    device = (struct libusb_device*)(*env)->GetIntField(env, obj, interface_libusb_device_field);

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, "libusb_open()", err);
        return 0;
    }

    if((err = usbw_claim_interface(handle, bInterfaceNumber))) {
        throwPlatformExceptionMsgCode(env, "libusb_claim_interface()", err);
        goto fail;
    };

    return (jint)handle;

fail:
    usbw_close(handle);
    return 0;
}

JNIEXPORT void JNICALL Java_javalibusb1_impl_Libusb1UsbInterface_nativeRelease
  (JNIEnv *env, jobject obj, jint handle)
{
    usbw_close((struct libusb_device_handle*)handle);
}
