#include "javalibusb0_libusb0.h"
#include <usb.h>

jclass libusb0BusClass = NULL;
jmethodID libusb0BusConstructor = NULL;

jclass libusb0DeviceClass = NULL;
jmethodID libusb0DeviceConstructor = NULL;

jclass usbDeviceDescriptorClass = NULL;

jclass defaultUsbDeviceDescriptorClass = NULL;
jmethodID defaultUsbDeviceDescriptorConstructor = NULL;

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
        (*env)->FatalError(env, name);
        printf("Error finding class %s\n", name);
        return NULL;
    }

    klass = (jclass) (*env)->NewGlobalRef(env, klass);

    if(klass == NULL) {
        (*env)->ExceptionClear(env);
        (*env)->FatalError(env, name);
        printf("Error adding reference to class %s\n", name);
        return NULL;
    }

    return klass;
}

JNIEXPORT void JNICALL Java_javalibusb0_libusb0_init
    (JNIEnv *env, jclass class) {
    printf("Java_javalibusb0_libusb0_init\n");

    libusb0BusClass = findAndReferenceClass(env, "javalibusb0/Libusb0Bus");

    if((libusb0BusConstructor = (*env)->GetMethodID(env, libusb0BusClass, "<init>", "(J[Ljavalibusb0/Libusb0Device;)V")) == NULL) {
        return;
    }

    libusb0DeviceClass = findAndReferenceClass(env, "javalibusb0/Libusb0Device");

    if((libusb0DeviceConstructor = (*env)->GetMethodID(env, libusb0DeviceClass, "<init>", "(Ljava/lang/String;Ljavax/usb/UsbDeviceDescriptor;)V")) == NULL) {
        return;
    }

    usbDeviceDescriptorClass = findAndReferenceClass(env, "javax/usb/UsbDeviceDescriptor");

    defaultUsbDeviceDescriptorClass = findAndReferenceClass(env, "javax/usb/impl/DefaultUsbDeviceDescriptor");
    if((defaultUsbDeviceDescriptorConstructor = (*env)->GetMethodID(env, defaultUsbDeviceDescriptorClass, "<init>", "(SBBBBSSSBBBB)V")) == NULL) {
        return;
    }

    printf("usb_init()\n");
    usb_init();
}

static jobject usb_deviceToJava(JNIEnv *env, struct usb_device *device) {
    jstring filename = (*env)->NewStringUTF(env, device->filename);

    jobject usbDeviceDescriptor = (*env)->NewObject(env, defaultUsbDeviceDescriptorClass, defaultUsbDeviceDescriptorConstructor,
        (jshort) device->descriptor.bcdUSB,
        (jbyte) device->descriptor.bDeviceClass,
        (jbyte) device->descriptor.bDeviceSubClass,
        (jbyte) device->descriptor.bDeviceProtocol,
        (jbyte) device->descriptor.bMaxPacketSize0,
        (jshort) device->descriptor.idVendor,
        (jshort) device->descriptor.idProduct,
        (jshort) device->descriptor.bcdDevice,
        (jbyte) device->descriptor.iManufacturer,
        (jbyte) device->descriptor.iProduct,
        (jbyte) device->descriptor.iSerialNumber,
        (jbyte) device->descriptor.bNumConfigurations);

    if(usbDeviceDescriptor == NULL) {
        return NULL;
    }

    return (*env)->NewObject(env, libusb0DeviceClass, libusb0DeviceConstructor, filename, usbDeviceDescriptor);
}

JNIEXPORT jint JNICALL Java_javalibusb0_libusb0_jusb_1find_1busses
    (JNIEnv *env, jclass class) {
    return usb_find_busses();
}

JNIEXPORT jint JNICALL Java_javalibusb0_libusb0_jusb_1find_1devices
    (JNIEnv *env, jclass class) {
    return usb_find_devices();
}

JNIEXPORT jobjectArray JNICALL Java_javalibusb0_libusb0_jusb_1get_1busses
    (JNIEnv *env, jclass class) {
    printf("Java_javalibusb0_libusb0_jusb_1get_1busses\n");

    jsize busCount;

    printf("usb_get_busses()\n");
    struct usb_bus *bus = usb_get_busses();

    for(busCount = 0; bus; busCount++, bus = bus->next);

    jobjectArray usbBusses = (*env)->NewObjectArray(env, busCount, libusb0BusClass, NULL);
    if(usbBusses == NULL) {
        return usbBusses;
    }

    printf("usb_get_busses()\n");
    bus = usb_get_busses();
    for(busCount = 0; bus; busCount++, bus = bus->next) {
        printf("busCount=%d, bus->location=%d\n", busCount, bus->location);
        struct usb_device* device = bus->devices;

        int deviceCount;
        for(deviceCount = 0; device; deviceCount++, device = device->next);
        printf("deviceCount=%d\n", deviceCount);

        device = bus->devices;
        jobjectArray usbDevices = (*env)->NewObjectArray(env, deviceCount, libusb0DeviceClass, NULL);
        if(usbDevices == NULL) {
            return usbDevices;
        }
        for(deviceCount = 0; device; deviceCount++, device = device->next) {
            jobject javaDescriptor = usb_deviceToJava(env, device);

            if(javaDescriptor == NULL) {
                return NULL;
            }

            (*env)->SetObjectArrayElement(env, usbDevices, deviceCount, javaDescriptor);
        }

        jobject usbBus = (*env)->NewObject(env, libusb0BusClass, libusb0BusConstructor, (jlong)bus->location, usbDevices);
        (*env)->SetObjectArrayElement(env, usbBusses, busCount, usbBus);
        if((*env)->ExceptionCheck(env)) {
            return NULL;
        }
    }

    return usbBusses;
}
