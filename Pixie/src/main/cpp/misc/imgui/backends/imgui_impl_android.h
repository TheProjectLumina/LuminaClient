



















#pragma once
#include "imgui.h"
#ifndef IMGUI_DISABLE

struct ANativeWindow;
struct AInputEvent;

IMGUI_IMPL_API bool     ImGui_ImplAndroid_Init(ANativeWindow* window);
IMGUI_IMPL_API int32_t  ImGui_ImplAndroid_HandleInputEvent(AInputEvent* input_event);
IMGUI_IMPL_API void     ImGui_ImplAndroid_Shutdown();
IMGUI_IMPL_API void     ImGui_ImplAndroid_NewFrame();
IMGUI_IMPL_API void     ImGui_ImplAndroid_NewFrame(int screen_width = 0, int screen_height = 0);

#endif
