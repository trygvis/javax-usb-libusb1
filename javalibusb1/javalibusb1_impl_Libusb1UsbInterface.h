/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class javalibusb1_impl_Libusb1UsbInterface */

#ifndef _Included_javalibusb1_impl_Libusb1UsbInterface
#define _Included_javalibusb1_impl_Libusb1UsbInterface
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     javalibusb1_impl_Libusb1UsbInterface
 * Method:    nativeSetConfiguration
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_javalibusb1_impl_Libusb1UsbInterface_nativeSetConfiguration
  (JNIEnv *, jobject, jint);

/*
 * Class:     javalibusb1_impl_Libusb1UsbInterface
 * Method:    nativeClaimInterface
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_javalibusb1_impl_Libusb1UsbInterface_nativeClaimInterface
  (JNIEnv *, jobject, jint);

/*
 * Class:     javalibusb1_impl_Libusb1UsbInterface
 * Method:    nativeRelease
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_javalibusb1_impl_Libusb1UsbInterface_nativeRelease
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
