//

//
//======================================================================================================================
#pragma once
//======================================================================================================================
#include <Globals.h>
#include <DrawUtils.h>
//======================================================================================================================
struct MenuVariables {
    float winWidth = 0.0f;
    float winHeight = 0.0f;
    float winPosWidth = 0.0f;
    float winPosHeight = 0.0f;
    ImVec4 textColor = ImVec4(0.990f, 0.0396f, 0.816f, 1.00f);
    ImVec4 color_red = ImVec4(1.0f, 0.0f, 0.0f, 1.00f);
    ImVec4 color_purple = ImVec4(1.0f, 0.0f, 1.0f, 1.00f);
    ImVec4 color_green = ImVec4(0.0f, 1.0f, 0.0f, 1.0f);
} MenuVars;
//======================================================================================================================
struct CheatVariables {
    float damageMultiplierF = 1.0f;
    int damageMultiplier = 1;
    int int_radio_A = 0;
    int int_button_math = 0;
    int drag_A = 0;
    int drag_B = 0;
    bool bool_checkbox_A = false;
    bool bool_draw_line = false;
    bool bool_draw_box = false;
} CheatVars;
//======================================================================================================================
void HelpMarker(const char* desc) {
    ImGui::TextDisabled(OBFUSCATE("(?)"));
    if (ImGui::BeginItemTooltip())
    {
        ImGui::PushTextWrapPos(ImGui::GetFontSize() * 30.0f);
        ImGui::TextUnformatted(desc);
        ImGui::PopTextWrapPos();
        ImGui::EndTooltip();
    }
}
//======================================================================================================================
void DrawLeftColumn () {

    ImGui::SetColumnWidth(-1,400);



    ImGui::Separator();





    ImGui::SeparatorText(OBFUSCATE("##LEFT_COLUMN_TOP_SEPARATOR_1"));


    ImGui::Text(OBFUSCATE("This is a normal text!"));
    ImGui::Spacing();
    ImGui::Spacing();
    ImGui::Text(OBFUSCATE("This is a normal text but it's a little ..."));


    ImGui::SeparatorText(OBFUSCATE("Separator"));


    ImGui::TextWrapped(OBFUSCATE("Longer text that will go to next line when reaching the end of the column!"));

    ImGui::SeparatorText(OBFUSCATE("##LEFT_COLUMN_TOP_SEPARATOR_2"));


    ImGui::TextColored(MenuVars.textColor, OBFUSCATE("I'm colored!"));

    ImGui::SeparatorText(OBFUSCATE("##LEFT_COLUMN_TOP_SEPARATOR_3"));
    ImGui::Spacing();
    ImGui::BulletText(OBFUSCATE("I'm a bullet text!"));
    ImGui::Spacing();
    ImGui::SeparatorText(OBFUSCATE("##LEFT_COLUMN_TOP_SEPARATOR_4"));


    ImGui::TextDisabled(OBFUSCATE("Disabled text!"));


    ImGui::SameLine();


    HelpMarker(OBFUSCATE("Short Description!"));

    ImGui::SeparatorText(OBFUSCATE("##LEFT_COLUMN_TOP_SEPARATOR_5"));
}
//======================================================================================================================
void DrawCheats () {


    ImGui::Spacing();
    ImGui::Spacing();
    ImGui::Text(OBFUSCATE("Cheats Tab!"));
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    const char* items_combo[] = { "AAAA", "BBBB", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIIIIII", "JJJJ", "KKKKKKK" };
    static int item_combo_current = 0;
    ImGui::Text("Combo: "); ImGui::SameLine(); ImGui::Text("%s", items_combo[item_combo_current]);


    ImGui::Combo("##_my_combo", &item_combo_current, items_combo, IM_ARRAYSIZE(items_combo));
    ImGui::SeparatorText("##SEP_MID_1");


    const char* items_list[] = { "Apple", "Banana", "Cherry", "Kiwi", "Mango", "Orange", "Pineapple", "Strawberry", "Watermelon" };
    static int item_list_current = 1;
    ImGui::Text("List: "); ImGui::SameLine(); ImGui::Text("%s", items_list[item_list_current]);
    ImGui::ListBox("##_my_list", &item_list_current, items_list, IM_ARRAYSIZE(items_list), 5);
    ImGui::SeparatorText("##SEP_MID_1");

    ImGui::Text("Drag INT, not capped");
    ImGui::DragInt("##drag int", &CheatVars.drag_A, 1);
    ImGui::SeparatorText("##SEP_MID_1");


    ImGui::EndTabItem();
}
//======================================================================================================================
void DrawMore () {
    ImGui::Spacing();
    ImGui::Spacing();
    ImGui::Text(OBFUSCATE("More Tab!"));
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));

    ImGui::TextColored(MenuVars.textColor, "Slider with INTEGERS - 1 to 100");
    ImGui::SliderInt("##_dmg", &CheatVars.damageMultiplier, 1, 100);
    ImGui::SameLine(); HelpMarker("Slider from 1 to 100");
    ImGui::SameLine(); ImGui::Text("§ %d", CheatVars.damageMultiplier);
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));

    ImGui::TextColored(MenuVars.textColor, "Slider with FLOATS - 1.0 to 10.0");
    ImGui::SliderFloat("##_dmg_f", &CheatVars.damageMultiplierF, 1.0f, 10.0f, "%.1f");
    ImGui::SameLine(); HelpMarker("Slider from 1.0 to 10.0");
    ImGui::SameLine(); ImGui::Text("§ %f", CheatVars.damageMultiplierF);
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));

    ImGui::Text("Drag INT, capped 0 - 100");
    ImGui::DragInt("##drag int 0..100_", &CheatVars.drag_B, 1, 0, 100, "%d%", ImGuiSliderFlags_AlwaysClamp);
    ImGui::SeparatorText("##SEP_MID_1");


    ImGui::EndTabItem();
}
//======================================================================================================================
void DrawEvenMore () {
    ImGui::Spacing();
    ImGui::Spacing();
    ImGui::Text(OBFUSCATE("Even More Tab!"));
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    ImGui::Checkbox("Check me nii-san!", &CheatVars.bool_checkbox_A);
    ImGui::SameLine(); HelpMarker("Onii-san!");
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    ImGui::RadioButton("Offline!", &CheatVars.int_radio_A, 0); ImGui::SameLine();
    ImGui::RadioButton("On-AIR!", &CheatVars.int_radio_A, 1); ImGui::SameLine();
    ImGui::SameLine(); HelpMarker("Kimochi!");
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    ImGui::Button("I do nothing!"); ImGui::SameLine();
    if (ImGui::Button("I do math")) {
        CheatVars.int_button_math++;
    }
    ImGui::SameLine(); HelpMarker("Tap to ++!");
    ImGui::SameLine(); ImGui::Text("Counter is: %d", CheatVars.int_button_math);
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    ImGui::Checkbox("Draw Line", &CheatVars.bool_draw_line);
    ImGui::SameLine();
    ImGui::Checkbox("Draw Box", &CheatVars.bool_draw_box);
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    ImGui::EndTabItem();
}
//======================================================================================================================
void DrawStyleEditor () {
    ImGui::Spacing();
    ImGui::Spacing();
    ImGui::Text(OBFUSCATE("Style Editor Tab!"));
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));

    ImGui::Text(OBFUSCATE("Coming soon . . ."));
    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_1"));


    ImGui::EndTabItem();
}
//======================================================================================================================
void DrawOtherStuff () {

    ImGui::SeparatorText(OBFUSCATE("##SEP_MID_0"));


    if (ImGui::BeginTabBar("_tab_bar", ImGuiTabBarFlags_FittingPolicyScroll)) {


        if (ImGui::BeginTabItem("Cheats", NULL, ImGuiTabItemFlags_Leading)) {
            DrawCheats();
        }


        if (ImGui::BeginTabItem("More")) {
            DrawMore();
        }


        if (ImGui::BeginTabItem("Even More")) {
            DrawEvenMore();
        }


        if (ImGui::BeginTabItem("Style Editor")) {
            DrawStyleEditor();
        }


        ImGui::EndTabBar();
    }
}
//======================================================================================================================
void DrawEsp () {




    if (CheatVars.bool_draw_line) {
        /*
        ** Start coords as ImVec2
        ** End coords as ImVec2
        ** Color as ImVec4
        */
        DrawLine({0,0}, {250,550}, MenuVars.color_red);

        /*
        ** Coords as ImVec2
        ** Color as ImVec4
        ** Text as const char *
        ** Font size as float
        */
        DrawText({252,500}, MenuVars.color_purple, "[X] Line from 0:0 to 250:550", 30.0f);
    }

    if (CheatVars.bool_draw_box) {
        DrawBox({250, 550}, {350, 650}, MenuVars.color_green);
        DrawText({350, 650}, MenuVars.color_red, "[X] Box from 250, 550 to 350, 650", 30.0f);
    }
}
//======================================================================================================================
void DrawColumnsDrivenMenu () {


    ImGui::Columns(2);


    DrawLeftColumn();


    ImGui::NextColumn();


    DrawOtherStuff();


    DrawEsp();
}
//======================================================================================================================