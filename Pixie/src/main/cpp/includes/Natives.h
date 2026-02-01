//

//
//======================================================================================================================
#pragma once
//======================================================================================================================
#include <Globals.h>
#include <vector>
#include <string>
#include <imgui.h>
//======================================================================================================================
std::vector<std::string> g_LogMessages;


struct MenuVariables {
    float winWidth;
    float winHeight;
    float winPosWidth;
    float winPosHeight;
} MenuVars;
//======================================================================================================================
void native_Init (JNIEnv *env, jclass clazz, jobject surface) {

    if (g_Initialized)
        return;

    g_NativeWindow = ANativeWindow_fromSurface(env, surface);

    ImGui::CreateContext();
    ImGuiStyle *style = &ImGui::GetStyle();
    style->WindowTitleAlign = ImVec2(0, 0.50);
    style->FrameBorderSize = 1;
    style->WindowRounding = 5.3f;
    style->ScrollbarRounding = 0;
    style->FramePadding = ImVec2(8, 6);
    style->ScaleAllSizes(2.0f);
    style->ScrollbarSize /= 1;
    style->WindowMinSize = ImVec2(400, 180);

    ImGuiIO *io = &ImGui::GetIO();

    ImGui_ImplAndroid_Init(g_NativeWindow);
    ImGui_ImplOpenGL3_Init(OBFUSCATE("#version 100"));

    ImFontConfig font_cfg;
    io->Fonts->AddFontFromMemoryTTF(const_cast<std::uint8_t*>(myFont), sizeof(myFont), 28);

    font_cfg.SizePixels = 28;
    io->Fonts->AddFontDefault(&font_cfg);

    g_Initialized = true;

}
//======================================================================================================================
void native_SurfaceChanged (JNIEnv *env, jclass clazz, jobject gl, jint width, jint height) {

    glWidth = width;
    glHeight = height;
    Debug_Log("W - %d | H - %d", width, height);
    glViewport(0, 0, width, height);

    ImGuiIO *io = &ImGui::GetIO();
    io->DisplaySize = ImVec2((float)width, (float)height);

}
//======================================================================================================================
void native_Tick(JNIEnv *env, jclass clazz, jobject thiz) {

    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame(glWidth, glHeight);
    ImGui::NewFrame();


    MenuVars.winWidth = glWidth * 0.95f;
    MenuVars.winHeight = glHeight * 0.80f;
    MenuVars.winPosWidth = glWidth * 0.05f;
    MenuVars.winPosHeight = glHeight * 0.10f;

    ImGui::SetNextWindowPos(ImVec2(MenuVars.winPosWidth, MenuVars.winPosHeight), ImGuiCond_FirstUseEver);
    ImGui::SetNextWindowSize({MenuVars.winWidth, MenuVars.winHeight});

    ImGui::SetNextWindowCollapsed(false, ImGuiCond_Once);


    ImGui::Begin(OBFUSCATE("Terminal View @androidrepublic.org"));


    ImGui::BeginChild("TerminalOutput", ImVec2(0, 0), true, ImGuiWindowFlags_HorizontalScrollbar);
    for (const auto& log : g_LogMessages) {
        ImGui::TextUnformatted(log.c_str());
    }

    if (ImGui::GetScrollY() >= ImGui::GetScrollMaxY())
        ImGui::SetScrollHereY(1.0f);
    ImGui::EndChild();

    ImGui::StyleColorsDark();
    ImGui::End();

    ImGui::Render();
    glClear(GL_COLOR_BUFFER_BIT);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}
//======================================================================================================================
void native_Shutdown (JNIEnv *env, jclass clazz) {

    if (!g_Initialized)
        return;

    g_Initialized = false;
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplAndroid_Shutdown();
    ImGui::DestroyContext();
    ANativeWindow_release(g_NativeWindow);
    g_LogMessages.clear();

}
//======================================================================================================================
jboolean native_Initialized (JNIEnv *env, jclass clazz) {
    return g_Initialized;
}
//======================================================================================================================
jstring native_stringFromJNI (JNIEnv *env, jclass clazz) {
    std::string jniString = "🥴";
    return env->NewStringUTF(jniString.c_str());
}
//======================================================================================================================
void native_AddLog(JNIEnv *env, jclass clazz, jstring logMessage) {
    const char* log = env->GetStringUTFChars(logMessage, nullptr);
    if (log) {
        g_LogMessages.emplace_back(log);
        env->ReleaseStringUTFChars(logMessage, log);
    }
}
//======================================================================================================================
int registerNativeFunctions (JNIEnv *env) {

    JNINativeMethod methods[] = {
            {OBFUSCATE("Init"), OBFUSCATE("(Landroid/view/Surface;)V"), reinterpret_cast<void *>(native_Init)},
            {OBFUSCATE("SurfaceChanged"), OBFUSCATE("(Ljavax/microedition/khronos/opengles/GL10;II)V"), reinterpret_cast<void *>(native_SurfaceChanged)},
            {OBFUSCATE("Tick"), OBFUSCATE("(Lorg/muffin/imgui/muffin/MuffinSurface;)V"), reinterpret_cast<void *>(native_Tick)},
            {OBFUSCATE("Shutdown"), OBFUSCATE("()V"), reinterpret_cast<void *>(native_Shutdown)},
            {OBFUSCATE("Initialized"), OBFUSCATE("()Z"), reinterpret_cast<void *>(native_Initialized)},
            {OBFUSCATE("AddLog"), OBFUSCATE("(Ljava/lang/String;)V"), reinterpret_cast<void *>(native_AddLog)}
    };

    jclass clazz = env->FindClass(OBFUSCATE("org/muffin/imgui/muffin/MuffinSurface"));
    if (!clazz)
        return -1;

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) != 0)
        return -1;

    JNINativeMethod moreMethods[] = {
            {OBFUSCATE("stringFromJNI"), OBFUSCATE("()Ljava/lang/String;"), reinterpret_cast<void *>(native_stringFromJNI)}
    };

    jclass anotherClazz = env->FindClass(OBFUSCATE("org/muffin/imgui/MainActivity"));
    if (!anotherClazz)
        return -1;

    if (env->RegisterNatives(anotherClazz, moreMethods, sizeof(moreMethods) / sizeof(moreMethods[0])) != 0)
        return -1;

    return 0;
}
//======================================================================================================================