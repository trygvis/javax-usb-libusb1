#ifndef JAVALIBUSB_H
#define JAVALIBUSB_H

// This hack is needed when storing the context and device pointers in the Java
// objects. The pointers are stored in a jlong and has to be cast back to a
// pointer type to prevent warnings from the compiler.
// The check_jvm_platform asserts that a jlong can store the pointer.
#ifdef __LP64__
#define POINTER_STORAGE_TYPE long
#else
#define POINTER_STORAGE_TYPE int
#endif

#endif
