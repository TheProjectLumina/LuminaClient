//

//
//======================================================================================================================
#pragma once
//======================================================================================================================
#include <Globals.h>
//======================================================================================================================
/*
** Drawing are usually done in the background but you can them on top too
** -> GetForegroundDrawList
*/
//======================================================================================================================
void DrawLine(ImVec2 startCoords, ImVec2 endCoords, ImVec4 color) {
    auto backgroundDrawList = ImGui::GetBackgroundDrawList();
    if(backgroundDrawList) {
        backgroundDrawList->AddLine(startCoords, endCoords, ImColor(color.x, color.y, color.z, color.w));
    }
}
//======================================================================================================================
void DrawText(ImVec2 positionCoords, ImVec4 color, const char *text, float fontSize) {
    auto backgroundDrawList = ImGui::GetBackgroundDrawList();
    if(backgroundDrawList) {
        backgroundDrawList->AddText(NULL, fontSize, positionCoords, ImColor(color.x, color.y, color.z, color.w), text);
    }
}
//======================================================================================================================
void DrawBox(ImVec2 topLeft, ImVec2 bottomRight, ImVec4 color) {
    auto backgroundDrawList = ImGui::GetBackgroundDrawList();
    if(backgroundDrawList) {
        backgroundDrawList->AddRect(topLeft, bottomRight, ImGui::GetColorU32(color));
    }
}
//======================================================================================================================