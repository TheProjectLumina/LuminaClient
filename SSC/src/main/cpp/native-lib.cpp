#include <jni.h>
#include <string>
#include"valid.cpp"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_project_lumina_client_util_HashCat_getSignaturesSha1(
        JNIEnv *env,
        jobject,
        jobject contextObject) {

    return env->NewStringUTF(app_sha1);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_project_lumina_client_util_HashCat_checkSha1(
        JNIEnv *env,
        jobject,
        jobject contextObject) {

char *sha1 = getSha1(env,contextObject);

jboolean result = checkValidity(env,sha1);

return result;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_project_lumina_client_util_HashCat_getToken(
        JNIEnv *env,
        jobject,
        jobject contextObject,
        jstring userId) {
    char *sha1 = getSha1(env,contextObject);
    jboolean result = checkValidity(env,sha1);

    if(result){
        return env->NewStringUTF("获取Token成功");
    }else{
        return env->NewStringUTF("获取失败，请检查valid.cpp文件配置的sha1值");
    }
}