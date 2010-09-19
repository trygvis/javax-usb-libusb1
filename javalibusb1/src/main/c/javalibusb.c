#include "javalibusb1_libusb1.h"
#include "usbw.h"
#include <stdlib.h>
#include <stdarg.h>
#include "javalibusb.h"

/* javalibusb1.Libusb1UsbDevice */
jclass libusb1UsbDeviceClass = NULL;
jmethodID libusb1UsbDeviceConstructor = NULL;
jmethodID libusb1UsbDeviceSetConfiguration = NULL;
jmethodID libusb1UsbDeviceSetActiveConfiguration = NULL;

/* javalibusb1.Libusb1UsbConfiguration */
jclass libusb1UsbConfigurationClass = NULL;
jmethodID libusb1UsbConfigurationConstructor = NULL;

/* javalibusb1.Libusb1UsbInterface */
jclass libusb1UsbInterfaceClass = NULL;
jmethodID libusb1UsbInterfaceConstructor = NULL;

/* javalibusb1.Libusb1UsbEndpoint */
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
jmethodID usbDisconnectedExceptionConstructorMsg = NULL;

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

/*
TODO: Make a construct() method that looks up and add a reference to the class objects and a
destroy() method that releases them again. These should be called by the constructor so that the JVM
will basically all of the reference counting.

usb_init() should be called as a part of the static initializer.
*/

static void releaseReferences(JNIEnv *env);

static jclass findAndReferenceClass(JNIEnv *env, const char* name) {
    fprintf(stderr, "Loading class %s\n", name);
    fflush(stderr);
    jclass klass = (*env)->FindClass(env, name);

    if(klass == NULL) {
        (*env)->ExceptionClear(env);
        fprintf(stderr, "Error finding class %s\n", name);
        fflush(stderr);
        (*env)->FatalError(env, name);
        return NULL;
    }

    klass = (jclass) (*env)->NewGlobalRef(env, klass);

    if(klass == NULL) {
        (*env)->ExceptionClear(env);
        fprintf(stderr, "Error adding reference to class %s\n", name);
        fflush(stderr);
        (*env)->FatalError(env, name);
        return NULL;
    }

    return klass;
}

static void unreferenceClass(JNIEnv *env, jclass* klass) {
    if(klass == NULL) {
        return;
    }

    (*env)->DeleteGlobalRef(env, *klass);
    *klass = NULL;
}

static void throwPlatformException(JNIEnv *env, const char *message)
{
    (*env)->ThrowNew(env, usbPlatformExceptionClass, message);
}

static void throwPlatformExceptionMsgCode(JNIEnv *env, int errorCode, const char *format, ...)
{
    char buf[1024];
    va_list ap;
    va_start(ap, format);
    (void)vsnprintf(buf, 1024, format, ap);
    va_end(ap);

    jstring s = (*env)->NewStringUTF(env, buf);

    jobject e;
    if(errorCode == LIBUSB_ERROR_NO_DEVICE) {
        e = (*env)->NewObject(env, usbDisconnectedExceptionClass, usbDisconnectedExceptionConstructorMsg, s);
    }
    else {
        e = (*env)->NewObject(env, usbPlatformExceptionClass, usbPlatformExceptionConstructorMsgCode, s, (jint)errorCode);
    }

    if(e == NULL) {
        return; // failed to allocate an exception
    }

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
                    usbInterface, usbEndpointDescriptor);
                if(usbEndpoint == NULL) {
                    return NULL;
                }

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

/**
 * Initializes the library in three phases:
 *
 * I) Create a libusb context
 * II) Look up all the required Java classes, fields and methods
 * III) Create the libusb object and return it.
 *
 * TODO: To prevent possible bugs, consider passing all Class as a reference
 * to the classhas to be registered with the JVM and unregistered when closed.
 * An alternative is to put all references in a list (libusb has an
 * implementation) and just iterate that on close().
 */
JNIEXPORT jobject JNICALL Java_javalibusb1_libusb1_create
  (JNIEnv *env, jclass klass, jint debug_level)
{
    struct libusb_context *context;

    /* Initalization, Phase I */
    if(usbw_init(&context)) {
        throwPlatformException(env, "Unable to initialize libusb.");
        goto fail;
    }

    usbw_set_debug(context, debug_level);

    /* Initalization, Phase II */
    // TODO: Create some macros to do the lookups
    /* Lookups */
    if((libusb1UsbDeviceClass = findAndReferenceClass(env, "javalibusb1/Libusb1UsbDevice")) == NULL) {
        goto fail;
    }
    if((libusb1UsbDeviceConstructor = (*env)->GetMethodID(env, libusb1UsbDeviceClass, "<init>", "(JBBILjavax/usb/UsbDeviceDescriptor;)V")) == NULL) {
        goto fail;
    }
    if((libusb1UsbDeviceSetConfiguration = (*env)->GetMethodID(env, libusb1UsbDeviceClass, "_setConfiguration", "(Ljavax/usb/UsbConfiguration;B)V")) == NULL) {
        goto fail;
    }
    if((libusb1UsbDeviceSetActiveConfiguration = (*env)->GetMethodID(env, libusb1UsbDeviceClass, "_setActiveConfiguration", "(B)V")) == NULL) {
        goto fail;
    }

    if((libusb1UsbConfigurationClass = findAndReferenceClass(env, "javalibusb1/Libusb1UsbConfiguration")) == NULL) {
        goto fail;
    }
    if((libusb1UsbConfigurationConstructor = (*env)->GetMethodID(env, libusb1UsbConfigurationClass, "<init>", "(Ljavalibusb1/Libusb1UsbDevice;Ljavax/usb/UsbConfigurationDescriptor;[[Ljavax/usb/UsbInterface;Z)V")) == NULL) {
        goto fail;
    }

    if((libusb1UsbInterfaceClass = findAndReferenceClass(env, "javalibusb1/Libusb1UsbInterface")) == NULL) {
        goto fail;
    }
    if((libusb1UsbInterfaceConstructor = (*env)->GetMethodID(env, libusb1UsbInterfaceClass, "<init>", "(Ljavalibusb1/Libusb1UsbConfiguration;Ljavax/usb/UsbInterfaceDescriptor;[Ljavax/usb/UsbEndpoint;Z)V")) == NULL) {
        goto fail;
    }

    if((libusb1UsbEndpointClass = findAndReferenceClass(env, "javalibusb1/Libusb1UsbEndpoint")) == NULL) {
        goto fail;
    }
    if((libusb1UsbEndpointConstructor = (*env)->GetMethodID(env, libusb1UsbEndpointClass, "<init>", "(Ljavalibusb1/Libusb1UsbInterface;Ljavax/usb/UsbEndpointDescriptor;)V")) == NULL) {
        goto fail;
    }

    if((usbDeviceDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbDeviceDescriptor")) == NULL) {
        goto fail;
    }

    if((usbConfigurationClass = findAndReferenceClass(env, "javax/usb/UsbConfiguration")) == NULL) {
        goto fail;
    }

    if((usbInterfaceClass = findAndReferenceClass(env, "javax/usb/UsbInterface")) == NULL) {
        goto fail;
    }
    if((usbInterfaceArrayClass = findAndReferenceClass(env, "[Ljavax/usb/UsbInterface;")) == NULL) {
        goto fail;
    }

    if((usbInterfaceDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbInterfaceDescriptor")) == NULL) {
        goto fail;
    }

    if((usbEndpointClass = findAndReferenceClass(env, "javax/usb/UsbEndpoint")) == NULL) {
        goto fail;
    }

    if((usbEndpointDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbEndpointDescriptor")) == NULL) {
        goto fail;
    }

    if((usbPlatformExceptionClass = findAndReferenceClass(env, "javax/usb/UsbPlatformException")) == NULL) {
        goto fail;
    }
    if((usbPlatformExceptionConstructorMsgCode = (*env)->GetMethodID(env, usbPlatformExceptionClass, "<init>", "(Ljava/lang/String;I)V")) == NULL) {
        goto fail;
    }

    if((usbDisconnectedExceptionClass = findAndReferenceClass(env, "javax/usb/UsbDisconnectedException")) == NULL) {
        goto fail;
    }
    if((usbDisconnectedExceptionConstructorMsg = (*env)->GetMethodID(env, usbDisconnectedExceptionClass, "<init>", "(Ljava/lang/String;)V")) == NULL) {
        goto fail;
    }

    if((defaultUsbDeviceDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbDeviceDescriptor")) == NULL) {
        goto fail;
    }
    if((defaultUsbDeviceDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbDeviceDescriptorClass, "<init>", "(SBBBBSSSBBBB)V")) == NULL) {
        goto fail;
    }

    if((defaultUsbConfigurationDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbConfigurationDescriptor")) == NULL) {
        goto fail;
    }
    if((defaultUsbConfigurationDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbConfigurationDescriptorClass, "<init>", "(BBBBBS)V")) == NULL) {
        goto fail;
    }

    if((defaultUsbInterfaceDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbInterfaceDescriptor")) == NULL) {
        goto fail;
    }
    if((defaultUsbInterfaceDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbInterfaceDescriptorClass, "<init>", "(BBBBBBB)V")) == NULL) {
        goto fail;
    }

    if((defaultUsbEndpointDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbEndpointDescriptor")) == NULL) {
        goto fail;
    }
    if((defaultUsbEndpointDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbEndpointDescriptorClass, "<init>", "(BBBS)V")) == NULL) {
        goto fail;
    }

    /* Initialization, phase III */
    jmethodID c = (*env)->GetMethodID(env, klass, "<init>", "(J)V");

    if((*env)->ExceptionCheck(env)) {
        goto fail;
    }

    return (*env)->NewObject(env, klass, c, context);

fail:
    releaseReferences(env);
    return NULL;
}

JNIEXPORT void JNICALL Java_javalibusb1_libusb1_close
    (JNIEnv *env, jobject obj, jlong java_context)
{
    struct libusb_context *context = (struct libusb_context *)(POINTER_STORAGE_TYPE)java_context;

    releaseReferences(env);

    usbw_exit(context);
}

static void releaseReferences(JNIEnv *env) {
    // In the opposite order of referencing
    unreferenceClass(env, &defaultUsbEndpointDescriptorClass);
    unreferenceClass(env, &defaultUsbInterfaceDescriptorClass);
    unreferenceClass(env, &defaultUsbConfigurationDescriptorClass);
    unreferenceClass(env, &defaultUsbDeviceDescriptorClass);
    unreferenceClass(env, &usbDisconnectedExceptionClass);
    unreferenceClass(env, &usbPlatformExceptionClass);
    unreferenceClass(env, &usbEndpointDescriptorClass);
    unreferenceClass(env, &usbEndpointClass);
    unreferenceClass(env, &usbInterfaceDescriptorClass);
    unreferenceClass(env, &usbInterfaceArrayClass);
    unreferenceClass(env, &usbInterfaceClass);
    unreferenceClass(env, &usbConfigurationClass);
    unreferenceClass(env, &usbDeviceDescriptorClass);
    unreferenceClass(env, &libusb1UsbEndpointClass);
    unreferenceClass(env, &libusb1UsbInterfaceClass);
    unreferenceClass(env, &libusb1UsbConfigurationClass);
    unreferenceClass(env, &libusb1UsbDeviceClass);
}

JNIEXPORT void JNICALL Java_javalibusb1_libusb1_set_1debug
    (JNIEnv *env, jobject obj, jlong java_context, jint level)
{
    struct libusb_context *context = (struct libusb_context *)(POINTER_STORAGE_TYPE)java_context;

    usbw_set_debug(context, level);
}

int load_configurations(JNIEnv *env, struct libusb_device *device, uint8_t bNumConfigurations, jobject usbDevice, struct libusb_device_descriptor descriptor) {
    int err;
    struct libusb_device_handle* handle;

    err = usbw_open(device, &handle);

    // On Darwin, the device has to be open while querying for the descriptor
    if(err) {
        // On Linux, you will get on some devices. Just ignore those devices.
        if(err == LIBUSB_ERROR_ACCESS) {
            return 1;
        }

        throwPlatformExceptionMsgCode(env, err, "libusb_open(): %s", usbw_error_to_string(err));
        return 1;
    }

    int config;
    if((err = usbw_get_configuration(handle, &config))) {
        // This happens on OSX with Apple's IR Receiver which almost always is suspended
        // fprintf(stderr, "**** get_configuration: could not get descriptor with index %d of %d in total. Skipping device %04x:%04x, err=%s\n", index, bNumConfigurations, descriptor.idVendor, descriptor.idProduct, usbw_error_to_string(err));
        // fflush(stderr);
        // throwPlatformExceptionMsgCode(env, err, "libusb_get_configuration(): %s", usbw_error_to_string(err));
        usbw_close(handle);
        return 1;
    }

    (*env)->CallVoidMethod(env, usbDevice, libusb1UsbDeviceSetActiveConfiguration, config);
    if((*env)->ExceptionCheck(env)) {
        return 1;
    }

    struct libusb_config_descriptor *config_descriptor = NULL;
    int index;
    for(index = 0; index < bNumConfigurations; index++) {
        if((err = usbw_get_config_descriptor(device, index, &config_descriptor))) {
            throwPlatformExceptionMsgCode(env, err, "libusb_get_config_descriptor(): %s", usbw_error_to_string(err));
            break;
        }

        jobject usbConfiguration = config_descriptor2usbConfiguration(env, usbDevice, config_descriptor, JNI_FALSE, config);
        usbw_free_config_descriptor(config_descriptor);
        if(usbConfiguration == NULL || (*env)->ExceptionCheck(env)) {
            break;
        }

        (*env)->CallVoidMethod(env, usbDevice, libusb1UsbDeviceSetConfiguration, usbConfiguration, index + 1);
        if((*env)->ExceptionCheck(env)) {
            break;
        }
    }

    usbw_close(handle);

    return 0;
}

JNIEXPORT jobjectArray JNICALL Java_javalibusb1_libusb1_get_1devices
    (JNIEnv *env, jobject obj, jlong libusb_context_ptr)
{
    struct libusb_context *context = (struct libusb_context *)(POINTER_STORAGE_TYPE)libusb_context_ptr;
    struct libusb_device **devices;
    struct libusb_device *device;
    struct libusb_device_descriptor descriptor;
    int i;
    ssize_t size;
    jobject usbDevice;
    uint8_t busNumber, deviceAddress;
    jint speed;
    jobject usbDeviceDescriptor;
    int failed = 0;

    size = usbw_get_device_list(context, &devices);
    if(size < 0) {
        throwPlatformExceptionMsgCode(env, size, "libusb_get_device_list(): %s", usbw_error_to_string(size));
        return NULL;
    }

    jobjectArray usbDevices = (*env)->NewObjectArray(env, size, libusb1UsbDeviceClass, NULL);
    if(usbDevices == NULL) {
        return NULL;
    }
    for(i = 0; i < size; i++) {
        device = devices[i];

        busNumber = usbw_get_bus_number(device);
        deviceAddress = usbw_get_device_address(device);

        if(usbw_get_device_descriptor(device, &descriptor)) {
            throwPlatformException(env, "libusb_get_device_descriptor()");
            failed = 1;
            break;
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
        if(usbDeviceDescriptor == NULL) {
            return NULL;
        }

        switch (usbw_get_speed(device)) {
            case LIBUSB_SPEED_LOW:
                speed = 1;
                break;
            case LIBUSB_SPEED_FULL:
                speed = 2;
                break;
            case LIBUSB_SPEED_HIGH:
                speed = 3;
                break;
            default:
                speed = 0;
        }

        jlong device_ptr = (jlong) (POINTER_STORAGE_TYPE) device;
        usbDevice = (*env)->NewObject(env, libusb1UsbDeviceClass,
            libusb1UsbDeviceConstructor,
            device_ptr,
            busNumber,
            deviceAddress,
            speed,
            usbDeviceDescriptor);
        if(usbDevice == NULL || (*env)->ExceptionCheck(env)) {
            return NULL;
        }

        int failed = load_configurations(env, device, descriptor.bNumConfigurations, usbDevice, descriptor);

        // Ignore devices that failed and did *not* throw an exception
        if((*env)->ExceptionCheck(env)) {
            failed = 1;
            break;
        }

        if(failed) {
            continue;
        }

        (*env)->SetObjectArrayElement(env, usbDevices, i, usbDevice);
        if((*env)->ExceptionCheck(env)) {
            failed = 1;
            break;
        }
    }

    if(failed) {
        usbw_free_device_list(devices, 1);
        usbDevices = NULL;
    }
    else {
        usbw_free_device_list(devices, 0);
    }

    return usbDevices;
}

JNIEXPORT jint JNICALL Java_javalibusb1_libusb1_control_1transfer
  (JNIEnv *env, jclass klass, jlong libusb_device_ptr, jbyte bmRequestType, jbyte bRequest, jshort wValue, jshort wIndex, jlong timeout, jbyteArray bytes, jint offset, jshort length)
{
    int err;
    struct libusb_device* device;
    struct libusb_device_handle *handle = NULL;
    uint8_t* data = NULL;
    uint16_t wLength;

    device = (struct libusb_device*)(POINTER_STORAGE_TYPE)libusb_device_ptr;

    data = malloc(length);
    if(data == NULL) {
        throwPlatformExceptionMsgCode(env, err, "Unable to allocate memory buffer");
        goto fail;
    }

    // If this is an OUT transfer, copy the bytes to data
    if(!(bmRequestType & LIBUSB_ENDPOINT_DIR_MASK)) {
        (*env)->GetByteArrayRegion(env, bytes, offset, length, (jbyte*)data);
        if((*env)->ExceptionCheck(env)) {
            goto fail;
        }
    }

    wLength = length;

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, err, "libusb_open(): %s", usbw_error_to_string(err));
        goto fail;
    }

    if((err = usbw_control_transfer(handle, bmRequestType, bRequest, wValue, wIndex, data, wLength, timeout)) < 0) {
        throwPlatformExceptionMsgCode(env, err, "libusb_control_transfer(): %s", usbw_error_to_string(err));
        goto fail;
    }

    // If this is an IN transfer, copy the data to bytes
    if(bmRequestType & LIBUSB_ENDPOINT_DIR_MASK) {
        (*env)->SetByteArrayRegion(env, bytes, offset, length, (jbyte*)data);
        if((*env)->ExceptionCheck(env)) {
            goto fail;
        }
    }

fail:
    if(data) {
        free(data);
    }
    if(handle) {
        usbw_close(handle);
    }

    return err;
}

enum transferType {
    transferTypeBulk,
    transferTypeInterrupt
};

static jint bulk_or_interrupt_transfer(enum transferType transferType, JNIEnv *env, jclass klass, jlong libusb_device_handle_ptr, jbyte bEndpointAddress, jbyteArray bytes, jint offset, jint length, jlong timeout)
{
    int err;
    struct libusb_device_handle *handle = (struct libusb_device_handle *)(POINTER_STORAGE_TYPE)libusb_device_handle_ptr;
    uint8_t* data = NULL;
    int transferred;

    data = malloc(length);
    if(data == NULL) {
        throwPlatformExceptionMsgCode(env, err, "Unable to allocate memory.");
        goto fail;
    }

    // If this is an OUT transfer, copy the bytes to data
    if(!(bEndpointAddress & LIBUSB_ENDPOINT_DIR_MASK)) {
        (*env)->GetByteArrayRegion(env, bytes, offset, length, (jbyte*)data);
        if((*env)->ExceptionCheck(env)) {
            goto fail;
        }
    }

    switch(transferType) {
        case transferTypeBulk:
            if((err = usbw_bulk_transfer(handle, bEndpointAddress, (unsigned char *)data + offset, length, &transferred, timeout))) {
                throwPlatformExceptionMsgCode(env, err, "libusb_bulk_transfer(): %s", usbw_error_to_string(err));
                goto fail;
            }
            break;
        case transferTypeInterrupt:
            if((err = usbw_interrupt_transfer(handle, bEndpointAddress, (unsigned char *)data + offset, length, &transferred, timeout))) {
                throwPlatformExceptionMsgCode(env, err, "libusb_interrupt_transfer(): %s", usbw_error_to_string(err));
                goto fail;
            }
            break;
    }

    // If this is an IN transfer, copy the data to bytes
    if(bEndpointAddress & LIBUSB_ENDPOINT_DIR_MASK) {
      (*env)->SetByteArrayRegion(env, bytes, offset, length, (jbyte*)data);
      if((*env)->ExceptionCheck(env)) {
          goto fail;
      }
    }

fail:
    if(data) {
        free(data);
    }

    return transferred;
}

JNIEXPORT jint JNICALL Java_javalibusb1_libusb1_bulk_1transfer
  (JNIEnv *env, jclass klass, jlong libusb_device_handle_ptr, jbyte bEndpointAddress, jbyteArray bytes, jint offset, jint length, jlong timeout)
{
    return bulk_or_interrupt_transfer(transferTypeBulk, env, klass, libusb_device_handle_ptr, bEndpointAddress, bytes, offset, length, timeout);
}

JNIEXPORT jint JNICALL Java_javalibusb1_libusb1_interrupt_1transfer
  (JNIEnv *env, jclass klass, jlong libusb_device_handle_ptr, jbyte bEndpointAddress, jbyteArray bytes, jint offset, jint length, jlong timeout)
{
    return bulk_or_interrupt_transfer(transferTypeInterrupt, env, klass, libusb_device_handle_ptr, bEndpointAddress, bytes, offset, length, timeout);
}

/*****************************************************************************
 * javalibusb1_Libusb1UsbDevice
 *****************************************************************************/

JNIEXPORT void JNICALL Java_javalibusb1_Libusb1UsbDevice_nativeClose
    (JNIEnv *env, jobject obj, jlong libusb_device_ptr)
{
    struct libusb_device *device;

    device = (struct libusb_device*)(POINTER_STORAGE_TYPE)libusb_device_ptr;

    if(device == 0) {
        return;
    }

    usbw_unref_device(device);
}

JNIEXPORT jstring JNICALL Java_javalibusb1_Libusb1UsbDevice_nativeGetString
  (JNIEnv *env, jobject obj, jlong libusb_device_ptr, jbyte index, jint length)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    unsigned char *data = NULL;
    jstring s = NULL;
    int err;

    device = (struct libusb_device*)(POINTER_STORAGE_TYPE)libusb_device_ptr;

    data = malloc(sizeof(unsigned char) * length);

    if(data == NULL) {
        throwPlatformException(env, "Unable to allocate buffer.");
        return s;
    }

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, err, "libusb_open(): %s", usbw_error_to_string(err));
        goto fail;
    }

    if((err = usbw_get_string_descriptor_ascii(handle, index, data, length)) <= 0) {
        throwPlatformExceptionMsgCode(env, err, "libusb_get_string_descriptor_ascii(): d", err);
        goto fail;
    }

    usbw_close(handle);

    s = (*env)->NewStringUTF(env, (const char*)data);

fail:
    free(data);

    return s;
}


/*****************************************************************************
 * javalibusb1.Libusb1UsbInterface
 *****************************************************************************/

JNIEXPORT void JNICALL Java_javalibusb1_Libusb1UsbInterface_nativeSetConfiguration
  (JNIEnv *env, jobject obj, jlong libusb_device_ptr, jint configuration)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    int err;

    device = (struct libusb_device*)(POINTER_STORAGE_TYPE)libusb_device_ptr;

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, err, "libusb_open(): %s", usbw_error_to_string(err));
        return;
    }

    if((err = usbw_set_configuration(handle, configuration))) {
        throwPlatformExceptionMsgCode(env, err, "libusb_set_configuration(): %s", usbw_error_to_string(err));
    };

    usbw_close(handle);
}

JNIEXPORT jlong JNICALL Java_javalibusb1_Libusb1UsbInterface_nativeClaimInterface
  (JNIEnv *env, jobject obj, jlong libusb_device_ptr, jint bInterfaceNumber)
{
    struct libusb_device *device;
    struct libusb_device_handle *handle;
    int err;

    device = (struct libusb_device*)(POINTER_STORAGE_TYPE)libusb_device_ptr;

    if((err = usbw_open(device, &handle))) {
        throwPlatformExceptionMsgCode(env, err, "libusb_open(): %s", usbw_error_to_string(err));
        return 0;
    }

    if((err = usbw_claim_interface(handle, bInterfaceNumber))) {
        throwPlatformExceptionMsgCode(env, err, "libusb_claim_interface(): %s", usbw_error_to_string(err));
        goto fail;
    };

    return (long)handle;

fail:
    usbw_close(handle);
    return 0;
}

JNIEXPORT void JNICALL Java_javalibusb1_Libusb1UsbInterface_nativeRelease
  (JNIEnv *env, jobject obj, jlong libusb_device_handle_ptr)
{
    usbw_close((struct libusb_device_handle*)((POINTER_STORAGE_TYPE)libusb_device_handle_ptr));
}
