/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_afei_camera2getpreview_util_NativeLibrary */

#ifndef _Included_com_afei_camera2getpreview_util_NativeLibrary
#define _Included_com_afei_camera2getpreview_util_NativeLibrary
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_afei_camera2getpreview_util_NativeLibrary
 * Method:    yuv420p2rgba
 * Signature: ([BII[B)V
 */
JNIEXPORT void JNICALL Java_com_afei_camera2getpreview_util_NativeLibrary_yuv420p2rgba
  (JNIEnv *, jclass, jbyteArray, jint, jint, jbyteArray);

JNIEXPORT int JNICALL Java_com_afei_camera2getpreview_util_NativeLibrary_checkSelectResult
        (JNIEnv *env, jclass type, jbyteArray yuv420p_, jint width, jint height, jbyteArray result);

#ifdef __cplusplus
}
#endif
#endif
