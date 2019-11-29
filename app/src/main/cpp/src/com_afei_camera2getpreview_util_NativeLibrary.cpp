#include "com_afei_camera2getpreview_util_NativeLibrary.h"
#include "ImageUtil.h"
#include "CCheckSelectResult.h"

JNIEXPORT void JNICALL Java_com_afei_camera2getpreview_util_NativeLibrary_yuv420p2rgba
        (JNIEnv *env, jclass type, jbyteArray yuv420p_, jint width, jint height, jbyteArray rgba_) {
    jbyte *yuv420p = env->GetByteArrayElements(yuv420p_, NULL);
    jbyte *rgba = env->GetByteArrayElements(rgba_, NULL);

    i420torgba(reinterpret_cast<const unsigned char *>(yuv420p), width, height, reinterpret_cast<unsigned char *>(rgba));

    env->ReleaseByteArrayElements(yuv420p_, yuv420p, 0);
    env->ReleaseByteArrayElements(rgba_, rgba, 0);
}

JNIEXPORT int JNICALL Java_com_afei_camera2getpreview_util_NativeLibrary_checkSelectResult
        (JNIEnv *env, jclass type, jbyteArray yuv420p_, jint width, jint height, jbyteArray result) {
    jbyte * pYUV420 = env->GetByteArrayElements(yuv420p_, NULL);
    jbyte * pResult = env->GetByteArrayElements(result, NULL);

    CCheckSelectResult checkResult;

    int nRC = checkResult.CheckBuffer(reinterpret_cast<unsigned char *>(pYUV420));
    for (int i = 0; i < nRC; i++) {
        *pResult++ = (char)checkResult.m_nSelResult[i][0];
        *pResult++ = (char)checkResult.m_nSelResult[i][1];
    }

    env->ReleaseByteArrayElements(yuv420p_, pYUV420, 0);
    env->ReleaseByteArrayElements(result, pResult, 0);
    return nRC;
}