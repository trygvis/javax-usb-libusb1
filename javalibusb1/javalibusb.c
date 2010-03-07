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
jfieldID libusb_device_field;

/* javalibusb1.impl.Libusb1UsbConfiguration */
jclass libusb1UsbConfigurationClass = NULL;
jmethodID libusb1UsbConfigurationConstructor = NULL;

/* javalibusb1.impl.Libusb1UsbInterface */
jclass libusb1UsbInterfaceClass = NULL;
jmethodID libusb1UsbInterfaceConstructor = NULL;

/* javax.usb.UsbDeviceDescriptor */
jclass usbDeviceDescriptorClass = NULL;

/* javax.usb.UsbConfiguration */
jclass usbConfigurationClass = NULL;

/* javax.usb.UsbInterface */
jclass usbInterfaceClass = NULL;
jclass usbInterfaceArrayClass = NULL;

/* javax.usb.UsbInterfaceDescriptor */
jclass usbInterfaceDescriptorClass = NULL;

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

static jobject interface_descriptor2java(JNIEnv *env, const struct libusb_interface_descriptor* interface_descriptor) {
    return (*env)->NewObject(env, defaultUsbInterfaceDescriptorClass, defaultUsbInterfaceDescriptorConstructor,
        interface_descriptor->bAlternateSetting,
        interface_descriptor->bInterfaceClass,
        interface_descriptor->bInterfaceNumber,
        interface_descriptor->bInterfaceProtocol,
        interface_descriptor->bInterfaceSubClass,
        interface_descriptor->bNumEndpoints,
        interface_descriptor->iInterface);
}

static jobject config_descriptor2usbConfiguration(JNIEnv *env, jobject usbDevice, const struct libusb_config_descriptor* config_descriptor, jboolean known_active, int config_value) {
    const struct libusb_interface *interface = NULL;
    jobject usbConfigurationDescriptor;
    jobjectArray interfacesArrayArray, interfacesArray;
    jobject usbInterfaceDescriptor;
    jobjectArray usbInterfaceDescriptors;
    jobject usbInterface;
    jboolean interface_active;
    int i, j;

    j = 0; usbInterfaceDescriptors = NULL; usbInterfaceDescriptor = NULL;
    if((usbConfigurationDescriptor = config_descriptor2java(env, config_descriptor)) == NULL) {
        return NULL;
    }

    if((interfacesArrayArray = (*env)->NewObjectArray(env, config_descriptor->bNumInterfaces, usbInterfaceArrayClass, NULL)) == NULL) {
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
            usbInterfaceDescriptor = interface_descriptor2java(env, &interface->altsetting[j]);
            if(usbInterfaceDescriptor == NULL) {
                return NULL;
            }

            // I'm not sure if this is a correct assumption, but right now it just sets
            // the first interface to be the active one.
            interface_active = j == 0;

            usbInterface = (*env)->NewObject(env, libusb1UsbInterfaceClass, libusb1UsbInterfaceConstructor,
                usbInterfaceDescriptors, usbInterfaceDescriptor, interface_active);
            if(usbInterface == NULL) {
                return NULL;
            }
    
            (*env)->SetObjectArrayElement(env, interfacesArray, j, usbInterface);
            if((*env)->ExceptionCheck(env)) {
                return NULL;
            }

        }
    }

    // If the device is not known to be active, but the bConfigurationValue matches the currently
    // active configuration value, then it's active
    if(!known_active && config_value == config_descriptor->bConfigurationValue) {
        known_active = JNI_TRUE;
    }

    return (*env)->NewObject(env, libusb1UsbConfigurationClass, libusb1UsbConfigurationConstructor,
        usbDevice, usbConfigurationDescriptor, interfacesArrayArray, known_active);
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
    if((libusb_device_field = (*env)->GetFieldID(env, libusb1UsbDeviceClass, "libusb_device", "I")) == NULL) {
        goto fail;
    }

    if((libusb1UsbConfigurationClass = findAndReferenceClass(env, "javalibusb1/impl/Libusb1UsbConfiguration")) == NULL) {
        return NULL;
    }
    if((libusb1UsbConfigurationConstructor = (*env)->GetMethodID(env, libusb1UsbConfigurationClass, "<init>", "(Ljavax/usb/UsbDevice;Ljavax/usb/UsbConfigurationDescriptor;[[Ljavax/usb/UsbInterface;Z)V")) == NULL) {
        return NULL;
    }

    if((libusb1UsbInterfaceClass = findAndReferenceClass(env, "javalibusb1/impl/Libusb1UsbInterface")) == NULL) {
        return NULL;
    }
    if((libusb1UsbInterfaceConstructor = (*env)->GetMethodID(env, libusb1UsbInterfaceClass, "<init>", "(Ljavax/usb/UsbConfiguration;Ljavax/usb/UsbInterfaceDescriptor;Z)V")) == NULL) {
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
    unreferenceClass(env, &usbDeviceDescriptorClass);
    unreferenceClass(env, &usbConfigurationClass);
    unreferenceClass(env, &usbInterfaceClass);
    unreferenceClass(env, &usbInterfaceArrayClass);
    unreferenceClass(env, &usbInterfaceDescriptorClass);
    unreferenceClass(env, &usbPlatformExceptionClass);
    unreferenceClass(env, &usbDisconnectedExceptionClass);
    unreferenceClass(env, &defaultUsbDeviceDescriptorClass);
    unreferenceClass(env, &defaultUsbConfigurationDescriptorClass);
    unreferenceClass(env, &defaultUsbInterfaceDescriptorClass);

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

    device = (struct libusb_device*)(*env)->GetIntField(env, obj, libusb_device_field);

    if(device == 0) {
        return;
    }

    (*env)->SetIntField(env, obj, libusb_device_field, 0);

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

    device = (struct libusb_device*)(*env)->GetIntField(env, obj, libusb_device_field);

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

    device = (struct libusb_device*)(*env)->GetIntField(env, usbDevice, libusb_device_field);

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

    device = (struct libusb_device*)(*env)->GetIntField(env, usbDevice, libusb_device_field);

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
