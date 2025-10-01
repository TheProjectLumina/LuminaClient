/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don’t bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */

#include <jni.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <stdint.h>
#include <inttypes.h>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <sstream>
#include <vector>
#include <map>
#include <iomanip>
#include <thread>
#include <algorithm>
#include <cmath>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/resource.h>
#include <sys/uio.h>

#include <fcntl.h>
#include <android/log.h>
#include <pthread.h>
#include <dirent.h>
#include <libgen.h>

#include <sys/mman.h>
#include <sys/wait.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/un.h>

#include <codecvt>
#include <chrono>
#include <queue>

#include "ImGui/imgui_internal.h"
#include "ImGui/imgui.h"
#include "ImGui/imgui_impl_android.h"
#include "ImGui/imgui_impl_opengl3.h"

#include <EGL/egl.h>
#include <GLES3/gl3.h>

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include <sys/system_properties.h>

#include "ImGui/FONTS/DEFAULT.h"
#include "ESP.h"
#include "touch.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "ArrayList", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "ArrayList", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "ArrayList", __VA_ARGS__))

int screenWidth = 0;
int screenHeight = 0;
bool g_Initialized = false;
ImGuiWindow* g_window = NULL;

struct ListItem {
    std::string name;
    float anim; // Animation state (0.0f to 1.0f)
    bool enabled; // Module enabled state
    std::string settingsText; // Optional settings display text
    bool visible; // Visibility in ArrayList
    int key; // Keybind (0 for none)
    ListItem(const std::string& n) : name(n), anim(0.0f), enabled(true), settingsText(""), visible(true), key(0) {}
};

std::vector<ListItem> g_listItems;
bool g_needsSort = false;
pthread_mutex_t g_listMutex = PTHREAD_MUTEX_INITIALIZER;

struct CompareByWidth {
    ImFont* font;
    float fontSize;
    CompareByWidth(ImFont* f, float size) : font(f), fontSize(size) {}
    bool operator()(const ListItem& a, const ListItem& b) const {
        std::string aFull = a.name + (a.settingsText.empty() ? "" : " " + a.settingsText);
        std::string bFull = b.name + (b.settingsText.empty() ? "" : " " + b.settingsText);
        float aSize = font ? font->CalcTextSizeA(fontSize, FLT_MAX, 0.0f, aFull.c_str()).x : aFull.length();
        float bSize = font ? font->CalcTextSizeA(fontSize, FLT_MAX, 0.0f, bFull.c_str()).x : bFull.length();
        return aSize > bSize;
    }
};

namespace Settings {
    enum class DisplayMode { None, Bar, Split, Outline };
    enum class Visibility { All, Bound };
    DisplayMode Display = DisplayMode::Bar;
    Visibility Visibility = Visibility::All;
    bool Glow = true;
    float GlowStrength = 10.0f; // Adjusted for multiple rectangles
    float FontSize = 20.0f;
    float Height = 20.0f;
    float BackgroundOpacity = 0.4f;
    bool ShadowBackground = true;
    float GradientSpeed = 1.0f;
}

float lerp(float a, float b, float t) {
    return a + t * (b - a);
}

float clamp(float value, float min, float max) {
    return std::max(min, std::min(max, value));
}

ImColor GetRainbowColor(float time, float offset) {
    float r = (sin(Settings::GradientSpeed * time + offset) * 0.5f + 0.5f);
    float g = (sin(Settings::GradientSpeed * time + offset + 2.0f) * 0.5f + 0.5f);
    float b = (sin(Settings::GradientSpeed * time + offset + 4.0f) * 0.5f + 0.5f);
    return ImColor(r, g, b, 1.0f);
}

void DrawShadowText(ImDrawList* drawList, ImFont* font, float fontSize, const std::string& text, ImVec2 pos, ImColor color, bool shadow = true) {
    ImVec2 shadowPos = ImVec2(pos.x + 1.0f, pos.y + 1.0f);
    ImVec2 textPos = pos;
    for (size_t i = 0; i < text.length(); ++i) {
        char c = text[i];
        if (shadow) {
            drawList->AddText(font, fontSize, shadowPos, ImColor(color.Value.x * 0.03f, color.Value.y * 0.03f, color.Value.z * 0.03f, 0.9f), &c, &c + 1);
        }
        drawList->AddText(font, fontSize, textPos, color, &c, &c + 1);
        float charWidth = font->CalcTextSizeA(fontSize, FLT_MAX, 0, &c, &c + 1).x;
        textPos.x += charWidth;
        shadowPos.x += charWidth;
    }
}

void DrawGlowRect(ImDrawList* drawList, ImVec2 min, ImVec2 max, ImColor color, float strength, float alpha, int layers = 3) {
    for (int i = 1; i <= layers; ++i) {
        float offset = i * strength / layers;
        float layerAlpha = alpha * (1.0f - static_cast<float>(i) / (layers + 1));
        ImVec2 layerMin = ImVec2(min.x - offset, min.y - offset);
        ImVec2 layerMax = ImVec2(max.x + offset, max.y + offset);
        drawList->AddRectFilled(layerMin, layerMax, ImColor(color.Value.x, color.Value.y, color.Value.z, layerAlpha));
    }
}

void DrawShadowBackground(ImDrawList* drawList, ImVec2 min, ImVec2 max, float opacity) {
    ImVec2 shadowMin = ImVec2(min.x + 2.0f, min.y + 2.0f);
    ImVec2 shadowMax = ImVec2(max.x + 2.0f, max.y + 2.0f);
    drawList->AddRectFilled(shadowMin, shadowMax, ImColor(0.0f, 0.0f, 0.0f, opacity));
}

void DrawFeatureList() {
    ImGuiIO& io = ImGui::GetIO();
    ImDrawList* drawList = ImGui::GetForegroundDrawList();
    ImFont* font = io.Fonts->Fonts.Size > 0 ? io.Fonts->Fonts[0] : nullptr;
    constexpr float padding = 4.0f;

    float maxWidth = 0.0f;
    std::vector<ListItem> items;
    pthread_mutex_lock(&g_listMutex);
    items = g_listItems;
    if (g_needsSort && font) {
        std::sort(items.begin(), items.end(), CompareByWidth(font, Settings::FontSize));
        g_listItems = items;
        g_needsSort = false;
    }
    for (const auto& item : items) {
        if (item.anim < 0.01f || !item.visible || (Settings::Visibility == Settings::Visibility::Bound && item.key == 0)) continue;
        std::string fullText = item.name + (item.settingsText.empty() ? "" : " " + item.settingsText);
        float width = font ? font->CalcTextSizeA(Settings::FontSize, FLT_MAX, 0.0f, fullText.c_str()).x : fullText.length() * 10.0f;
        maxWidth = std::max(maxWidth, width);
    }
    pthread_mutex_unlock(&g_listMutex);

    float windowWidth = maxWidth + padding * 10.0f + (Settings::Display == Settings::DisplayMode::Bar || Settings::Display == Settings::DisplayMode::Split ? 10.0f : 0.0f);
    ImGui::SetNextWindowPos(ImVec2(screenWidth - windowWidth - 10.0f, 10.0f), ImGuiCond_Always);
    ImGui::SetNextWindowSize(ImVec2(windowWidth, screenHeight), ImGuiCond_Always);

    ImGuiWindowFlags window_flags =
            ImGuiWindowFlags_NoTitleBar |
            ImGuiWindowFlags_NoBackground |
            ImGuiWindowFlags_NoResize |
            ImGuiWindowFlags_NoMove |
            ImGuiWindowFlags_NoScrollbar |
            ImGuiWindowFlags_NoSavedSettings;

    if (ImGui::Begin("##Features", nullptr, window_flags)) {
        g_window = ImGui::GetCurrentWindow();

        float y = 10.0f;
        float time = ImGui::GetTime();
        std::vector<std::tuple<ImVec2, ImVec2, ImColor>> backgroundRects;
        std::vector<std::tuple<ImVec2, ImVec2, ImColor>> outlineLines;

        pthread_mutex_lock(&g_listMutex);
        for (size_t i = 0; i < items.size(); ++i) {
            ListItem& item = items[i];
            if (!item.visible || (Settings::Visibility == Settings::Visibility::Bound && item.key == 0)) continue;

            item.anim = lerp(item.anim, item.enabled ? 1.0f : 0.0f, io.DeltaTime * 12.0f);
            item.anim = clamp(item.anim, 0.0f, 1.0f);
            g_listItems[i].anim = item.anim;
            if (item.anim < 0.01f) continue;

            std::string fullText = item.name + (item.settingsText.empty() ? "" : " " + item.settingsText);
            ImVec2 textSize = font ? font->CalcTextSizeA(Settings::FontSize, FLT_MAX, 0.0f, fullText.c_str()) : ImVec2(fullText.length() * 10.0f, Settings::FontSize);
            ImColor color = GetRainbowColor(time, i * 0.5f);

            float endX = screenWidth - textSize.x - padding * 5.0f - (Settings::Display == Settings::DisplayMode::Bar || Settings::Display == Settings::DisplayMode::Split ? 10.0f : 0.0f);
            float x = lerp(screenWidth + 14.0f, endX, item.anim);
            ImVec2 textPos = ImVec2(x, y);
            ImVec2 rectMin = ImVec2(x - padding, y);
            ImVec2 rectMax = ImVec2(x + textSize.x + padding, y + Settings::Height);

            if (Settings::Glow && Settings::Display != Settings::DisplayMode::None) {
                float glowAlpha = (Settings::Display == Settings::DisplayMode::Bar || Settings::Display == Settings::DisplayMode::Split) ? 0.7f : 0.83f;
                DrawGlowRect(drawList, rectMin, rectMax, color, Settings::GlowStrength, glowAlpha * item.anim);
            }

            if (Settings::ShadowBackground && Settings::Display != Settings::DisplayMode::Outline && Settings::Display != Settings::DisplayMode::None) {
                DrawShadowBackground(drawList, rectMin, rectMax, Settings::BackgroundOpacity * item.anim);
            }
            if (Settings::Display == Settings::DisplayMode::Outline) {
                drawList->AddRectFilled(rectMin, rectMax, ImColor(color.Value.x * Settings::BackgroundOpacity, color.Value.y * Settings::BackgroundOpacity, color.Value.z * Settings::BackgroundOpacity, 0.4f * item.anim));
            }

            ImVec2 nameSize = font ? font->CalcTextSizeA(Settings::FontSize, FLT_MAX, 0.0f, item.name.c_str()) : ImVec2(item.name.length() * 10.0f, Settings::FontSize);
            DrawShadowText(drawList, font, Settings::FontSize, item.name, textPos, color);
            if (!item.settingsText.empty()) {
                ImVec2 settingsPos = ImVec2(textPos.x + nameSize.x, textPos.y);
                DrawShadowText(drawList, font, Settings::FontSize, item.settingsText, settingsPos, ImColor(0.9f, 0.9f, 0.9f, 1.0f));
            }

            ImVec2 mousePos = io.MousePos;
            bool isHovered = mousePos.x >= rectMin.x && mousePos.x <= rectMax.x && mousePos.y >= rectMin.y && mousePos.y <= rectMax.y;
            if (isHovered) {
                drawList->AddRect(rectMin, rectMax, ImColor(1.0f, 1.0f, 1.0f, 0.5f * item.anim), 0.0f, 0, 2.0f);
                drawList->AddRectFilled(rectMin, rectMax, ImColor(1.0f, 1.0f, 1.0f, 0.1f * item.anim));
                if (io.MouseClicked[0]) {
                    item.enabled = !item.enabled;
                    g_listItems[i].enabled = item.enabled;
                    LOGI("Toggled module: %s (%s)", item.name.c_str(), item.enabled ? "enabled" : "disabled");
                }
            }

            if (Settings::Display == Settings::DisplayMode::Bar || Settings::Display == Settings::DisplayMode::Split) {
                ImVec2 lineStart = ImVec2(rectMax.x + 2.0f, rectMin.y + 4.0f);
                ImVec2 lineEnd = ImVec2(lineStart.x + (Settings::Display == Settings::DisplayMode::Bar ? 4.0f : 2.0f), rectMax.y - 4.0f);
                drawList->AddRectFilled(lineStart, lineEnd, color, 3.0f);
                if (Settings::Glow) {
                    DrawGlowRect(drawList, lineStart, lineEnd, color, Settings::GlowStrength, 0.7f * item.anim);
                }
            }

            if (Settings::Display == Settings::DisplayMode::Outline) {
                backgroundRects.emplace_back(rectMin, rectMax, color);
            }

            y += Settings::Height * item.anim;
        }
        pthread_mutex_unlock(&g_listMutex);

        if (Settings::Display == Settings::DisplayMode::Outline && !backgroundRects.empty()) {
            std::sort(backgroundRects.begin(), backgroundRects.end(),
                      [](const auto& a, const auto& b) { return std::get<0>(a).y < std::get<0>(b).y; });

            for (size_t i = 0; i < backgroundRects.size(); ++i) {
                auto [start, end, color] = backgroundRects[i];
                ImVec2 nextStart = i + 1 < backgroundRects.size() ? std::get<0>(backgroundRects[i + 1]) : start;

                if (i == 0 || nextStart.x >= start.x) {
                    outlineLines.emplace_back(ImVec2(start.x, start.y), ImVec2(start.x, end.y), color);
                }
                outlineLines.emplace_back(ImVec2(end.x, start.y), ImVec2(end.x, end.y), color);
                if (i == 0) {
                    outlineLines.emplace_back(ImVec2(start.x, start.y), ImVec2(end.x, start.y), color);
                }
                float endX = nextStart.x < start.x ? start.x : end.x;
                if (i == backgroundRects.size() - 1 || nextStart.x >= start.x) {
                    outlineLines.emplace_back(ImVec2(start.x, end.y), ImVec2(endX, end.y), color);
                }
            }

            std::sort(backgroundRects.begin(), backgroundRects.end(),
                      [](const auto& a, const auto& b) { return std::get<0>(a).x < std::get<0>(b).x; });
            if (!backgroundRects.empty()) {
                auto [start, end, color] = backgroundRects.front();
                outlineLines.emplace_back(ImVec2(start.x, start.y), ImVec2(start.x, end.y), color);
            }

            for (const auto& [start, end, color] : outlineLines) {
                drawList->AddLine(start, end, color, 2.0f);
            }
        }
    }
    ImGui::End();
}

extern "C" {
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_init(JNIEnv* env, jclass cls);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_resize(JNIEnv* env, jobject obj, jint width, jint height);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_step(JNIEnv* env, jobject obj);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_imgui_Shutdown(JNIEnv* env, jobject obj);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_MotionEventClick(JNIEnv* env, jobject obj, jboolean down, jfloat PosX, jfloat PosY);
JNIEXPORT jstring JNICALL Java_com_mycompany_application_GLES3JNIView_getWindowRect(JNIEnv *env, jobject thiz);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_addListItem(JNIEnv* env, jclass cls, jstring item, jstring settings, jboolean visible, jint key);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_removeListItem(JNIEnv* env, jclass cls, jstring item);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_removeListItemAt(JNIEnv* env, jclass cls, jint index);
JNIEXPORT jobjectArray JNICALL Java_com_mycompany_application_GLES3JNIView_getListItems(JNIEnv* env, jclass cls);
JNIEXPORT void JNICALL Java_com_mycompany_application_GLES3JNIView_clearList(JNIEnv* env, jclass cls);
JNIEXPORT void JNICALL
Java_com_mycompany_application_MainActivity_startTouchScreenHandle(JNIEnv* env, jclass cls) {
    TouchScreenHandle();
}
JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_setScreenDimensions(JNIEnv* env, jclass cls, jint width, jint height) {
    screenWidth = width;
    screenHeight = height;
}
};

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_init(JNIEnv* env, jclass cls) {
    if (g_Initialized) return;

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO();

    ImGui::StyleColorsDark();
    ImGuiStyle& style = ImGui::GetStyle();
    style.Colors[ImGuiCol_WindowBg] = ImVec4(0.0f, 0.0f, 0.0f, 0.0f);
    style.Colors[ImGuiCol_Text] = ImVec4(1.0f, 1.0f, 1.0f, 1.0f);
    style.WindowPadding = ImVec2(0, 0);
    style.ItemSpacing = ImVec2(0, 2);
    style.FramePadding = ImVec2(0, 0);

    ImGui_ImplAndroid_Init();
    ImGui_ImplOpenGL3_Init("#version 300 es");
    ImGui::GetStyle().ScaleAllSizes(1.0f);

    static const ImWchar icons_ranges[] = { 0xf000, 0xf3ff, 0x0900, 0x097F, 0, };
    ImFontConfig font_config;
    font_config.SizePixels = Settings::FontSize;
    ImFontConfig CustomFont;
    CustomFont.FontDataOwnedByAtlas = false;

    ImFont* font = io.Fonts->AddFontFromMemoryTTF(const_cast<std::uint8_t*>(Custom3), sizeof(Custom3), Settings::FontSize, &CustomFont);
    if (!font) {
        LOGE("Failed to load Custom3 font, using default");
        io.Fonts->AddFontDefault();
    }

    pthread_mutex_lock(&g_listMutex);
    g_listItems.clear();
    g_needsSort = false;
    pthread_mutex_unlock(&g_listMutex);

    g_Initialized = true;
    pthread_mutex_init(&g_listMutex, NULL);
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_resize(JNIEnv* env, jobject obj, jint width, jint height) {
    screenWidth = (int)width;
    screenHeight = (int)height;
    glViewport(0, 0, width, height);
    ImGuiIO& io = ImGui::GetIO();
    io.ConfigWindowsMoveFromTitleBarOnly = true;
    io.IniFilename = NULL;
    ImGui::GetIO().DisplaySize = ImVec2((float)width, (float)height);
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_step(JNIEnv* env, jobject obj) {
    ImGuiIO& io = ImGui::GetIO();

    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame(screenWidth, screenHeight);
    ImGui::NewFrame();

    DrawESPOverlay();
    DrawFeatureList();

    ImGui::Render();
    glClear(GL_COLOR_BUFFER_BIT);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_imgui_Shutdown(JNIEnv* env, jobject obj) {
    if (!g_Initialized) return;

    pthread_mutex_destroy(&g_listMutex);
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplAndroid_Shutdown();
    ImGui::DestroyContext();
    g_Initialized = false;
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_MotionEventClick(JNIEnv* env, jobject obj, jboolean down, jfloat PosX, jfloat PosY) {
    ImGuiIO& io = ImGui::GetIO();
    io.MouseDown[0] = down;
    io.MousePos = ImVec2(PosX, PosY);
}

JNIEXPORT jstring JNICALL
Java_com_mycompany_application_GLES3JNIView_getWindowRect(JNIEnv *env, jobject thiz) {
    char result[256] = "0|0|0|0";
    if (g_window) {
        sprintf(result, "%d|%d|%d|%d", (int)g_window->Pos.x, (int)g_window->Pos.y,
                (int)g_window->Size.x, (int)g_window->Size.y);
        LOGI("Window rect: %s", result);
    } else {
        LOGW("g_window is NULL");
    }
    return env->NewStringUTF(result);
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_addListItem(JNIEnv* env, jclass cls, jstring item, jstring settings, jboolean visible, jint key) {
    const char* itemStr = env->GetStringUTFChars(item, NULL);
    const char* settingsStr = settings ? env->GetStringUTFChars(settings, NULL) : "";
    std::string newItem(itemStr);
    std::string newSettings(settingsStr);

    pthread_mutex_lock(&g_listMutex);
    bool exists = false;
    for (const auto& existingItem : g_listItems) {
        if (existingItem.name == newItem) {
            exists = true;
            LOGI("Item already exists: %s", itemStr);
            break;
        }
    }
    if (!exists) {
        if (g_listItems.size() >= 10) {
            LOGW("Cannot add item: %s - Max 10 items reached", itemStr);
        } else {
            ListItem newListItem(newItem);
            newListItem.settingsText = newSettings;
            newListItem.visible = visible;
            newListItem.key = key;
            g_listItems.emplace_back(newListItem);
            g_needsSort = true;
            LOGI("Added item: %s (settings: %s, visible: %d, key: %d)", itemStr, newSettings.c_str(), visible, key);
        }
    }
    pthread_mutex_unlock(&g_listMutex);
    env->ReleaseStringUTFChars(item, itemStr);
    if (settings) env->ReleaseStringUTFChars(settings, settingsStr);
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_removeListItem(JNIEnv* env, jclass cls, jstring item) {
    const char* itemStr = env->GetStringUTFChars(item, NULL);
    std::string itemToRemove(itemStr);

    pthread_mutex_lock(&g_listMutex);
    auto it = std::find_if(g_listItems.begin(), g_listItems.end(),
                           [&itemToRemove](const ListItem& item) { return item.name == itemToRemove; });
    if (it != g_listItems.end()) {
        g_listItems.erase(it);
        g_needsSort = true;
        LOGI("Removed item: %s", itemStr);
    } else {
        LOGW("Item not found for removal: %s", itemStr);
    }
    pthread_mutex_unlock(&g_listMutex);
    env->ReleaseStringUTFChars(item, itemStr);
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_removeListItemAt(JNIEnv* env, jclass cls, jint index) {
    pthread_mutex_lock(&g_listMutex);
    if (index >= 0 && index < g_listItems.size()) {
        LOGI("Removed item at index %d: %s", index, g_listItems[index].name.c_str());
        g_listItems.erase(g_listItems.begin() + index);
        g_needsSort = true;
    } else {
        LOGW("Invalid index: %d (list size: %zu)", index, g_listItems.size());
    }
    pthread_mutex_unlock(&g_listMutex);
}

JNIEXPORT jobjectArray JNICALL
Java_com_mycompany_application_GLES3JNIView_getListItems(JNIEnv* env, jclass cls) {
    pthread_mutex_lock(&g_listMutex);
    jobjectArray result = env->NewObjectArray(g_listItems.size(),
                                              env->FindClass("java/lang/String"),
                                              env->NewStringUTF(""));
    for (size_t i = 0; i < g_listItems.size(); i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(g_listItems[i].name.c_str()));
    }
    pthread_mutex_unlock(&g_listMutex);
    return result;
}

JNIEXPORT void JNICALL
Java_com_mycompany_application_GLES3JNIView_clearList(JNIEnv* env, jclass cls) {
    pthread_mutex_lock(&g_listMutex);
    g_listItems.clear();
    g_needsSort = false;
    LOGI("Cleared list");
    pthread_mutex_unlock(&g_listMutex);
}

