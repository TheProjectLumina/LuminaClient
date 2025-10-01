//

//
//======================================================================================================================
#include <Globals.h>
#include <Natives.h>
#include <Hooks.h>
//======================================================================================================================
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *_env;

    if (vm->GetEnv(reinterpret_cast<void **>(&_env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    if (registerNativeFunctions(_env) != 0) {
        return -1;
    }

    Debug_Log("| 1 | dl_open libinput");
    void *g_Input = xdl_open(OBFUSCATE("libinput.so"), 0);
    if (g_Input) {
        Debug_Log("| 1 | pointer is 0x%08x", g_Input);
        Debug_Log("| 1 | dl_sym _ZN7android13InputConsumer21initializeMotionEventEPNS_11MotionEventEPKNS_12InputMessageE");
        void *inputSymbolPointer = (void *) xdl_sym(g_Input, OBFUSCATE("_ZN7android13InputConsumer21initializeMotionEventEPNS_11MotionEventEPKNS_12InputMessageE"), nullptr);
        if(inputSymbolPointer) {
            Debug_Log("| 1 | pointer is 0x%08x", inputSymbolPointer);
            Debug_Log("| 1 | hook _ZN7android13InputConsumer21initializeMotionEventEPNS_11MotionEventEPKNS_12InputMessageE");
            MUFF(inputSymbolPointer, hook_Input, orig_Input);
        }
    }
    Debug_Log("| 1 | dl_close libinput");
    xdl_close(g_Input);

    return JNI_VERSION_1_6;
}
//======================================================================================================================