


/*

Index of this file:

















*/





//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------










//-----------------------------------------------------------------------------




//-----------------------------------------------------------------------------











//-----------------------------------------------------------------------------




//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------














//-----------------------------------------------------------------------------






//-----------------------------------------------------------------------------







//-----------------------------------------------------------------------------





//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------








//-----------------------------------------------------------------------------






//-----------------------------------------------------------------------------






//-----------------------------------------------------------------------------








//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------







//-----------------------------------------------------------------------------








//





//


//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

#if defined(_MSC_VER) && !defined(_CRT_SECURE_NO_WARNINGS)
#define _CRT_SECURE_NO_WARNINGS
#endif

#ifndef IMGUI_DEFINE_MATH_OPERATORS
#define IMGUI_DEFINE_MATH_OPERATORS
#endif

#include "imgui.h"
#ifndef IMGUI_DISABLE
#include "imgui_internal.h"


#include <stdint.h>


#ifdef _MSC_VER
#pragma warning (disable: 4127)
#pragma warning (disable: 4996)
#if defined(_MSC_VER) && _MSC_VER >= 1922
#pragma warning (disable: 5054)
#endif
#pragma warning (disable: 26451)
#pragma warning (disable: 26812)
#endif


#if defined(__clang__)
#if __has_warning("-Wunknown-warning-option")
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#endif
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma clang diagnostic ignored "-Wold-style-cast"
#pragma clang diagnostic ignored "-Wfloat-equal"
#pragma clang diagnostic ignored "-Wformat-nonliteral"
#pragma clang diagnostic ignored "-Wsign-conversion"
#pragma clang diagnostic ignored "-Wzero-as-null-pointer-constant"
#pragma clang diagnostic ignored "-Wdouble-promotion"
#pragma clang diagnostic ignored "-Wenum-enum-conversion"
#pragma clang diagnostic ignored "-Wdeprecated-enum-enum-conversion"
#pragma clang diagnostic ignored "-Wimplicit-int-float-conversion"
#elif defined(__GNUC__)
#pragma GCC diagnostic ignored "-Wpragmas"
#pragma GCC diagnostic ignored "-Wformat-nonliteral"
#pragma GCC diagnostic ignored "-Wclass-memaccess"
#endif

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------












//-----------------------------------------------------------------------------


static const int TABLE_DRAW_CHANNEL_BG0 = 0;
static const int TABLE_DRAW_CHANNEL_BG2_FROZEN = 1;
static const int TABLE_DRAW_CHANNEL_NOCLIP = 2;
static const float TABLE_BORDER_SIZE                     = 1.0f;
static const float TABLE_RESIZE_SEPARATOR_HALF_THICKNESS = 4.0f;
static const float TABLE_RESIZE_SEPARATOR_FEEDBACK_TIMER = 0.06f;


inline ImGuiTableFlags TableFixFlags(ImGuiTableFlags flags, ImGuiWindow* outer_window)
{

    if ((flags & ImGuiTableFlags_SizingMask_) == 0)
        flags |= ((flags & ImGuiTableFlags_ScrollX) || (outer_window->Flags & ImGuiWindowFlags_AlwaysAutoResize)) ? ImGuiTableFlags_SizingFixedFit : ImGuiTableFlags_SizingStretchSame;


    if ((flags & ImGuiTableFlags_SizingMask_) == ImGuiTableFlags_SizingFixedSame)
        flags |= ImGuiTableFlags_NoKeepColumnsVisible;


    if (flags & ImGuiTableFlags_Resizable)
        flags |= ImGuiTableFlags_BordersInnerV;


    if (flags & (ImGuiTableFlags_ScrollX | ImGuiTableFlags_ScrollY))
        flags &= ~(ImGuiTableFlags_NoHostExtendX | ImGuiTableFlags_NoHostExtendY);


    if (flags & ImGuiTableFlags_NoBordersInBodyUntilResize)
        flags &= ~ImGuiTableFlags_NoBordersInBody;


    if ((flags & (ImGuiTableFlags_Resizable | ImGuiTableFlags_Hideable | ImGuiTableFlags_Reorderable | ImGuiTableFlags_Sortable)) == 0)
        flags |= ImGuiTableFlags_NoSavedSettings;


    if (outer_window->RootWindow->Flags & ImGuiWindowFlags_NoSavedSettings)
        flags |= ImGuiTableFlags_NoSavedSettings;

    return flags;
}

ImGuiTable* ImGui::TableFindByID(ImGuiID id)
{
    ImGuiContext& g = *GImGui;
    return g.Tables.GetByKey(id);
}


bool    ImGui::BeginTable(const char* str_id, int columns_count, ImGuiTableFlags flags, const ImVec2& outer_size, float inner_width)
{
    ImGuiID id = GetID(str_id);
    return BeginTableEx(str_id, id, columns_count, flags, outer_size, inner_width);
}

bool    ImGui::BeginTableEx(const char* name, ImGuiID id, int columns_count, ImGuiTableFlags flags, const ImVec2& outer_size, float inner_width)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* outer_window = GetCurrentWindow();
    if (outer_window->SkipItems)
        return false;


    IM_ASSERT(columns_count > 0 && columns_count < IMGUI_TABLE_MAX_COLUMNS);
    if (flags & ImGuiTableFlags_ScrollX)
        IM_ASSERT(inner_width >= 0.0f);


    const bool use_child_window = (flags & (ImGuiTableFlags_ScrollX | ImGuiTableFlags_ScrollY)) != 0;
    const ImVec2 avail_size = GetContentRegionAvail();
    ImVec2 actual_outer_size = CalcItemSize(outer_size, ImMax(avail_size.x, 1.0f), use_child_window ? ImMax(avail_size.y, 1.0f) : 0.0f);
    ImRect outer_rect(outer_window->DC.CursorPos, outer_window->DC.CursorPos + actual_outer_size);
    if (use_child_window && IsClippedEx(outer_rect, 0))
    {
        ItemSize(outer_rect);
        return false;
    }


    ImGuiTable* table = g.Tables.GetOrAddByKey(id);
    const ImGuiTableFlags table_last_flags = table->Flags;


    const int table_idx = g.Tables.GetIndex(table);
    if (++g.TablesTempDataStacked > g.TablesTempData.Size)
        g.TablesTempData.resize(g.TablesTempDataStacked, ImGuiTableTempData());
    ImGuiTableTempData* temp_data = table->TempData = &g.TablesTempData[g.TablesTempDataStacked - 1];
    temp_data->TableIndex = table_idx;
    table->DrawSplitter = &table->TempData->DrawSplitter;
    table->DrawSplitter->Clear();


    table->IsDefaultSizingPolicy = (flags & ImGuiTableFlags_SizingMask_) == 0;
    flags = TableFixFlags(flags, outer_window);


    const int instance_no = (table->LastFrameActive != g.FrameCount) ? 0 : table->InstanceCurrent + 1;
    table->ID = id;
    table->Flags = flags;
    table->LastFrameActive = g.FrameCount;
    table->OuterWindow = table->InnerWindow = outer_window;
    table->ColumnsCount = columns_count;
    table->IsLayoutLocked = false;
    table->InnerWidth = inner_width;
    temp_data->UserOuterSize = outer_size;


    ImGuiID instance_id;
    table->InstanceCurrent = (ImS16)instance_no;
    if (instance_no > 0)
    {
        IM_ASSERT(table->ColumnsCount == columns_count && "BeginTable(): Cannot change columns count mid-frame while preserving same ID");
        if (table->InstanceDataExtra.Size < instance_no)
            table->InstanceDataExtra.push_back(ImGuiTableInstanceData());
        instance_id = GetIDWithSeed(instance_no, GetIDWithSeed("##Instances", NULL, id));
    }
    else
    {
        instance_id = id;
    }
    ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, table->InstanceCurrent);
    table_instance->TableInstanceID = instance_id;


    if (use_child_window)
    {


        ImVec2 override_content_size(FLT_MAX, FLT_MAX);
        if ((flags & ImGuiTableFlags_ScrollX) && !(flags & ImGuiTableFlags_ScrollY))
            override_content_size.y = FLT_MIN;





        if ((flags & ImGuiTableFlags_ScrollX) && inner_width > 0.0f)
            override_content_size.x = inner_width;

        if (override_content_size.x != FLT_MAX || override_content_size.y != FLT_MAX)
            SetNextWindowContentSize(ImVec2(override_content_size.x != FLT_MAX ? override_content_size.x : 0.0f, override_content_size.y != FLT_MAX ? override_content_size.y : 0.0f));


        if ((table_last_flags & (ImGuiTableFlags_ScrollX | ImGuiTableFlags_ScrollY)) == 0)
            SetNextWindowScroll(ImVec2(0.0f, 0.0f));


        ImGuiWindowFlags child_flags = (flags & ImGuiTableFlags_ScrollX) ? ImGuiWindowFlags_HorizontalScrollbar : ImGuiWindowFlags_None;
        BeginChildEx(name, instance_id, outer_rect.GetSize(), false, child_flags);
        table->InnerWindow = g.CurrentWindow;
        table->WorkRect = table->InnerWindow->WorkRect;
        table->OuterRect = table->InnerWindow->Rect();
        table->InnerRect = table->InnerWindow->InnerRect;
        IM_ASSERT(table->InnerWindow->WindowPadding.x == 0.0f && table->InnerWindow->WindowPadding.y == 0.0f && table->InnerWindow->WindowBorderSize == 0.0f);


        if (instance_no == 0)
        {
            table->HasScrollbarYPrev = table->HasScrollbarYCurr;
            table->HasScrollbarYCurr = false;
        }
        table->HasScrollbarYCurr |= table->InnerWindow->ScrollbarY;
    }
    else
    {


        table->WorkRect = table->OuterRect = table->InnerRect = outer_rect;
    }


    PushOverrideID(id);
    if (instance_no > 0)
        PushOverrideID(instance_id);


    ImGuiWindow* inner_window = table->InnerWindow;
    table->HostIndentX = inner_window->DC.Indent.x;
    table->HostClipRect = inner_window->ClipRect;
    table->HostSkipItems = inner_window->SkipItems;
    temp_data->HostBackupWorkRect = inner_window->WorkRect;
    temp_data->HostBackupParentWorkRect = inner_window->ParentWorkRect;
    temp_data->HostBackupColumnsOffset = outer_window->DC.ColumnsOffset;
    temp_data->HostBackupPrevLineSize = inner_window->DC.PrevLineSize;
    temp_data->HostBackupCurrLineSize = inner_window->DC.CurrLineSize;
    temp_data->HostBackupCursorMaxPos = inner_window->DC.CursorMaxPos;
    temp_data->HostBackupItemWidth = outer_window->DC.ItemWidth;
    temp_data->HostBackupItemWidthStackSize = outer_window->DC.ItemWidthStack.Size;
    inner_window->DC.PrevLineSize = inner_window->DC.CurrLineSize = ImVec2(0.0f, 0.0f);






    const bool pad_outer_x = (flags & ImGuiTableFlags_NoPadOuterX) ? false : (flags & ImGuiTableFlags_PadOuterX) ? true : (flags & ImGuiTableFlags_BordersOuterV) != 0;
    const bool pad_inner_x = (flags & ImGuiTableFlags_NoPadInnerX) ? false : true;
    const float inner_spacing_for_border = (flags & ImGuiTableFlags_BordersInnerV) ? TABLE_BORDER_SIZE : 0.0f;
    const float inner_spacing_explicit = (pad_inner_x && (flags & ImGuiTableFlags_BordersInnerV) == 0) ? g.Style.CellPadding.x : 0.0f;
    const float inner_padding_explicit = (pad_inner_x && (flags & ImGuiTableFlags_BordersInnerV) != 0) ? g.Style.CellPadding.x : 0.0f;
    table->CellSpacingX1 = inner_spacing_explicit + inner_spacing_for_border;
    table->CellSpacingX2 = inner_spacing_explicit;
    table->CellPaddingX = inner_padding_explicit;
    table->CellPaddingY = g.Style.CellPadding.y;

    const float outer_padding_for_border = (flags & ImGuiTableFlags_BordersOuterV) ? TABLE_BORDER_SIZE : 0.0f;
    const float outer_padding_explicit = pad_outer_x ? g.Style.CellPadding.x : 0.0f;
    table->OuterPaddingX = (outer_padding_for_border + outer_padding_explicit) - table->CellPaddingX;

    table->CurrentColumn = -1;
    table->CurrentRow = -1;
    table->RowBgColorCounter = 0;
    table->LastRowFlags = ImGuiTableRowFlags_None;
    table->InnerClipRect = (inner_window == outer_window) ? table->WorkRect : inner_window->ClipRect;
    table->InnerClipRect.ClipWith(table->WorkRect);
    table->InnerClipRect.ClipWithFull(table->HostClipRect);
    table->InnerClipRect.Max.y = (flags & ImGuiTableFlags_NoHostExtendY) ? ImMin(table->InnerClipRect.Max.y, inner_window->WorkRect.Max.y) : inner_window->ClipRect.Max.y;

    table->RowPosY1 = table->RowPosY2 = table->WorkRect.Min.y;
    table->RowTextBaseline = 0.0f;
    table->FreezeRowsRequest = table->FreezeRowsCount = 0;
    table->FreezeColumnsRequest = table->FreezeColumnsCount = 0;
    table->IsUnfrozenRows = true;
    table->DeclColumnsCount = 0;


    table->BorderColorStrong = GetColorU32(ImGuiCol_TableBorderStrong);
    table->BorderColorLight = GetColorU32(ImGuiCol_TableBorderLight);


    g.CurrentTable = table;
    outer_window->DC.NavIsScrollPushableX = false;
    outer_window->DC.CurrentTableIdx = table_idx;
    if (inner_window != outer_window)
        inner_window->DC.CurrentTableIdx = table_idx;

    if ((table_last_flags & ImGuiTableFlags_Reorderable) && (flags & ImGuiTableFlags_Reorderable) == 0)
        table->IsResetDisplayOrderRequest = true;


    if (table_idx >= g.TablesLastTimeActive.Size)
        g.TablesLastTimeActive.resize(table_idx + 1, -1.0f);
    g.TablesLastTimeActive[table_idx] = (float)g.Time;
    temp_data->LastTimeActive = (float)g.Time;
    table->MemoryCompacted = false;


    ImGuiTableColumn* old_columns_to_preserve = NULL;
    void* old_columns_raw_data = NULL;
    const int old_columns_count = table->Columns.size();
    if (old_columns_count != 0 && old_columns_count != columns_count)
    {

        old_columns_to_preserve = table->Columns.Data;
        old_columns_raw_data = table->RawData;
        table->RawData = NULL;
    }
    if (table->RawData == NULL)
    {
        TableBeginInitMemory(table, columns_count);
        table->IsInitializing = table->IsSettingsRequestLoad = true;
    }
    if (table->IsResetAllRequest)
        TableResetSettings(table);
    if (table->IsInitializing)
    {

        table->SettingsOffset = -1;
        table->IsSortSpecsDirty = true;
        table->InstanceInteracted = -1;
        table->ContextPopupColumn = -1;
        table->ReorderColumn = table->ResizedColumn = table->LastResizedColumn = -1;
        table->AutoFitSingleColumn = -1;
        table->HoveredColumnBody = table->HoveredColumnBorder = -1;
        for (int n = 0; n < columns_count; n++)
        {
            ImGuiTableColumn* column = &table->Columns[n];
            if (old_columns_to_preserve && n < old_columns_count)
            {

                *column = old_columns_to_preserve[n];
            }
            else
            {
                float width_auto = column->WidthAuto;
                *column = ImGuiTableColumn();
                column->WidthAuto = width_auto;
                column->IsPreserveWidthAuto = true;
                column->IsEnabled = column->IsUserEnabled = column->IsUserEnabledNextFrame = true;
            }
            column->DisplayOrder = table->DisplayOrderToIndex[n] = (ImGuiTableColumnIdx)n;
        }
    }
    if (old_columns_raw_data)
        IM_FREE(old_columns_raw_data);


    if (table->IsSettingsRequestLoad)
        TableLoadSettings(table);






    const float new_ref_scale_unit = g.FontSize;
    if (table->RefScale != 0.0f && table->RefScale != new_ref_scale_unit)
    {
        const float scale_factor = new_ref_scale_unit / table->RefScale;
        //IMGUI_DEBUG_PRINT("[table] %08X RefScaleUnit %.3f -> %.3f, scaling width by %.3f\n", table->ID, table->RefScaleUnit, new_ref_scale_unit, scale_factor);
        for (int n = 0; n < columns_count; n++)
            table->Columns[n].WidthRequest = table->Columns[n].WidthRequest * scale_factor;
    }
    table->RefScale = new_ref_scale_unit;




    inner_window->SkipItems = true;



    if (table->ColumnsNames.Buf.Size > 0)
        table->ColumnsNames.Buf.resize(0);


    TableBeginApplyRequests(table);

    return true;
}










void ImGui::TableBeginInitMemory(ImGuiTable* table, int columns_count)
{

    const int columns_bit_array_size = (int)ImBitArrayGetStorageSizeInBytes(columns_count);
    ImSpanAllocator<6> span_allocator;
    span_allocator.Reserve(0, columns_count * sizeof(ImGuiTableColumn));
    span_allocator.Reserve(1, columns_count * sizeof(ImGuiTableColumnIdx));
    span_allocator.Reserve(2, columns_count * sizeof(ImGuiTableCellData), 4);
    for (int n = 3; n < 6; n++)
        span_allocator.Reserve(n, columns_bit_array_size);
    table->RawData = IM_ALLOC(span_allocator.GetArenaSizeInBytes());
    memset(table->RawData, 0, span_allocator.GetArenaSizeInBytes());
    span_allocator.SetArenaBasePtr(table->RawData);
    span_allocator.GetSpan(0, &table->Columns);
    span_allocator.GetSpan(1, &table->DisplayOrderToIndex);
    span_allocator.GetSpan(2, &table->RowCellData);
    table->EnabledMaskByDisplayOrder = (ImU32*)span_allocator.GetSpanPtrBegin(3);
    table->EnabledMaskByIndex = (ImU32*)span_allocator.GetSpanPtrBegin(4);
    table->VisibleMaskByIndex = (ImU32*)span_allocator.GetSpanPtrBegin(5);
}


void ImGui::TableBeginApplyRequests(ImGuiTable* table)
{



    if (table->InstanceCurrent == 0)
    {
        if (table->ResizedColumn != -1 && table->ResizedColumnNextWidth != FLT_MAX)
            TableSetColumnWidth(table->ResizedColumn, table->ResizedColumnNextWidth);
        table->LastResizedColumn = table->ResizedColumn;
        table->ResizedColumnNextWidth = FLT_MAX;
        table->ResizedColumn = -1;



        if (table->AutoFitSingleColumn != -1)
        {
            TableSetColumnWidth(table->AutoFitSingleColumn, table->Columns[table->AutoFitSingleColumn].WidthAuto);
            table->AutoFitSingleColumn = -1;
        }
    }



    if (table->InstanceCurrent == 0)
    {
        if (table->HeldHeaderColumn == -1 && table->ReorderColumn != -1)
            table->ReorderColumn = -1;
        table->HeldHeaderColumn = -1;
        if (table->ReorderColumn != -1 && table->ReorderColumnDir != 0)
        {




            const int reorder_dir = table->ReorderColumnDir;
            IM_ASSERT(reorder_dir == -1 || reorder_dir == +1);
            IM_ASSERT(table->Flags & ImGuiTableFlags_Reorderable);
            ImGuiTableColumn* src_column = &table->Columns[table->ReorderColumn];
            ImGuiTableColumn* dst_column = &table->Columns[(reorder_dir == -1) ? src_column->PrevEnabledColumn : src_column->NextEnabledColumn];
            IM_UNUSED(dst_column);
            const int src_order = src_column->DisplayOrder;
            const int dst_order = dst_column->DisplayOrder;
            src_column->DisplayOrder = (ImGuiTableColumnIdx)dst_order;
            for (int order_n = src_order + reorder_dir; order_n != dst_order + reorder_dir; order_n += reorder_dir)
                table->Columns[table->DisplayOrderToIndex[order_n]].DisplayOrder -= (ImGuiTableColumnIdx)reorder_dir;
            IM_ASSERT(dst_column->DisplayOrder == dst_order - reorder_dir);


            for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
                table->DisplayOrderToIndex[table->Columns[column_n].DisplayOrder] = (ImGuiTableColumnIdx)column_n;
            table->ReorderColumnDir = 0;
            table->IsSettingsDirty = true;
        }
    }


    if (table->IsResetDisplayOrderRequest)
    {
        for (int n = 0; n < table->ColumnsCount; n++)
            table->DisplayOrderToIndex[n] = table->Columns[n].DisplayOrder = (ImGuiTableColumnIdx)n;
        table->IsResetDisplayOrderRequest = false;
        table->IsSettingsDirty = true;
    }
}


static void TableSetupColumnFlags(ImGuiTable* table, ImGuiTableColumn* column, ImGuiTableColumnFlags flags_in)
{
    ImGuiTableColumnFlags flags = flags_in;


    if ((flags & ImGuiTableColumnFlags_WidthMask_) == 0)
    {
        const ImGuiTableFlags table_sizing_policy = (table->Flags & ImGuiTableFlags_SizingMask_);
        if (table_sizing_policy == ImGuiTableFlags_SizingFixedFit || table_sizing_policy == ImGuiTableFlags_SizingFixedSame)
            flags |= ImGuiTableColumnFlags_WidthFixed;
        else
            flags |= ImGuiTableColumnFlags_WidthStretch;
    }
    else
    {
        IM_ASSERT(ImIsPowerOfTwo(flags & ImGuiTableColumnFlags_WidthMask_));
    }


    if ((table->Flags & ImGuiTableFlags_Resizable) == 0)
        flags |= ImGuiTableColumnFlags_NoResize;


    if ((flags & ImGuiTableColumnFlags_NoSortAscending) && (flags & ImGuiTableColumnFlags_NoSortDescending))
        flags |= ImGuiTableColumnFlags_NoSort;


    if ((flags & ImGuiTableColumnFlags_IndentMask_) == 0)
        flags |= (table->Columns.index_from_ptr(column) == 0) ? ImGuiTableColumnFlags_IndentEnable : ImGuiTableColumnFlags_IndentDisable;


    //if ((flags & ImGuiTableColumnFlags_AlignMask_) == 0)

    //IM_ASSERT(ImIsPowerOfTwo(flags & ImGuiTableColumnFlags_AlignMask_));


    column->Flags = flags | (column->Flags & ImGuiTableColumnFlags_StatusMask_);


    column->SortDirectionsAvailCount = column->SortDirectionsAvailMask = column->SortDirectionsAvailList = 0;
    if (table->Flags & ImGuiTableFlags_Sortable)
    {
        int count = 0, mask = 0, list = 0;
        if ((flags & ImGuiTableColumnFlags_PreferSortAscending)  != 0 && (flags & ImGuiTableColumnFlags_NoSortAscending)  == 0) { mask |= 1 << ImGuiSortDirection_Ascending;  list |= ImGuiSortDirection_Ascending  << (count << 1); count++; }
        if ((flags & ImGuiTableColumnFlags_PreferSortDescending) != 0 && (flags & ImGuiTableColumnFlags_NoSortDescending) == 0) { mask |= 1 << ImGuiSortDirection_Descending; list |= ImGuiSortDirection_Descending << (count << 1); count++; }
        if ((flags & ImGuiTableColumnFlags_PreferSortAscending)  == 0 && (flags & ImGuiTableColumnFlags_NoSortAscending)  == 0) { mask |= 1 << ImGuiSortDirection_Ascending;  list |= ImGuiSortDirection_Ascending  << (count << 1); count++; }
        if ((flags & ImGuiTableColumnFlags_PreferSortDescending) == 0 && (flags & ImGuiTableColumnFlags_NoSortDescending) == 0) { mask |= 1 << ImGuiSortDirection_Descending; list |= ImGuiSortDirection_Descending << (count << 1); count++; }
        if ((table->Flags & ImGuiTableFlags_SortTristate) || count == 0) { mask |= 1 << ImGuiSortDirection_None; count++; }
        column->SortDirectionsAvailList = (ImU8)list;
        column->SortDirectionsAvailMask = (ImU8)mask;
        column->SortDirectionsAvailCount = (ImU8)count;
        ImGui::TableFixColumnSortDirection(table, column);
    }
}





void ImGui::TableUpdateLayout(ImGuiTable* table)
{
    ImGuiContext& g = *GImGui;
    IM_ASSERT(table->IsLayoutLocked == false);

    const ImGuiTableFlags table_sizing_policy = (table->Flags & ImGuiTableFlags_SizingMask_);
    table->IsDefaultDisplayOrder = true;
    table->ColumnsEnabledCount = 0;
    ImBitArrayClearAllBits(table->EnabledMaskByIndex, table->ColumnsCount);
    ImBitArrayClearAllBits(table->EnabledMaskByDisplayOrder, table->ColumnsCount);
    table->LeftMostEnabledColumn = -1;
    table->MinColumnWidth = ImMax(1.0f, g.Style.FramePadding.x * 1.0f);



    int count_fixed = 0;
    int count_stretch = 0;
    int prev_visible_column_idx = -1;
    bool has_auto_fit_request = false;
    bool has_resizable = false;
    float stretch_sum_width_auto = 0.0f;
    float fixed_max_width_auto = 0.0f;
    for (int order_n = 0; order_n < table->ColumnsCount; order_n++)
    {
        const int column_n = table->DisplayOrderToIndex[order_n];
        if (column_n != order_n)
            table->IsDefaultDisplayOrder = false;
        ImGuiTableColumn* column = &table->Columns[column_n];




        if (table->DeclColumnsCount <= column_n)
        {
            TableSetupColumnFlags(table, column, ImGuiTableColumnFlags_None);
            column->NameOffset = -1;
            column->UserID = 0;
            column->InitStretchWeightOrWidth = -1.0f;
        }


        if (!(table->Flags & ImGuiTableFlags_Hideable) || (column->Flags & ImGuiTableColumnFlags_NoHide))
            column->IsUserEnabledNextFrame = true;
        if (column->IsUserEnabled != column->IsUserEnabledNextFrame)
        {
            column->IsUserEnabled = column->IsUserEnabledNextFrame;
            table->IsSettingsDirty = true;
        }
        column->IsEnabled = column->IsUserEnabled && (column->Flags & ImGuiTableColumnFlags_Disabled) == 0;

        if (column->SortOrder != -1 && !column->IsEnabled)
            table->IsSortSpecsDirty = true;
        if (column->SortOrder > 0 && !(table->Flags & ImGuiTableFlags_SortMulti))
            table->IsSortSpecsDirty = true;


        const bool start_auto_fit = (column->Flags & ImGuiTableColumnFlags_WidthFixed) ? (column->WidthRequest < 0.0f) : (column->StretchWeight < 0.0f);
        if (start_auto_fit)
            column->AutoFitQueue = column->CannotSkipItemsQueue = (1 << 3) - 1;

        if (!column->IsEnabled)
        {
            column->IndexWithinEnabledSet = -1;
            continue;
        }


        column->PrevEnabledColumn = (ImGuiTableColumnIdx)prev_visible_column_idx;
        column->NextEnabledColumn = -1;
        if (prev_visible_column_idx != -1)
            table->Columns[prev_visible_column_idx].NextEnabledColumn = (ImGuiTableColumnIdx)column_n;
        else
            table->LeftMostEnabledColumn = (ImGuiTableColumnIdx)column_n;
        column->IndexWithinEnabledSet = table->ColumnsEnabledCount++;
        ImBitArraySetBit(table->EnabledMaskByIndex, column_n);
        ImBitArraySetBit(table->EnabledMaskByDisplayOrder, column->DisplayOrder);
        prev_visible_column_idx = column_n;
        IM_ASSERT(column->IndexWithinEnabledSet <= column->DisplayOrder);



        if (!column->IsPreserveWidthAuto)
            column->WidthAuto = TableGetColumnWidthAuto(table, column);


        const bool column_is_resizable = (column->Flags & ImGuiTableColumnFlags_NoResize) == 0;
        if (column_is_resizable)
            has_resizable = true;
        if ((column->Flags & ImGuiTableColumnFlags_WidthFixed) && column->InitStretchWeightOrWidth > 0.0f && !column_is_resizable)
            column->WidthAuto = column->InitStretchWeightOrWidth;

        if (column->AutoFitQueue != 0x00)
            has_auto_fit_request = true;
        if (column->Flags & ImGuiTableColumnFlags_WidthStretch)
        {
            stretch_sum_width_auto += column->WidthAuto;
            count_stretch++;
        }
        else
        {
            fixed_max_width_auto = ImMax(fixed_max_width_auto, column->WidthAuto);
            count_fixed++;
        }
    }
    if ((table->Flags & ImGuiTableFlags_Sortable) && table->SortSpecsCount == 0 && !(table->Flags & ImGuiTableFlags_SortTristate))
        table->IsSortSpecsDirty = true;
    table->RightMostEnabledColumn = (ImGuiTableColumnIdx)prev_visible_column_idx;
    IM_ASSERT(table->LeftMostEnabledColumn >= 0 && table->RightMostEnabledColumn >= 0);




    if (has_auto_fit_request && table->OuterWindow != table->InnerWindow)
        table->InnerWindow->SkipItems = false;
    if (has_auto_fit_request)
        table->IsSettingsDirty = true;


    float sum_width_requests = 0.0f;
    float stretch_sum_weights = 0.0f;
    table->LeftMostStretchedColumn = table->RightMostStretchedColumn = -1;
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        if (!IM_BITARRAY_TESTBIT(table->EnabledMaskByIndex, column_n))
            continue;
        ImGuiTableColumn* column = &table->Columns[column_n];

        const bool column_is_resizable = (column->Flags & ImGuiTableColumnFlags_NoResize) == 0;
        if (column->Flags & ImGuiTableColumnFlags_WidthFixed)
        {

            float width_auto = column->WidthAuto;
            if (table_sizing_policy == ImGuiTableFlags_SizingFixedSame && (column->AutoFitQueue != 0x00 || !column_is_resizable))
                width_auto = fixed_max_width_auto;



            if (column->AutoFitQueue != 0x00)
                column->WidthRequest = width_auto;
            else if ((column->Flags & ImGuiTableColumnFlags_WidthFixed) && !column_is_resizable && column->IsRequestOutput)
                column->WidthRequest = width_auto;







            if (column->AutoFitQueue > 0x01 && table->IsInitializing && !column->IsPreserveWidthAuto)
                column->WidthRequest = ImMax(column->WidthRequest, table->MinColumnWidth * 4.0f);
            sum_width_requests += column->WidthRequest;
        }
        else
        {

            if (column->AutoFitQueue != 0x00 || column->StretchWeight < 0.0f || !column_is_resizable)
            {
                if (column->InitStretchWeightOrWidth > 0.0f)
                    column->StretchWeight = column->InitStretchWeightOrWidth;
                else if (table_sizing_policy == ImGuiTableFlags_SizingStretchProp)
                    column->StretchWeight = (column->WidthAuto / stretch_sum_width_auto) * count_stretch;
                else
                    column->StretchWeight = 1.0f;
            }

            stretch_sum_weights += column->StretchWeight;
            if (table->LeftMostStretchedColumn == -1 || table->Columns[table->LeftMostStretchedColumn].DisplayOrder > column->DisplayOrder)
                table->LeftMostStretchedColumn = (ImGuiTableColumnIdx)column_n;
            if (table->RightMostStretchedColumn == -1 || table->Columns[table->RightMostStretchedColumn].DisplayOrder < column->DisplayOrder)
                table->RightMostStretchedColumn = (ImGuiTableColumnIdx)column_n;
        }
        column->IsPreserveWidthAuto = false;
        sum_width_requests += table->CellPaddingX * 2.0f;
    }
    table->ColumnsEnabledFixedCount = (ImGuiTableColumnIdx)count_fixed;
    table->ColumnsStretchSumWeights = stretch_sum_weights;


    const ImRect work_rect = table->WorkRect;
    const float width_spacings = (table->OuterPaddingX * 2.0f) + (table->CellSpacingX1 + table->CellSpacingX2) * (table->ColumnsEnabledCount - 1);
    const float width_removed = (table->HasScrollbarYPrev && !table->InnerWindow->ScrollbarY) ? g.Style.ScrollbarSize : 0.0f;
    const float width_avail = ImMax(1.0f, (((table->Flags & ImGuiTableFlags_ScrollX) && table->InnerWidth == 0.0f) ? table->InnerClipRect.GetWidth() : work_rect.GetWidth()) - width_removed);
    const float width_avail_for_stretched_columns = width_avail - width_spacings - sum_width_requests;
    float width_remaining_for_stretched_columns = width_avail_for_stretched_columns;
    table->ColumnsGivenWidth = width_spacings + (table->CellPaddingX * 2.0f) * table->ColumnsEnabledCount;
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        if (!IM_BITARRAY_TESTBIT(table->EnabledMaskByIndex, column_n))
            continue;
        ImGuiTableColumn* column = &table->Columns[column_n];


        if (column->Flags & ImGuiTableColumnFlags_WidthStretch)
        {
            float weight_ratio = column->StretchWeight / stretch_sum_weights;
            column->WidthRequest = IM_FLOOR(ImMax(width_avail_for_stretched_columns * weight_ratio, table->MinColumnWidth) + 0.01f);
            width_remaining_for_stretched_columns -= column->WidthRequest;
        }



        if (column->NextEnabledColumn == -1 && table->LeftMostStretchedColumn != -1)
            column->Flags |= ImGuiTableColumnFlags_NoDirectResize_;


        column->WidthGiven = ImFloor(ImMax(column->WidthRequest, table->MinColumnWidth));
        table->ColumnsGivenWidth += column->WidthGiven;
    }



    if (width_remaining_for_stretched_columns >= 1.0f && !(table->Flags & ImGuiTableFlags_PreciseWidths))
        for (int order_n = table->ColumnsCount - 1; stretch_sum_weights > 0.0f && width_remaining_for_stretched_columns >= 1.0f && order_n >= 0; order_n--)
        {
            if (!IM_BITARRAY_TESTBIT(table->EnabledMaskByDisplayOrder, order_n))
                continue;
            ImGuiTableColumn* column = &table->Columns[table->DisplayOrderToIndex[order_n]];
            if (!(column->Flags & ImGuiTableColumnFlags_WidthStretch))
                continue;
            column->WidthRequest += 1.0f;
            column->WidthGiven += 1.0f;
            width_remaining_for_stretched_columns -= 1.0f;
        }






    ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, table->InstanceCurrent);
    table_instance->HoveredRowLast = table_instance->HoveredRowNext;
    table_instance->HoveredRowNext = -1;
    table->HoveredColumnBody = -1;
    table->HoveredColumnBorder = -1;
    const ImRect mouse_hit_rect(table->OuterRect.Min.x, table->OuterRect.Min.y, table->OuterRect.Max.x, ImMax(table->OuterRect.Max.y, table->OuterRect.Min.y + table_instance->LastOuterHeight));
    const ImGuiID backup_active_id = g.ActiveId;
    g.ActiveId = 0;
    const bool is_hovering_table = ItemHoverable(mouse_hit_rect, 0, ImGuiItemFlags_None);
    g.ActiveId = backup_active_id;



    int visible_n = 0;
    bool offset_x_frozen = (table->FreezeColumnsCount > 0);
    float offset_x = ((table->FreezeColumnsCount > 0) ? table->OuterRect.Min.x : work_rect.Min.x) + table->OuterPaddingX - table->CellSpacingX1;
    ImRect host_clip_rect = table->InnerClipRect;
    //host_clip_rect.Max.x += table->CellPaddingX + table->CellSpacingX2;
    ImBitArrayClearAllBits(table->VisibleMaskByIndex, table->ColumnsCount);
    for (int order_n = 0; order_n < table->ColumnsCount; order_n++)
    {
        const int column_n = table->DisplayOrderToIndex[order_n];
        ImGuiTableColumn* column = &table->Columns[column_n];

        column->NavLayerCurrent = (ImS8)(table->FreezeRowsCount > 0 ? ImGuiNavLayer_Menu : ImGuiNavLayer_Main);

        if (offset_x_frozen && table->FreezeColumnsCount == visible_n)
        {
            offset_x += work_rect.Min.x - table->OuterRect.Min.x;
            offset_x_frozen = false;
        }


        column->Flags &= ~ImGuiTableColumnFlags_StatusMask_;

        if (!IM_BITARRAY_TESTBIT(table->EnabledMaskByDisplayOrder, order_n))
        {


            column->MinX = column->MaxX = column->WorkMinX = column->ClipRect.Min.x = column->ClipRect.Max.x = offset_x;
            column->WidthGiven = 0.0f;
            column->ClipRect.Min.y = work_rect.Min.y;
            column->ClipRect.Max.y = FLT_MAX;
            column->ClipRect.ClipWithFull(host_clip_rect);
            column->IsVisibleX = column->IsVisibleY = column->IsRequestOutput = false;
            column->IsSkipItems = true;
            column->ItemWidth = 1.0f;
            continue;
        }


        if (is_hovering_table && g.IO.MousePos.x >= column->ClipRect.Min.x && g.IO.MousePos.x < column->ClipRect.Max.x)
            table->HoveredColumnBody = (ImGuiTableColumnIdx)column_n;


        column->MinX = offset_x;


        float max_width = TableGetMaxColumnWidth(table, column_n);
        column->WidthGiven = ImMin(column->WidthGiven, max_width);
        column->WidthGiven = ImMax(column->WidthGiven, ImMin(column->WidthRequest, table->MinColumnWidth));
        column->MaxX = offset_x + column->WidthGiven + table->CellSpacingX1 + table->CellSpacingX2 + table->CellPaddingX * 2.0f;






        column->WorkMinX = column->MinX + table->CellPaddingX + table->CellSpacingX1;
        column->WorkMaxX = column->MaxX - table->CellPaddingX - table->CellSpacingX2;
        column->ItemWidth = ImFloor(column->WidthGiven * 0.65f);
        column->ClipRect.Min.x = column->MinX;
        column->ClipRect.Min.y = work_rect.Min.y;
        column->ClipRect.Max.x = column->MaxX; //column->WorkMaxX;
        column->ClipRect.Max.y = FLT_MAX;
        column->ClipRect.ClipWithFull(host_clip_rect);








        column->IsVisibleX = (column->ClipRect.Max.x > column->ClipRect.Min.x);
        column->IsVisibleY = true;
        const bool is_visible = column->IsVisibleX; //&& column->IsVisibleY;
        if (is_visible)
            ImBitArraySetBit(table->VisibleMaskByIndex, column_n);


        column->IsRequestOutput = is_visible || column->AutoFitQueue != 0 || column->CannotSkipItemsQueue != 0;


        column->IsSkipItems = !column->IsEnabled || table->HostSkipItems;
        if (column->IsSkipItems)
            IM_ASSERT(!is_visible);


        column->Flags |= ImGuiTableColumnFlags_IsEnabled;
        if (is_visible)
            column->Flags |= ImGuiTableColumnFlags_IsVisible;
        if (column->SortOrder != -1)
            column->Flags |= ImGuiTableColumnFlags_IsSorted;
        if (table->HoveredColumnBody == column_n)
            column->Flags |= ImGuiTableColumnFlags_IsHovered;





        //if (column->Flags & ImGuiTableColumnFlags_AlignRight)

        //else if (column->Flags & ImGuiTableColumnFlags_AlignCenter)



        column->ContentMaxXFrozen = column->ContentMaxXUnfrozen = column->WorkMinX;
        column->ContentMaxXHeadersUsed = column->ContentMaxXHeadersIdeal = column->WorkMinX;


        if (table->HostSkipItems == false)
        {
            column->AutoFitQueue >>= 1;
            column->CannotSkipItemsQueue >>= 1;
        }

        if (visible_n < table->FreezeColumnsCount)
            host_clip_rect.Min.x = ImClamp(column->MaxX + TABLE_BORDER_SIZE, host_clip_rect.Min.x, host_clip_rect.Max.x);

        offset_x += column->WidthGiven + table->CellSpacingX1 + table->CellSpacingX2 + table->CellPaddingX * 2.0f;
        visible_n++;
    }




    const float unused_x1 = ImMax(table->WorkRect.Min.x, table->Columns[table->RightMostEnabledColumn].ClipRect.Max.x);
    if (is_hovering_table && table->HoveredColumnBody == -1)
    {
        if (g.IO.MousePos.x >= unused_x1)
            table->HoveredColumnBody = (ImGuiTableColumnIdx)table->ColumnsCount;
    }
    if (has_resizable == false && (table->Flags & ImGuiTableFlags_Resizable))
        table->Flags &= ~ImGuiTableFlags_Resizable;




    if (table->RightMostStretchedColumn != -1)
        table->Flags &= ~ImGuiTableFlags_NoHostExtendX;
    if (table->Flags & ImGuiTableFlags_NoHostExtendX)
    {
        table->OuterRect.Max.x = table->WorkRect.Max.x = unused_x1;
        table->InnerClipRect.Max.x = ImMin(table->InnerClipRect.Max.x, unused_x1);
    }
    table->InnerWindow->ParentWorkRect = table->WorkRect;
    table->BorderX1 = table->InnerClipRect.Min.x;
    table->BorderX2 = table->InnerClipRect.Max.x;


    float window_content_max_y;
    if (table->Flags & ImGuiTableFlags_NoHostExtendY)
        window_content_max_y = table->OuterRect.Max.y;
    else
        window_content_max_y = ImMax(table->InnerWindow->ContentRegionRect.Max.y, (table->Flags & ImGuiTableFlags_ScrollY) ? 0.0f : table->OuterRect.Max.y);
    table->InnerWindow->WorkRect.Max.y = ImClamp(window_content_max_y - g.Style.CellPadding.y, table->InnerWindow->WorkRect.Min.y, table->InnerWindow->WorkRect.Max.y);


    TableSetupDrawChannels(table);


    if (table->Flags & ImGuiTableFlags_Resizable)
        TableUpdateBorders(table);
    table_instance->LastFirstRowHeight = 0.0f;
    table->IsLayoutLocked = true;
    table->IsUsingHeaders = false;


    if (TableBeginContextMenuPopup(table))
    {
        TableDrawContextMenu(table);
        EndPopup();
    }



    if (table->IsSortSpecsDirty && (table->Flags & ImGuiTableFlags_Sortable))
        TableSortSpecsBuild(table);


    if (table->FreezeColumnsRequest > 0)
        table->InnerWindow->DecoInnerSizeX1 = table->Columns[table->DisplayOrderToIndex[table->FreezeColumnsRequest - 1]].MaxX - table->OuterRect.Min.x;
    if (table->FreezeRowsRequest > 0)
        table->InnerWindow->DecoInnerSizeY1 = table_instance->LastFrozenHeight;
    table_instance->LastFrozenHeight = 0.0f;


    ImGuiWindow* inner_window = table->InnerWindow;
    if (table->Flags & ImGuiTableFlags_NoClip)
        table->DrawSplitter->SetCurrentChannel(inner_window->DrawList, TABLE_DRAW_CHANNEL_NOCLIP);
    else
        inner_window->DrawList->PushClipRect(inner_window->ClipRect.Min, inner_window->ClipRect.Max, false);
}



void ImGui::TableUpdateBorders(ImGuiTable* table)
{
    ImGuiContext& g = *GImGui;
    IM_ASSERT(table->Flags & ImGuiTableFlags_Resizable);





    ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, table->InstanceCurrent);
    const float hit_half_width = TABLE_RESIZE_SEPARATOR_HALF_THICKNESS;
    const float hit_y1 = table->OuterRect.Min.y;
    const float hit_y2_body = ImMax(table->OuterRect.Max.y, hit_y1 + table_instance->LastOuterHeight);
    const float hit_y2_head = hit_y1 + table_instance->LastFirstRowHeight;

    for (int order_n = 0; order_n < table->ColumnsCount; order_n++)
    {
        if (!IM_BITARRAY_TESTBIT(table->EnabledMaskByDisplayOrder, order_n))
            continue;

        const int column_n = table->DisplayOrderToIndex[order_n];
        ImGuiTableColumn* column = &table->Columns[column_n];
        if (column->Flags & (ImGuiTableColumnFlags_NoResize | ImGuiTableColumnFlags_NoDirectResize_))
            continue;


        const float border_y2_hit = (table->Flags & ImGuiTableFlags_NoBordersInBody) ? hit_y2_head : hit_y2_body;
        if ((table->Flags & ImGuiTableFlags_NoBordersInBody) && table->IsUsingHeaders == false)
            continue;

        if (!column->IsVisibleX && table->LastResizedColumn != column_n)
            continue;

        ImGuiID column_id = TableGetColumnResizeID(table, column_n, table->InstanceCurrent);
        ImRect hit_rect(column->MaxX - hit_half_width, hit_y1, column->MaxX + hit_half_width, border_y2_hit);
        ItemAdd(hit_rect, column_id, NULL, ImGuiItemFlags_NoNav);
        //GetForegroundDrawList()->AddRect(hit_rect.Min, hit_rect.Max, IM_COL32(255, 0, 0, 100));

        bool hovered = false, held = false;
        bool pressed = ButtonBehavior(hit_rect, column_id, &hovered, &held, ImGuiButtonFlags_FlattenChildren | ImGuiButtonFlags_PressedOnClick | ImGuiButtonFlags_PressedOnDoubleClick | ImGuiButtonFlags_NoNavFocus);
        if (pressed && IsMouseDoubleClicked(0))
        {
            TableSetColumnWidthAutoSingle(table, column_n);
            ClearActiveID();
            held = hovered = false;
        }
        if (held)
        {
            if (table->LastResizedColumn == -1)
                table->ResizeLockMinContentsX2 = table->RightMostEnabledColumn != -1 ? table->Columns[table->RightMostEnabledColumn].MaxX : -FLT_MAX;
            table->ResizedColumn = (ImGuiTableColumnIdx)column_n;
            table->InstanceInteracted = table->InstanceCurrent;
        }
        if ((hovered && g.HoveredIdTimer > TABLE_RESIZE_SEPARATOR_FEEDBACK_TIMER) || held)
        {
            table->HoveredColumnBorder = (ImGuiTableColumnIdx)column_n;
            SetMouseCursor(ImGuiMouseCursor_ResizeEW);
        }
    }
}

void    ImGui::EndTable()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL && "Only call EndTable() if BeginTable() returns true!");



    //IM_ASSERT(table->IsLayoutLocked && "Table unused: never called TableNextRow(), is that the intent?");



    if (!table->IsLayoutLocked)
        TableUpdateLayout(table);

    const ImGuiTableFlags flags = table->Flags;
    ImGuiWindow* inner_window = table->InnerWindow;
    ImGuiWindow* outer_window = table->OuterWindow;
    ImGuiTableTempData* temp_data = table->TempData;
    IM_ASSERT(inner_window == g.CurrentWindow);
    IM_ASSERT(outer_window == inner_window || outer_window == inner_window->ParentWindow);

    if (table->IsInsideRow)
        TableEndRow(table);


    if (flags & ImGuiTableFlags_ContextMenuInBody)
        if (table->HoveredColumnBody != -1 && !IsAnyItemHovered() && IsMouseReleased(ImGuiMouseButton_Right))
            TableOpenContextMenu((int)table->HoveredColumnBody);


    ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, table->InstanceCurrent);
    inner_window->DC.PrevLineSize = temp_data->HostBackupPrevLineSize;
    inner_window->DC.CurrLineSize = temp_data->HostBackupCurrLineSize;
    inner_window->DC.CursorMaxPos = temp_data->HostBackupCursorMaxPos;
    const float inner_content_max_y = table->RowPosY2;
    IM_ASSERT(table->RowPosY2 == inner_window->DC.CursorPos.y);
    if (inner_window != outer_window)
        inner_window->DC.CursorMaxPos.y = inner_content_max_y;
    else if (!(flags & ImGuiTableFlags_NoHostExtendY))
        table->OuterRect.Max.y = table->InnerRect.Max.y = ImMax(table->OuterRect.Max.y, inner_content_max_y);
    table->WorkRect.Max.y = ImMax(table->WorkRect.Max.y, table->OuterRect.Max.y);
    table_instance->LastOuterHeight = table->OuterRect.GetHeight();




    if (table->Flags & ImGuiTableFlags_ScrollX)
    {
        const float outer_padding_for_border = (table->Flags & ImGuiTableFlags_BordersOuterV) ? TABLE_BORDER_SIZE : 0.0f;
        float max_pos_x = table->InnerWindow->DC.CursorMaxPos.x;
        if (table->RightMostEnabledColumn != -1)
            max_pos_x = ImMax(max_pos_x, table->Columns[table->RightMostEnabledColumn].WorkMaxX + table->CellPaddingX + table->OuterPaddingX - outer_padding_for_border);
        if (table->ResizedColumn != -1)
            max_pos_x = ImMax(max_pos_x, table->ResizeLockMinContentsX2);
        table->InnerWindow->DC.CursorMaxPos.x = max_pos_x;
    }


    if (!(flags & ImGuiTableFlags_NoClip))
        inner_window->DrawList->PopClipRect();
    inner_window->ClipRect = inner_window->DrawList->_ClipRectStack.back();


    if ((flags & ImGuiTableFlags_Borders) != 0)
        TableDrawBorders(table);

#if 0




    if (table->DummyDrawChannel != (ImGuiTableColumnIdx)-1)
    {
        ImDrawChannel* dummy_channel = &table->DrawSplitter._Channels[table->DummyDrawChannel];
        dummy_channel->_CmdBuffer.resize(0);
        dummy_channel->_IdxBuffer.resize(0);
    }
#endif


    ImDrawListSplitter* splitter = table->DrawSplitter;
    splitter->SetCurrentChannel(inner_window->DrawList, 0);
    if ((table->Flags & ImGuiTableFlags_NoClip) == 0)
        TableMergeDrawChannels(table);
    splitter->Merge(inner_window->DrawList);


    float auto_fit_width_for_fixed = 0.0f;
    float auto_fit_width_for_stretched = 0.0f;
    float auto_fit_width_for_stretched_min = 0.0f;
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
        if (IM_BITARRAY_TESTBIT(table->EnabledMaskByIndex, column_n))
        {
            ImGuiTableColumn* column = &table->Columns[column_n];
            float column_width_request = ((column->Flags & ImGuiTableColumnFlags_WidthFixed) && !(column->Flags & ImGuiTableColumnFlags_NoResize)) ? column->WidthRequest : TableGetColumnWidthAuto(table, column);
            if (column->Flags & ImGuiTableColumnFlags_WidthFixed)
                auto_fit_width_for_fixed += column_width_request;
            else
                auto_fit_width_for_stretched += column_width_request;
            if ((column->Flags & ImGuiTableColumnFlags_WidthStretch) && (column->Flags & ImGuiTableColumnFlags_NoResize) != 0)
                auto_fit_width_for_stretched_min = ImMax(auto_fit_width_for_stretched_min, column_width_request / (column->StretchWeight / table->ColumnsStretchSumWeights));
        }
    const float width_spacings = (table->OuterPaddingX * 2.0f) + (table->CellSpacingX1 + table->CellSpacingX2) * (table->ColumnsEnabledCount - 1);
    table->ColumnsAutoFitWidth = width_spacings + (table->CellPaddingX * 2.0f) * table->ColumnsEnabledCount + auto_fit_width_for_fixed + ImMax(auto_fit_width_for_stretched, auto_fit_width_for_stretched_min);


    if ((table->Flags & ImGuiTableFlags_ScrollX) == 0 && inner_window != outer_window)
    {
        inner_window->Scroll.x = 0.0f;
    }
    else if (table->LastResizedColumn != -1 && table->ResizedColumn == -1 && inner_window->ScrollbarX && table->InstanceInteracted == table->InstanceCurrent)
    {

        const float neighbor_width_to_keep_visible = table->MinColumnWidth + table->CellPaddingX * 2.0f;
        ImGuiTableColumn* column = &table->Columns[table->LastResizedColumn];
        if (column->MaxX < table->InnerClipRect.Min.x)
            SetScrollFromPosX(inner_window, column->MaxX - inner_window->Pos.x - neighbor_width_to_keep_visible, 1.0f);
        else if (column->MaxX > table->InnerClipRect.Max.x)
            SetScrollFromPosX(inner_window, column->MaxX - inner_window->Pos.x + neighbor_width_to_keep_visible, 1.0f);
    }


    if (table->ResizedColumn != -1 && table->InstanceCurrent == table->InstanceInteracted)
    {
        ImGuiTableColumn* column = &table->Columns[table->ResizedColumn];
        const float new_x2 = (g.IO.MousePos.x - g.ActiveIdClickOffset.x + TABLE_RESIZE_SEPARATOR_HALF_THICKNESS);
        const float new_width = ImFloor(new_x2 - column->MinX - table->CellSpacingX1 - table->CellPaddingX * 2.0f);
        table->ResizedColumnNextWidth = new_width;
    }


    IM_ASSERT_USER_ERROR(inner_window->IDStack.back() == table_instance->TableInstanceID, "Mismatching PushID/PopID!");
    IM_ASSERT_USER_ERROR(outer_window->DC.ItemWidthStack.Size >= temp_data->HostBackupItemWidthStackSize, "Too many PopItemWidth!");
    if (table->InstanceCurrent > 0)
        PopID();
    PopID();


    const ImVec2 backup_outer_max_pos = outer_window->DC.CursorMaxPos;
    inner_window->WorkRect = temp_data->HostBackupWorkRect;
    inner_window->ParentWorkRect = temp_data->HostBackupParentWorkRect;
    inner_window->SkipItems = table->HostSkipItems;
    outer_window->DC.CursorPos = table->OuterRect.Min;
    outer_window->DC.ItemWidth = temp_data->HostBackupItemWidth;
    outer_window->DC.ItemWidthStack.Size = temp_data->HostBackupItemWidthStackSize;
    outer_window->DC.ColumnsOffset = temp_data->HostBackupColumnsOffset;




    if (inner_window != outer_window)
    {
        EndChild();
    }
    else
    {
        ItemSize(table->OuterRect.GetSize());
        ItemAdd(table->OuterRect, 0);
    }


    if (table->Flags & ImGuiTableFlags_NoHostExtendX)
    {


        IM_ASSERT((table->Flags & ImGuiTableFlags_ScrollX) == 0);
        outer_window->DC.CursorMaxPos.x = ImMax(backup_outer_max_pos.x, table->OuterRect.Min.x + table->ColumnsAutoFitWidth);
    }
    else if (temp_data->UserOuterSize.x <= 0.0f)
    {
        const float decoration_size = (table->Flags & ImGuiTableFlags_ScrollX) ? inner_window->ScrollbarSizes.x : 0.0f;
        outer_window->DC.IdealMaxPos.x = ImMax(outer_window->DC.IdealMaxPos.x, table->OuterRect.Min.x + table->ColumnsAutoFitWidth + decoration_size - temp_data->UserOuterSize.x);
        outer_window->DC.CursorMaxPos.x = ImMax(backup_outer_max_pos.x, ImMin(table->OuterRect.Max.x, table->OuterRect.Min.x + table->ColumnsAutoFitWidth));
    }
    else
    {
        outer_window->DC.CursorMaxPos.x = ImMax(backup_outer_max_pos.x, table->OuterRect.Max.x);
    }
    if (temp_data->UserOuterSize.y <= 0.0f)
    {
        const float decoration_size = (table->Flags & ImGuiTableFlags_ScrollY) ? inner_window->ScrollbarSizes.y : 0.0f;
        outer_window->DC.IdealMaxPos.y = ImMax(outer_window->DC.IdealMaxPos.y, inner_content_max_y + decoration_size - temp_data->UserOuterSize.y);
        outer_window->DC.CursorMaxPos.y = ImMax(backup_outer_max_pos.y, ImMin(table->OuterRect.Max.y, inner_content_max_y));
    }
    else
    {

        outer_window->DC.CursorMaxPos.y = ImMax(backup_outer_max_pos.y, table->OuterRect.Max.y);
    }


    if (table->IsSettingsDirty)
        TableSaveSettings(table);
    table->IsInitializing = false;


    IM_ASSERT(g.CurrentWindow == outer_window && g.CurrentTable == table);
    IM_ASSERT(g.TablesTempDataStacked > 0);
    temp_data = (--g.TablesTempDataStacked > 0) ? &g.TablesTempData[g.TablesTempDataStacked - 1] : NULL;
    g.CurrentTable = temp_data ? g.Tables.GetByIndex(temp_data->TableIndex) : NULL;
    if (g.CurrentTable)
    {
        g.CurrentTable->TempData = temp_data;
        g.CurrentTable->DrawSplitter = &temp_data->DrawSplitter;
    }
    outer_window->DC.CurrentTableIdx = g.CurrentTable ? g.Tables.GetIndex(g.CurrentTable) : -1;
    NavUpdateCurrentWindowIsScrollPushableX();
}



void ImGui::TableSetupColumn(const char* label, ImGuiTableColumnFlags flags, float init_width_or_weight, ImGuiID user_id)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL && "Need to call TableSetupColumn() after BeginTable()!");
    IM_ASSERT(table->IsLayoutLocked == false && "Need to call call TableSetupColumn() before first row!");
    IM_ASSERT((flags & ImGuiTableColumnFlags_StatusMask_) == 0 && "Illegal to pass StatusMask values to TableSetupColumn()");
    if (table->DeclColumnsCount >= table->ColumnsCount)
    {
        IM_ASSERT_USER_ERROR(table->DeclColumnsCount < table->ColumnsCount, "Called TableSetupColumn() too many times!");
        return;
    }

    ImGuiTableColumn* column = &table->Columns[table->DeclColumnsCount];
    table->DeclColumnsCount++;



    if (table->IsDefaultSizingPolicy && (flags & ImGuiTableColumnFlags_WidthMask_) == 0 && (flags & ImGuiTableFlags_ScrollX) == 0)
        IM_ASSERT(init_width_or_weight <= 0.0f && "Can only specify width/weight if sizing policy is set explicitly in either Table or Column.");



    if ((flags & ImGuiTableColumnFlags_WidthMask_) == 0 && init_width_or_weight > 0.0f)
        if ((table->Flags & ImGuiTableFlags_SizingMask_) == ImGuiTableFlags_SizingFixedFit || (table->Flags & ImGuiTableFlags_SizingMask_) == ImGuiTableFlags_SizingFixedSame)
            flags |= ImGuiTableColumnFlags_WidthFixed;

    TableSetupColumnFlags(table, column, flags);
    column->UserID = user_id;
    flags = column->Flags;


    column->InitStretchWeightOrWidth = init_width_or_weight;
    if (table->IsInitializing)
    {

        if (column->WidthRequest < 0.0f && column->StretchWeight < 0.0f)
        {
            if ((flags & ImGuiTableColumnFlags_WidthFixed) && init_width_or_weight > 0.0f)
                column->WidthRequest = init_width_or_weight;
            if (flags & ImGuiTableColumnFlags_WidthStretch)
                column->StretchWeight = (init_width_or_weight > 0.0f) ? init_width_or_weight : -1.0f;


            if (init_width_or_weight > 0.0f)
                column->AutoFitQueue = 0x00;
        }


        if ((flags & ImGuiTableColumnFlags_DefaultHide) && (table->SettingsLoadedFlags & ImGuiTableFlags_Hideable) == 0)
            column->IsUserEnabled = column->IsUserEnabledNextFrame = false;
        if (flags & ImGuiTableColumnFlags_DefaultSort && (table->SettingsLoadedFlags & ImGuiTableFlags_Sortable) == 0)
        {
            column->SortOrder = 0;
            column->SortDirection = (column->Flags & ImGuiTableColumnFlags_PreferSortDescending) ? (ImS8)ImGuiSortDirection_Descending : (ImU8)(ImGuiSortDirection_Ascending);
        }
    }


    column->NameOffset = -1;
    if (label != NULL && label[0] != 0)
    {
        column->NameOffset = (ImS16)table->ColumnsNames.size();
        table->ColumnsNames.append(label, label + strlen(label) + 1);
    }
}


void ImGui::TableSetupScrollFreeze(int columns, int rows)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL && "Need to call TableSetupColumn() after BeginTable()!");
    IM_ASSERT(table->IsLayoutLocked == false && "Need to call TableSetupColumn() before first row!");
    IM_ASSERT(columns >= 0 && columns < IMGUI_TABLE_MAX_COLUMNS);
    IM_ASSERT(rows >= 0 && rows < 128);

    table->FreezeColumnsRequest = (table->Flags & ImGuiTableFlags_ScrollX) ? (ImGuiTableColumnIdx)ImMin(columns, table->ColumnsCount) : 0;
    table->FreezeColumnsCount = (table->InnerWindow->Scroll.x != 0.0f) ? table->FreezeColumnsRequest : 0;
    table->FreezeRowsRequest = (table->Flags & ImGuiTableFlags_ScrollY) ? (ImGuiTableColumnIdx)rows : 0;
    table->FreezeRowsCount = (table->InnerWindow->Scroll.y != 0.0f) ? table->FreezeRowsRequest : 0;
    table->IsUnfrozenRows = (table->FreezeRowsCount == 0);



    for (int column_n = 0; column_n < table->FreezeColumnsRequest; column_n++)
    {
        int order_n = table->DisplayOrderToIndex[column_n];
        if (order_n != column_n && order_n >= table->FreezeColumnsRequest)
        {
            ImSwap(table->Columns[table->DisplayOrderToIndex[order_n]].DisplayOrder, table->Columns[table->DisplayOrderToIndex[column_n]].DisplayOrder);
            ImSwap(table->DisplayOrderToIndex[order_n], table->DisplayOrderToIndex[column_n]);
        }
    }
}

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------










//-----------------------------------------------------------------------------

int ImGui::TableGetColumnCount()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    return table ? table->ColumnsCount : 0;
}

const char* ImGui::TableGetColumnName(int column_n)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return NULL;
    if (column_n < 0)
        column_n = table->CurrentColumn;
    return TableGetColumnName(table, column_n);
}

const char* ImGui::TableGetColumnName(const ImGuiTable* table, int column_n)
{
    if (table->IsLayoutLocked == false && column_n >= table->DeclColumnsCount)
        return "";
    const ImGuiTableColumn* column = &table->Columns[column_n];
    if (column->NameOffset == -1)
        return "";
    return &table->ColumnsNames.Buf[column->NameOffset];
}







void ImGui::TableSetColumnEnabled(int column_n, bool enabled)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL);
    if (!table)
        return;
    IM_ASSERT(table->Flags & ImGuiTableFlags_Hideable);
    if (column_n < 0)
        column_n = table->CurrentColumn;
    IM_ASSERT(column_n >= 0 && column_n < table->ColumnsCount);
    ImGuiTableColumn* column = &table->Columns[column_n];
    column->IsUserEnabledNextFrame = enabled;
}


ImGuiTableColumnFlags ImGui::TableGetColumnFlags(int column_n)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return ImGuiTableColumnFlags_None;
    if (column_n < 0)
        column_n = table->CurrentColumn;
    if (column_n == table->ColumnsCount)
        return (table->HoveredColumnBody == column_n) ? ImGuiTableColumnFlags_IsHovered : ImGuiTableColumnFlags_None;
    return table->Columns[column_n].Flags;
}







ImRect ImGui::TableGetCellBgRect(const ImGuiTable* table, int column_n)
{
    const ImGuiTableColumn* column = &table->Columns[column_n];
    float x1 = column->MinX;
    float x2 = column->MaxX;
    //if (column->PrevEnabledColumn == -1)

    //if (column->NextEnabledColumn == -1)

    x1 = ImMax(x1, table->WorkRect.Min.x);
    x2 = ImMin(x2, table->WorkRect.Max.x);
    return ImRect(x1, table->RowPosY1, x2, table->RowPosY2);
}


ImGuiID ImGui::TableGetColumnResizeID(ImGuiTable* table, int column_n, int instance_no)
{
    IM_ASSERT(column_n >= 0 && column_n < table->ColumnsCount);
    ImGuiID instance_id = TableGetInstanceID(table, instance_no);
    return instance_id + 1 + column_n;
}


int ImGui::TableGetHoveredColumn()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return -1;
    return (int)table->HoveredColumnBody;
}




int ImGui::TableGetHoveredRow()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return -1;
    ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, table->InstanceCurrent);
    return (int)table_instance->HoveredRowLast;
}

void ImGui::TableSetBgColor(ImGuiTableBgTarget target, ImU32 color, int column_n)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(target != ImGuiTableBgTarget_None);

    if (color == IM_COL32_DISABLE)
        color = 0;


    switch (target)
    {
    case ImGuiTableBgTarget_CellBg:
    {
        if (table->RowPosY1 > table->InnerClipRect.Max.y)
            return;
        if (column_n == -1)
            column_n = table->CurrentColumn;
        if (!IM_BITARRAY_TESTBIT(table->VisibleMaskByIndex, column_n))
            return;
        if (table->RowCellDataCurrent < 0 || table->RowCellData[table->RowCellDataCurrent].Column != column_n)
            table->RowCellDataCurrent++;
        ImGuiTableCellData* cell_data = &table->RowCellData[table->RowCellDataCurrent];
        cell_data->BgColor = color;
        cell_data->Column = (ImGuiTableColumnIdx)column_n;
        break;
    }
    case ImGuiTableBgTarget_RowBg0:
    case ImGuiTableBgTarget_RowBg1:
    {
        if (table->RowPosY1 > table->InnerClipRect.Max.y)
            return;
        IM_ASSERT(column_n == -1);
        int bg_idx = (target == ImGuiTableBgTarget_RowBg1) ? 1 : 0;
        table->RowBgColor[bg_idx] = color;
        break;
    }
    default:
        IM_ASSERT(0);
    }
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------




//-------------------------------------------------------------------------


int ImGui::TableGetRowIndex()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return 0;
    return table->CurrentRow;
}


void ImGui::TableNextRow(ImGuiTableRowFlags row_flags, float row_min_height)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;

    if (!table->IsLayoutLocked)
        TableUpdateLayout(table);
    if (table->IsInsideRow)
        TableEndRow(table);

    table->LastRowFlags = table->RowFlags;
    table->RowFlags = row_flags;
    table->RowMinHeight = row_min_height;
    TableBeginRow(table);



    table->RowPosY2 += table->CellPaddingY * 2.0f;
    table->RowPosY2 = ImMax(table->RowPosY2, table->RowPosY1 + row_min_height);


    table->InnerWindow->SkipItems = true;
}


void ImGui::TableBeginRow(ImGuiTable* table)
{
    ImGuiWindow* window = table->InnerWindow;
    IM_ASSERT(!table->IsInsideRow);


    table->CurrentRow++;
    table->CurrentColumn = -1;
    table->RowBgColor[0] = table->RowBgColor[1] = IM_COL32_DISABLE;
    table->RowCellDataCurrent = -1;
    table->IsInsideRow = true;


    float next_y1 = table->RowPosY2;
    if (table->CurrentRow == 0 && table->FreezeRowsCount > 0)
        next_y1 = window->DC.CursorPos.y = table->OuterRect.Min.y;

    table->RowPosY1 = table->RowPosY2 = next_y1;
    table->RowTextBaseline = 0.0f;
    table->RowIndentOffsetX = window->DC.Indent.x - table->HostIndentX;
    window->DC.PrevLineTextBaseOffset = 0.0f;
    window->DC.CurrLineSize = ImVec2(0.0f, 0.0f);
    window->DC.IsSameLine = window->DC.IsSetPos = false;
    window->DC.CursorMaxPos.y = next_y1;


    if (table->RowFlags & ImGuiTableRowFlags_Headers)
    {
        TableSetBgColor(ImGuiTableBgTarget_RowBg0, GetColorU32(ImGuiCol_TableHeaderBg));
        if (table->CurrentRow == 0)
            table->IsUsingHeaders = true;
    }
}


void ImGui::TableEndRow(ImGuiTable* table)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    IM_ASSERT(window == table->InnerWindow);
    IM_ASSERT(table->IsInsideRow);

    if (table->CurrentColumn != -1)
        TableEndCell(table);


    if (g.LogEnabled)
        LogRenderedText(NULL, "|");



    window->DC.CursorPos.y = table->RowPosY2;


    const float bg_y1 = table->RowPosY1;
    const float bg_y2 = table->RowPosY2;
    const bool unfreeze_rows_actual = (table->CurrentRow + 1 == table->FreezeRowsCount);
    const bool unfreeze_rows_request = (table->CurrentRow + 1 == table->FreezeRowsRequest);
    if (table->CurrentRow == 0)
        TableGetInstanceData(table, table->InstanceCurrent)->LastFirstRowHeight = bg_y2 - bg_y1;

    const bool is_visible = (bg_y2 >= table->InnerClipRect.Min.y && bg_y1 <= table->InnerClipRect.Max.y);
    if (is_visible)
    {

        if (table->HoveredColumnBody != -1 && g.IO.MousePos.y >= bg_y1 && g.IO.MousePos.y < bg_y2)
            TableGetInstanceData(table, table->InstanceCurrent)->HoveredRowNext = table->CurrentRow;


        ImU32 bg_col0 = 0;
        ImU32 bg_col1 = 0;
        if (table->RowBgColor[0] != IM_COL32_DISABLE)
            bg_col0 = table->RowBgColor[0];
        else if (table->Flags & ImGuiTableFlags_RowBg)
            bg_col0 = GetColorU32((table->RowBgColorCounter & 1) ? ImGuiCol_TableRowBgAlt : ImGuiCol_TableRowBg);
        if (table->RowBgColor[1] != IM_COL32_DISABLE)
            bg_col1 = table->RowBgColor[1];


        ImU32 border_col = 0;
        const float border_size = TABLE_BORDER_SIZE;
        if (table->CurrentRow > 0 || table->InnerWindow == table->OuterWindow)
            if (table->Flags & ImGuiTableFlags_BordersInnerH)
                border_col = (table->LastRowFlags & ImGuiTableRowFlags_Headers) ? table->BorderColorStrong : table->BorderColorLight;

        const bool draw_cell_bg_color = table->RowCellDataCurrent >= 0;
        const bool draw_strong_bottom_border = unfreeze_rows_actual;
        if ((bg_col0 | bg_col1 | border_col) != 0 || draw_strong_bottom_border || draw_cell_bg_color)
        {


            if ((table->Flags & ImGuiTableFlags_NoClip) == 0)
                window->DrawList->_CmdHeader.ClipRect = table->Bg0ClipRectForDrawCmd.ToVec4();
            table->DrawSplitter->SetCurrentChannel(window->DrawList, TABLE_DRAW_CHANNEL_BG0);
        }



        if (bg_col0 || bg_col1)
        {
            ImRect row_rect(table->WorkRect.Min.x, bg_y1, table->WorkRect.Max.x, bg_y2);
            row_rect.ClipWith(table->BgClipRect);
            if (bg_col0 != 0 && row_rect.Min.y < row_rect.Max.y)
                window->DrawList->AddRectFilled(row_rect.Min, row_rect.Max, bg_col0);
            if (bg_col1 != 0 && row_rect.Min.y < row_rect.Max.y)
                window->DrawList->AddRectFilled(row_rect.Min, row_rect.Max, bg_col1);
        }


        if (draw_cell_bg_color)
        {
            ImGuiTableCellData* cell_data_end = &table->RowCellData[table->RowCellDataCurrent];
            for (ImGuiTableCellData* cell_data = &table->RowCellData[0]; cell_data <= cell_data_end; cell_data++)
            {


                const ImGuiTableColumn* column = &table->Columns[cell_data->Column];
                ImRect cell_bg_rect = TableGetCellBgRect(table, cell_data->Column);
                cell_bg_rect.ClipWith(table->BgClipRect);
                cell_bg_rect.Min.x = ImMax(cell_bg_rect.Min.x, column->ClipRect.Min.x);
                cell_bg_rect.Max.x = ImMin(cell_bg_rect.Max.x, column->MaxX);
                window->DrawList->AddRectFilled(cell_bg_rect.Min, cell_bg_rect.Max, cell_data->BgColor);
            }
        }


        if (border_col && bg_y1 >= table->BgClipRect.Min.y && bg_y1 < table->BgClipRect.Max.y)
            window->DrawList->AddLine(ImVec2(table->BorderX1, bg_y1), ImVec2(table->BorderX2, bg_y1), border_col, border_size);


        if (draw_strong_bottom_border && bg_y2 >= table->BgClipRect.Min.y && bg_y2 < table->BgClipRect.Max.y)
            window->DrawList->AddLine(ImVec2(table->BorderX1, bg_y2), ImVec2(table->BorderX2, bg_y2), table->BorderColorStrong, border_size);
    }




    if (unfreeze_rows_request)
        for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
            table->Columns[column_n].NavLayerCurrent = ImGuiNavLayer_Main;
    if (unfreeze_rows_actual)
    {
        IM_ASSERT(table->IsUnfrozenRows == false);
        const float y0 = ImMax(table->RowPosY2 + 1, window->InnerClipRect.Min.y);
        table->IsUnfrozenRows = true;
        TableGetInstanceData(table, table->InstanceCurrent)->LastFrozenHeight = y0 - table->OuterRect.Min.y;


        table->BgClipRect.Min.y = table->Bg2ClipRectForDrawCmd.Min.y = ImMin(y0, window->InnerClipRect.Max.y);
        table->BgClipRect.Max.y = table->Bg2ClipRectForDrawCmd.Max.y = window->InnerClipRect.Max.y;
        table->Bg2DrawChannelCurrent = table->Bg2DrawChannelUnfrozen;
        IM_ASSERT(table->Bg2ClipRectForDrawCmd.Min.y <= table->Bg2ClipRectForDrawCmd.Max.y);

        float row_height = table->RowPosY2 - table->RowPosY1;
        table->RowPosY2 = window->DC.CursorPos.y = table->WorkRect.Min.y + table->RowPosY2 - table->OuterRect.Min.y;
        table->RowPosY1 = table->RowPosY2 - row_height;
        for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
        {
            ImGuiTableColumn* column = &table->Columns[column_n];
            column->DrawChannelCurrent = column->DrawChannelUnfrozen;
            column->ClipRect.Min.y = table->Bg2ClipRectForDrawCmd.Min.y;
        }


        SetWindowClipRectBeforeSetChannel(window, table->Columns[0].ClipRect);
        table->DrawSplitter->SetCurrentChannel(window->DrawList, table->Columns[0].DrawChannelCurrent);
    }

    if (!(table->RowFlags & ImGuiTableRowFlags_Headers))
        table->RowBgColorCounter++;
    table->IsInsideRow = false;
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------





//-------------------------------------------------------------------------

int ImGui::TableGetColumnIndex()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return 0;
    return table->CurrentColumn;
}


bool ImGui::TableSetColumnIndex(int column_n)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return false;

    if (table->CurrentColumn != column_n)
    {
        if (table->CurrentColumn != -1)
            TableEndCell(table);
        IM_ASSERT(column_n >= 0 && table->ColumnsCount);
        TableBeginCell(table, column_n);
    }



    return table->Columns[column_n].IsRequestOutput;
}


bool ImGui::TableNextColumn()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (!table)
        return false;

    if (table->IsInsideRow && table->CurrentColumn + 1 < table->ColumnsCount)
    {
        if (table->CurrentColumn != -1)
            TableEndCell(table);
        TableBeginCell(table, table->CurrentColumn + 1);
    }
    else
    {
        TableNextRow();
        TableBeginCell(table, 0);
    }



    return table->Columns[table->CurrentColumn].IsRequestOutput;
}





void ImGui::TableBeginCell(ImGuiTable* table, int column_n)
{
    ImGuiContext& g = *GImGui;
    ImGuiTableColumn* column = &table->Columns[column_n];
    ImGuiWindow* window = table->InnerWindow;
    table->CurrentColumn = column_n;


    float start_x = column->WorkMinX;
    if (column->Flags & ImGuiTableColumnFlags_IndentEnable)
        start_x += table->RowIndentOffsetX;

    window->DC.CursorPos.x = start_x;
    window->DC.CursorPos.y = table->RowPosY1 + table->CellPaddingY;
    window->DC.CursorMaxPos.x = window->DC.CursorPos.x;
    window->DC.ColumnsOffset.x = start_x - window->Pos.x - window->DC.Indent.x;
    window->DC.CurrLineTextBaseOffset = table->RowTextBaseline;
    window->DC.NavLayerCurrent = (ImGuiNavLayer)column->NavLayerCurrent;


    window->WorkRect.Min.y = window->DC.CursorPos.y;
    window->WorkRect.Min.x = column->WorkMinX;
    window->WorkRect.Max.x = column->WorkMaxX;
    window->DC.ItemWidth = column->ItemWidth;

    window->SkipItems = column->IsSkipItems;
    if (column->IsSkipItems)
    {
        g.LastItemData.ID = 0;
        g.LastItemData.StatusFlags = 0;
    }

    if (table->Flags & ImGuiTableFlags_NoClip)
    {

        table->DrawSplitter->SetCurrentChannel(window->DrawList, TABLE_DRAW_CHANNEL_NOCLIP);
        //IM_ASSERT(table->DrawSplitter._Current == TABLE_DRAW_CHANNEL_NOCLIP);
    }
    else
    {

        SetWindowClipRectBeforeSetChannel(window, column->ClipRect);
        table->DrawSplitter->SetCurrentChannel(window->DrawList, column->DrawChannelCurrent);
    }


    if (g.LogEnabled && !column->IsSkipItems)
    {
        LogRenderedText(&window->DC.CursorPos, "|");
        g.LogLinePosY = FLT_MAX;
    }
}


void ImGui::TableEndCell(ImGuiTable* table)
{
    ImGuiTableColumn* column = &table->Columns[table->CurrentColumn];
    ImGuiWindow* window = table->InnerWindow;

    if (window->DC.IsSetPos)
        ErrorCheckUsingSetCursorPosToExtendParentBoundaries();


    float* p_max_pos_x;
    if (table->RowFlags & ImGuiTableRowFlags_Headers)
        p_max_pos_x = &column->ContentMaxXHeadersUsed;
    else
        p_max_pos_x = table->IsUnfrozenRows ? &column->ContentMaxXUnfrozen : &column->ContentMaxXFrozen;
    *p_max_pos_x = ImMax(*p_max_pos_x, window->DC.CursorMaxPos.x);
    if (column->IsEnabled)
        table->RowPosY2 = ImMax(table->RowPosY2, window->DC.CursorMaxPos.y + table->CellPaddingY);
    column->ItemWidth = window->DC.ItemWidth;



    table->RowTextBaseline = ImMax(table->RowTextBaseline, window->DC.PrevLineTextBaseOffset);
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------






//-------------------------------------------------------------------------


float ImGui::TableGetMaxColumnWidth(const ImGuiTable* table, int column_n)
{
    const ImGuiTableColumn* column = &table->Columns[column_n];
    float max_width = FLT_MAX;
    const float min_column_distance = table->MinColumnWidth + table->CellPaddingX * 2.0f + table->CellSpacingX1 + table->CellSpacingX2;
    if (table->Flags & ImGuiTableFlags_ScrollX)
    {


        if (column->DisplayOrder < table->FreezeColumnsRequest)
        {
            max_width = (table->InnerClipRect.Max.x - (table->FreezeColumnsRequest - column->DisplayOrder) * min_column_distance) - column->MinX;
            max_width = max_width - table->OuterPaddingX - table->CellPaddingX - table->CellSpacingX2;
        }
    }
    else if ((table->Flags & ImGuiTableFlags_NoKeepColumnsVisible) == 0)
    {





        max_width = table->WorkRect.Max.x - (table->ColumnsEnabledCount - column->IndexWithinEnabledSet - 1) * min_column_distance - column->MinX;
        //max_width -= table->CellSpacingX1;
        max_width -= table->CellSpacingX2;
        max_width -= table->CellPaddingX * 2.0f;
        max_width -= table->OuterPaddingX;
    }
    return max_width;
}


float ImGui::TableGetColumnWidthAuto(ImGuiTable* table, ImGuiTableColumn* column)
{
    const float content_width_body = ImMax(column->ContentMaxXFrozen, column->ContentMaxXUnfrozen) - column->WorkMinX;
    const float content_width_headers = column->ContentMaxXHeadersIdeal - column->WorkMinX;
    float width_auto = content_width_body;
    if (!(column->Flags & ImGuiTableColumnFlags_NoHeaderWidth))
        width_auto = ImMax(width_auto, content_width_headers);


    if ((column->Flags & ImGuiTableColumnFlags_WidthFixed) && column->InitStretchWeightOrWidth > 0.0f)
        if (!(table->Flags & ImGuiTableFlags_Resizable) || (column->Flags & ImGuiTableColumnFlags_NoResize))
            width_auto = column->InitStretchWeightOrWidth;

    return ImMax(width_auto, table->MinColumnWidth);
}


void ImGui::TableSetColumnWidth(int column_n, float width)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL && table->IsLayoutLocked == false);
    IM_ASSERT(column_n >= 0 && column_n < table->ColumnsCount);
    ImGuiTableColumn* column_0 = &table->Columns[column_n];
    float column_0_width = width;



    IM_ASSERT(table->MinColumnWidth > 0.0f);
    const float min_width = table->MinColumnWidth;
    const float max_width = ImMax(min_width, TableGetMaxColumnWidth(table, column_n));
    column_0_width = ImClamp(column_0_width, min_width, max_width);
    if (column_0->WidthGiven == column_0_width || column_0->WidthRequest == column_0_width)
        return;

    //IMGUI_DEBUG_PRINT("TableSetColumnWidth(%d, %.1f->%.1f)\n", column_0_idx, column_0->WidthGiven, column_0_width);
    ImGuiTableColumn* column_1 = (column_0->NextEnabledColumn != -1) ? &table->Columns[column_0->NextEnabledColumn] : NULL;



































    if (column_0->Flags & ImGuiTableColumnFlags_WidthFixed)
        if (!column_1 || table->LeftMostStretchedColumn == -1 || table->Columns[table->LeftMostStretchedColumn].DisplayOrder >= column_0->DisplayOrder)
        {
            column_0->WidthRequest = column_0_width;
            table->IsSettingsDirty = true;
            return;
        }


    if (column_1 == NULL)
        column_1 = (column_0->PrevEnabledColumn != -1) ? &table->Columns[column_0->PrevEnabledColumn] : NULL;
    if (column_1 == NULL)
        return;



    float column_1_width = ImMax(column_1->WidthRequest - (column_0_width - column_0->WidthRequest), min_width);
    column_0_width = column_0->WidthRequest + column_1->WidthRequest - column_1_width;
    IM_ASSERT(column_0_width > 0.0f && column_1_width > 0.0f);
    column_0->WidthRequest = column_0_width;
    column_1->WidthRequest = column_1_width;
    if ((column_0->Flags | column_1->Flags) & ImGuiTableColumnFlags_WidthStretch)
        TableUpdateColumnsWeightFromWidth(table);
    table->IsSettingsDirty = true;
}



void ImGui::TableSetColumnWidthAutoSingle(ImGuiTable* table, int column_n)
{

    ImGuiTableColumn* column = &table->Columns[column_n];
    if (!column->IsEnabled)
        return;
    column->CannotSkipItemsQueue = (1 << 0);
    table->AutoFitSingleColumn = (ImGuiTableColumnIdx)column_n;
}

void ImGui::TableSetColumnWidthAutoAll(ImGuiTable* table)
{
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        ImGuiTableColumn* column = &table->Columns[column_n];
        if (!column->IsEnabled && !(column->Flags & ImGuiTableColumnFlags_WidthStretch))
            continue;
        column->CannotSkipItemsQueue = (1 << 0);
        column->AutoFitQueue = (1 << 1);
    }
}

void ImGui::TableUpdateColumnsWeightFromWidth(ImGuiTable* table)
{
    IM_ASSERT(table->LeftMostStretchedColumn != -1 && table->RightMostStretchedColumn != -1);


    float visible_weight = 0.0f;
    float visible_width = 0.0f;
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        ImGuiTableColumn* column = &table->Columns[column_n];
        if (!column->IsEnabled || !(column->Flags & ImGuiTableColumnFlags_WidthStretch))
            continue;
        IM_ASSERT(column->StretchWeight > 0.0f);
        visible_weight += column->StretchWeight;
        visible_width += column->WidthRequest;
    }
    IM_ASSERT(visible_weight > 0.0f && visible_width > 0.0f);


    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        ImGuiTableColumn* column = &table->Columns[column_n];
        if (!column->IsEnabled || !(column->Flags & ImGuiTableColumnFlags_WidthStretch))
            continue;
        column->StretchWeight = (column->WidthRequest / visible_width) * visible_weight;
        IM_ASSERT(column->StretchWeight > 0.0f);
    }
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------





//-------------------------------------------------------------------------



void ImGui::TablePushBackgroundChannel()
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    ImGuiTable* table = g.CurrentTable;


    table->HostBackupInnerClipRect = window->ClipRect;
    SetWindowClipRectBeforeSetChannel(window, table->Bg2ClipRectForDrawCmd);
    table->DrawSplitter->SetCurrentChannel(window->DrawList, table->Bg2DrawChannelCurrent);
}

void ImGui::TablePopBackgroundChannel()
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    ImGuiTable* table = g.CurrentTable;
    ImGuiTableColumn* column = &table->Columns[table->CurrentColumn];


    SetWindowClipRectBeforeSetChannel(window, table->HostBackupInnerClipRect);
    table->DrawSplitter->SetCurrentChannel(window->DrawList, column->DrawChannelCurrent);
}

















void ImGui::TableSetupDrawChannels(ImGuiTable* table)
{
    const int freeze_row_multiplier = (table->FreezeRowsCount > 0) ? 2 : 1;
    const int channels_for_row = (table->Flags & ImGuiTableFlags_NoClip) ? 1 : table->ColumnsEnabledCount;
    const int channels_for_bg = 1 + 1 * freeze_row_multiplier;
    const int channels_for_dummy = (table->ColumnsEnabledCount < table->ColumnsCount || (memcmp(table->VisibleMaskByIndex, table->EnabledMaskByIndex, ImBitArrayGetStorageSizeInBytes(table->ColumnsCount)) != 0)) ? +1 : 0;
    const int channels_total = channels_for_bg + (channels_for_row * freeze_row_multiplier) + channels_for_dummy;
    table->DrawSplitter->Split(table->InnerWindow->DrawList, channels_total);
    table->DummyDrawChannel = (ImGuiTableDrawChannelIdx)((channels_for_dummy > 0) ? channels_total - 1 : -1);
    table->Bg2DrawChannelCurrent = TABLE_DRAW_CHANNEL_BG2_FROZEN;
    table->Bg2DrawChannelUnfrozen = (ImGuiTableDrawChannelIdx)((table->FreezeRowsCount > 0) ? 2 + channels_for_row : TABLE_DRAW_CHANNEL_BG2_FROZEN);

    int draw_channel_current = 2;
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        ImGuiTableColumn* column = &table->Columns[column_n];
        if (column->IsVisibleX && column->IsVisibleY)
        {
            column->DrawChannelFrozen = (ImGuiTableDrawChannelIdx)(draw_channel_current);
            column->DrawChannelUnfrozen = (ImGuiTableDrawChannelIdx)(draw_channel_current + (table->FreezeRowsCount > 0 ? channels_for_row + 1 : 0));
            if (!(table->Flags & ImGuiTableFlags_NoClip))
                draw_channel_current++;
        }
        else
        {
            column->DrawChannelFrozen = column->DrawChannelUnfrozen = table->DummyDrawChannel;
        }
        column->DrawChannelCurrent = column->DrawChannelFrozen;
    }




    table->BgClipRect = table->InnerClipRect;
    table->Bg0ClipRectForDrawCmd = table->OuterWindow->ClipRect;
    table->Bg2ClipRectForDrawCmd = table->HostClipRect;
    IM_ASSERT(table->BgClipRect.Min.y <= table->BgClipRect.Max.y);
}




//




//



//






//







//

void ImGui::TableMergeDrawChannels(ImGuiTable* table)
{
    ImGuiContext& g = *GImGui;
    ImDrawListSplitter* splitter = table->DrawSplitter;
    const bool has_freeze_v = (table->FreezeRowsCount > 0);
    const bool has_freeze_h = (table->FreezeColumnsCount > 0);
    IM_ASSERT(splitter->_Current == 0);


    struct MergeGroup
    {
        ImRect          ClipRect;
        int             ChannelsCount = 0;
        ImBitArrayPtr   ChannelsMask = NULL;
    };
    int merge_group_mask = 0x00;
    MergeGroup merge_groups[4];


    const int max_draw_channels = (4 + table->ColumnsCount * 2);
    const int size_for_masks_bitarrays_one = (int)ImBitArrayGetStorageSizeInBytes(max_draw_channels);
    g.TempBuffer.reserve(size_for_masks_bitarrays_one * 5);
    memset(g.TempBuffer.Data, 0, size_for_masks_bitarrays_one * 5);
    for (int n = 0; n < IM_ARRAYSIZE(merge_groups); n++)
        merge_groups[n].ChannelsMask = (ImBitArrayPtr)(void*)(g.TempBuffer.Data + (size_for_masks_bitarrays_one * n));
    ImBitArrayPtr remaining_mask = (ImBitArrayPtr)(void*)(g.TempBuffer.Data + (size_for_masks_bitarrays_one * 4));


    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        if (!IM_BITARRAY_TESTBIT(table->VisibleMaskByIndex, column_n))
            continue;
        ImGuiTableColumn* column = &table->Columns[column_n];

        const int merge_group_sub_count = has_freeze_v ? 2 : 1;
        for (int merge_group_sub_n = 0; merge_group_sub_n < merge_group_sub_count; merge_group_sub_n++)
        {
            const int channel_no = (merge_group_sub_n == 0) ? column->DrawChannelFrozen : column->DrawChannelUnfrozen;


            ImDrawChannel* src_channel = &splitter->_Channels[channel_no];
            if (src_channel->_CmdBuffer.Size > 0 && src_channel->_CmdBuffer.back().ElemCount == 0 && src_channel->_CmdBuffer.back().UserCallback == NULL)
                src_channel->_CmdBuffer.pop_back();
            if (src_channel->_CmdBuffer.Size != 1)
                continue;



            if (!(column->Flags & ImGuiTableColumnFlags_NoClip))
            {
                float content_max_x;
                if (!has_freeze_v)
                    content_max_x = ImMax(column->ContentMaxXUnfrozen, column->ContentMaxXHeadersUsed);
                else if (merge_group_sub_n == 0)
                    content_max_x = ImMax(column->ContentMaxXFrozen, column->ContentMaxXHeadersUsed);
                else
                    content_max_x = column->ContentMaxXUnfrozen;
                if (content_max_x > column->ClipRect.Max.x)
                    continue;
            }

            const int merge_group_n = (has_freeze_h && column_n < table->FreezeColumnsCount ? 0 : 1) + (has_freeze_v && merge_group_sub_n == 0 ? 0 : 2);
            IM_ASSERT(channel_no < max_draw_channels);
            MergeGroup* merge_group = &merge_groups[merge_group_n];
            if (merge_group->ChannelsCount == 0)
                merge_group->ClipRect = ImRect(+FLT_MAX, +FLT_MAX, -FLT_MAX, -FLT_MAX);
            ImBitArraySetBit(merge_group->ChannelsMask, channel_no);
            merge_group->ChannelsCount++;
            merge_group->ClipRect.Add(src_channel->_CmdBuffer[0].ClipRect);
            merge_group_mask |= (1 << merge_group_n);
        }



        column->DrawChannelCurrent = (ImGuiTableDrawChannelIdx)-1;
    }


#if 0
    if (g.IO.KeyShift)
        for (int merge_group_n = 0; merge_group_n < IM_ARRAYSIZE(merge_groups); merge_group_n++)
        {
            MergeGroup* merge_group = &merge_groups[merge_group_n];
            if (merge_group->ChannelsCount == 0)
                continue;
            char buf[32];
            ImFormatString(buf, 32, "MG%d:%d", merge_group_n, merge_group->ChannelsCount);
            ImVec2 text_pos = merge_group->ClipRect.Min + ImVec2(4, 4);
            ImVec2 text_size = CalcTextSize(buf, NULL);
            GetForegroundDrawList()->AddRectFilled(text_pos, text_pos + text_size, IM_COL32(0, 0, 0, 255));
            GetForegroundDrawList()->AddText(text_pos, IM_COL32(255, 255, 0, 255), buf, NULL);
            GetForegroundDrawList()->AddRect(merge_group->ClipRect.Min, merge_group->ClipRect.Max, IM_COL32(255, 255, 0, 255));
        }
#endif


    if (merge_group_mask != 0)
    {

        const int LEADING_DRAW_CHANNELS = 2;
        g.DrawChannelsTempMergeBuffer.resize(splitter->_Count - LEADING_DRAW_CHANNELS);
        ImDrawChannel* dst_tmp = g.DrawChannelsTempMergeBuffer.Data;
        ImBitArraySetBitRange(remaining_mask, LEADING_DRAW_CHANNELS, splitter->_Count);
        ImBitArrayClearBit(remaining_mask, table->Bg2DrawChannelUnfrozen);
        IM_ASSERT(has_freeze_v == false || table->Bg2DrawChannelUnfrozen != TABLE_DRAW_CHANNEL_BG2_FROZEN);
        int remaining_count = splitter->_Count - (has_freeze_v ? LEADING_DRAW_CHANNELS + 1 : LEADING_DRAW_CHANNELS);
        //ImRect host_rect = (table->InnerWindow == table->OuterWindow) ? table->InnerClipRect : table->HostClipRect;
        ImRect host_rect = table->HostClipRect;
        for (int merge_group_n = 0; merge_group_n < IM_ARRAYSIZE(merge_groups); merge_group_n++)
        {
            if (int merge_channels_count = merge_groups[merge_group_n].ChannelsCount)
            {
                MergeGroup* merge_group = &merge_groups[merge_group_n];
                ImRect merge_clip_rect = merge_group->ClipRect;








                if ((merge_group_n & 1) == 0 || !has_freeze_h)
                    merge_clip_rect.Min.x = ImMin(merge_clip_rect.Min.x, host_rect.Min.x);
                if ((merge_group_n & 2) == 0 || !has_freeze_v)
                    merge_clip_rect.Min.y = ImMin(merge_clip_rect.Min.y, host_rect.Min.y);
                if ((merge_group_n & 1) != 0)
                    merge_clip_rect.Max.x = ImMax(merge_clip_rect.Max.x, host_rect.Max.x);
                if ((merge_group_n & 2) != 0 && (table->Flags & ImGuiTableFlags_NoHostExtendY) == 0)
                    merge_clip_rect.Max.y = ImMax(merge_clip_rect.Max.y, host_rect.Max.y);
                //GetForegroundDrawList()->AddRect(merge_group->ClipRect.Min, merge_group->ClipRect.Max, IM_COL32(255, 0, 0, 200), 0.0f, 0, 1.0f);
                //GetForegroundDrawList()->AddLine(merge_group->ClipRect.Min, merge_clip_rect.Min, IM_COL32(255, 100, 0, 200));
                //GetForegroundDrawList()->AddLine(merge_group->ClipRect.Max, merge_clip_rect.Max, IM_COL32(255, 100, 0, 200));
                remaining_count -= merge_group->ChannelsCount;
                for (int n = 0; n < (size_for_masks_bitarrays_one >> 2); n++)
                    remaining_mask[n] &= ~merge_group->ChannelsMask[n];
                for (int n = 0; n < splitter->_Count && merge_channels_count != 0; n++)
                {

                    if (!IM_BITARRAY_TESTBIT(merge_group->ChannelsMask, n))
                        continue;
                    IM_BITARRAY_CLEARBIT(merge_group->ChannelsMask, n);
                    merge_channels_count--;

                    ImDrawChannel* channel = &splitter->_Channels[n];
                    IM_ASSERT(channel->_CmdBuffer.Size == 1 && merge_clip_rect.Contains(ImRect(channel->_CmdBuffer[0].ClipRect)));
                    channel->_CmdBuffer[0].ClipRect = merge_clip_rect.ToVec4();
                    memcpy(dst_tmp++, channel, sizeof(ImDrawChannel));
                }
            }


            if (merge_group_n == 1 && has_freeze_v)
                memcpy(dst_tmp++, &splitter->_Channels[table->Bg2DrawChannelUnfrozen], sizeof(ImDrawChannel));
        }


        for (int n = 0; n < splitter->_Count && remaining_count != 0; n++)
        {
            if (!IM_BITARRAY_TESTBIT(remaining_mask, n))
                continue;
            ImDrawChannel* channel = &splitter->_Channels[n];
            memcpy(dst_tmp++, channel, sizeof(ImDrawChannel));
            remaining_count--;
        }
        IM_ASSERT(dst_tmp == g.DrawChannelsTempMergeBuffer.Data + g.DrawChannelsTempMergeBuffer.Size);
        memcpy(splitter->_Channels.Data + LEADING_DRAW_CHANNELS, g.DrawChannelsTempMergeBuffer.Data, (splitter->_Count - LEADING_DRAW_CHANNELS) * sizeof(ImDrawChannel));
    }
}


void ImGui::TableDrawBorders(ImGuiTable* table)
{
    ImGuiWindow* inner_window = table->InnerWindow;
    if (!table->OuterWindow->ClipRect.Overlaps(table->OuterRect))
        return;

    ImDrawList* inner_drawlist = inner_window->DrawList;
    table->DrawSplitter->SetCurrentChannel(inner_drawlist, TABLE_DRAW_CHANNEL_BG0);
    inner_drawlist->PushClipRect(table->Bg0ClipRectForDrawCmd.Min, table->Bg0ClipRectForDrawCmd.Max, false);


    ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, table->InstanceCurrent);
    const float border_size = TABLE_BORDER_SIZE;
    const float draw_y1 = table->InnerRect.Min.y;
    const float draw_y2_body = table->InnerRect.Max.y;
    const float draw_y2_head = table->IsUsingHeaders ? ImMin(table->InnerRect.Max.y, (table->FreezeRowsCount >= 1 ? table->InnerRect.Min.y : table->WorkRect.Min.y) + table_instance->LastFirstRowHeight) : draw_y1;
    if (table->Flags & ImGuiTableFlags_BordersInnerV)
    {
        for (int order_n = 0; order_n < table->ColumnsCount; order_n++)
        {
            if (!IM_BITARRAY_TESTBIT(table->EnabledMaskByDisplayOrder, order_n))
                continue;

            const int column_n = table->DisplayOrderToIndex[order_n];
            ImGuiTableColumn* column = &table->Columns[column_n];
            const bool is_hovered = (table->HoveredColumnBorder == column_n);
            const bool is_resized = (table->ResizedColumn == column_n) && (table->InstanceInteracted == table->InstanceCurrent);
            const bool is_resizable = (column->Flags & (ImGuiTableColumnFlags_NoResize | ImGuiTableColumnFlags_NoDirectResize_)) == 0;
            const bool is_frozen_separator = (table->FreezeColumnsCount == order_n + 1);
            if (column->MaxX > table->InnerClipRect.Max.x && !is_resized)
                continue;


            if (column->NextEnabledColumn == -1 && !is_resizable)
                if ((table->Flags & ImGuiTableFlags_SizingMask_) != ImGuiTableFlags_SizingFixedSame || (table->Flags & ImGuiTableFlags_NoHostExtendX))
                    continue;
            if (column->MaxX <= column->ClipRect.Min.x)
                continue;



            ImU32 col;
            float draw_y2;
            if (is_hovered || is_resized || is_frozen_separator)
            {
                draw_y2 = draw_y2_body;
                col = is_resized ? GetColorU32(ImGuiCol_SeparatorActive) : is_hovered ? GetColorU32(ImGuiCol_SeparatorHovered) : table->BorderColorStrong;
            }
            else
            {
                draw_y2 = (table->Flags & (ImGuiTableFlags_NoBordersInBody | ImGuiTableFlags_NoBordersInBodyUntilResize)) ? draw_y2_head : draw_y2_body;
                col = (table->Flags & (ImGuiTableFlags_NoBordersInBody | ImGuiTableFlags_NoBordersInBodyUntilResize)) ? table->BorderColorStrong : table->BorderColorLight;
            }

            if (draw_y2 > draw_y1)
                inner_drawlist->AddLine(ImVec2(column->MaxX, draw_y1), ImVec2(column->MaxX, draw_y2), col, border_size);
        }
    }



    if (table->Flags & ImGuiTableFlags_BordersOuter)
    {





        const ImRect outer_border = table->OuterRect;
        const ImU32 outer_col = table->BorderColorStrong;
        if ((table->Flags & ImGuiTableFlags_BordersOuter) == ImGuiTableFlags_BordersOuter)
        {
            inner_drawlist->AddRect(outer_border.Min, outer_border.Max, outer_col, 0.0f, 0, border_size);
        }
        else if (table->Flags & ImGuiTableFlags_BordersOuterV)
        {
            inner_drawlist->AddLine(outer_border.Min, ImVec2(outer_border.Min.x, outer_border.Max.y), outer_col, border_size);
            inner_drawlist->AddLine(ImVec2(outer_border.Max.x, outer_border.Min.y), outer_border.Max, outer_col, border_size);
        }
        else if (table->Flags & ImGuiTableFlags_BordersOuterH)
        {
            inner_drawlist->AddLine(outer_border.Min, ImVec2(outer_border.Max.x, outer_border.Min.y), outer_col, border_size);
            inner_drawlist->AddLine(ImVec2(outer_border.Min.x, outer_border.Max.y), outer_border.Max, outer_col, border_size);
        }
    }
    if ((table->Flags & ImGuiTableFlags_BordersInnerH) && table->RowPosY2 < table->OuterRect.Max.y)
    {

        const float border_y = table->RowPosY2;
        if (border_y >= table->BgClipRect.Min.y && border_y < table->BgClipRect.Max.y)
            inner_drawlist->AddLine(ImVec2(table->BorderX1, border_y), ImVec2(table->BorderX2, border_y), table->BorderColorLight, border_size);
    }

    inner_drawlist->PopClipRect();
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------






//-------------------------------------------------------------------------





ImGuiTableSortSpecs* ImGui::TableGetSortSpecs()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL);

    if (!(table->Flags & ImGuiTableFlags_Sortable))
        return NULL;


    if (!table->IsLayoutLocked)
        TableUpdateLayout(table);

    TableSortSpecsBuild(table);
    return &table->SortSpecs;
}

static inline ImGuiSortDirection TableGetColumnAvailSortDirection(ImGuiTableColumn* column, int n)
{
    IM_ASSERT(n < column->SortDirectionsAvailCount);
    return (column->SortDirectionsAvailList >> (n << 1)) & 0x03;
}


void ImGui::TableFixColumnSortDirection(ImGuiTable* table, ImGuiTableColumn* column)
{
    if (column->SortOrder == -1 || (column->SortDirectionsAvailMask & (1 << column->SortDirection)) != 0)
        return;
    column->SortDirection = (ImU8)TableGetColumnAvailSortDirection(column, 0);
    table->IsSortSpecsDirty = true;
}




IM_STATIC_ASSERT(ImGuiSortDirection_None == 0 && ImGuiSortDirection_Ascending == 1 && ImGuiSortDirection_Descending == 2);
ImGuiSortDirection ImGui::TableGetColumnNextSortDirection(ImGuiTableColumn* column)
{
    IM_ASSERT(column->SortDirectionsAvailCount > 0);
    if (column->SortOrder == -1)
        return TableGetColumnAvailSortDirection(column, 0);
    for (int n = 0; n < 3; n++)
        if (column->SortDirection == TableGetColumnAvailSortDirection(column, n))
            return TableGetColumnAvailSortDirection(column, (n + 1) % column->SortDirectionsAvailCount);
    IM_ASSERT(0);
    return ImGuiSortDirection_None;
}



void ImGui::TableSetColumnSortDirection(int column_n, ImGuiSortDirection sort_direction, bool append_to_sort_specs)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;

    if (!(table->Flags & ImGuiTableFlags_SortMulti))
        append_to_sort_specs = false;
    if (!(table->Flags & ImGuiTableFlags_SortTristate))
        IM_ASSERT(sort_direction != ImGuiSortDirection_None);

    ImGuiTableColumnIdx sort_order_max = 0;
    if (append_to_sort_specs)
        for (int other_column_n = 0; other_column_n < table->ColumnsCount; other_column_n++)
            sort_order_max = ImMax(sort_order_max, table->Columns[other_column_n].SortOrder);

    ImGuiTableColumn* column = &table->Columns[column_n];
    column->SortDirection = (ImU8)sort_direction;
    if (column->SortDirection == ImGuiSortDirection_None)
        column->SortOrder = -1;
    else if (column->SortOrder == -1 || !append_to_sort_specs)
        column->SortOrder = append_to_sort_specs ? sort_order_max + 1 : 0;

    for (int other_column_n = 0; other_column_n < table->ColumnsCount; other_column_n++)
    {
        ImGuiTableColumn* other_column = &table->Columns[other_column_n];
        if (other_column != column && !append_to_sort_specs)
            other_column->SortOrder = -1;
        TableFixColumnSortDirection(table, other_column);
    }
    table->IsSettingsDirty = true;
    table->IsSortSpecsDirty = true;
}

void ImGui::TableSortSpecsSanitize(ImGuiTable* table)
{
    IM_ASSERT(table->Flags & ImGuiTableFlags_Sortable);


    int sort_order_count = 0;
    ImU64 sort_order_mask = 0x00;
    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
    {
        ImGuiTableColumn* column = &table->Columns[column_n];
        if (column->SortOrder != -1 && !column->IsEnabled)
            column->SortOrder = -1;
        if (column->SortOrder == -1)
            continue;
        sort_order_count++;
        sort_order_mask |= ((ImU64)1 << column->SortOrder);
        IM_ASSERT(sort_order_count < (int)sizeof(sort_order_mask) * 8);
    }

    const bool need_fix_linearize = ((ImU64)1 << sort_order_count) != (sort_order_mask + 1);
    const bool need_fix_single_sort_order = (sort_order_count > 1) && !(table->Flags & ImGuiTableFlags_SortMulti);
    if (need_fix_linearize || need_fix_single_sort_order)
    {
        ImU64 fixed_mask = 0x00;
        for (int sort_n = 0; sort_n < sort_order_count; sort_n++)
        {


            int column_with_smallest_sort_order = -1;
            for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
                if ((fixed_mask & ((ImU64)1 << (ImU64)column_n)) == 0 && table->Columns[column_n].SortOrder != -1)
                    if (column_with_smallest_sort_order == -1 || table->Columns[column_n].SortOrder < table->Columns[column_with_smallest_sort_order].SortOrder)
                        column_with_smallest_sort_order = column_n;
            IM_ASSERT(column_with_smallest_sort_order != -1);
            fixed_mask |= ((ImU64)1 << column_with_smallest_sort_order);
            table->Columns[column_with_smallest_sort_order].SortOrder = (ImGuiTableColumnIdx)sort_n;


            if (need_fix_single_sort_order)
            {
                sort_order_count = 1;
                for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
                    if (column_n != column_with_smallest_sort_order)
                        table->Columns[column_n].SortOrder = -1;
                break;
            }
        }
    }


    if (sort_order_count == 0 && !(table->Flags & ImGuiTableFlags_SortTristate))
        for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
        {
            ImGuiTableColumn* column = &table->Columns[column_n];
            if (column->IsEnabled && !(column->Flags & ImGuiTableColumnFlags_NoSort))
            {
                sort_order_count = 1;
                column->SortOrder = 0;
                column->SortDirection = (ImU8)TableGetColumnAvailSortDirection(column, 0);
                break;
            }
        }

    table->SortSpecsCount = (ImGuiTableColumnIdx)sort_order_count;
}

void ImGui::TableSortSpecsBuild(ImGuiTable* table)
{
    bool dirty = table->IsSortSpecsDirty;
    if (dirty)
    {
        TableSortSpecsSanitize(table);
        table->SortSpecsMulti.resize(table->SortSpecsCount <= 1 ? 0 : table->SortSpecsCount);
        table->SortSpecs.SpecsDirty = true;
        table->IsSortSpecsDirty = false;
    }


    ImGuiTableColumnSortSpecs* sort_specs = (table->SortSpecsCount == 0) ? NULL : (table->SortSpecsCount == 1) ? &table->SortSpecsSingle : table->SortSpecsMulti.Data;
    if (dirty && sort_specs != NULL)
        for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
        {
            ImGuiTableColumn* column = &table->Columns[column_n];
            if (column->SortOrder == -1)
                continue;
            IM_ASSERT(column->SortOrder < table->SortSpecsCount);
            ImGuiTableColumnSortSpecs* sort_spec = &sort_specs[column->SortOrder];
            sort_spec->ColumnUserID = column->UserID;
            sort_spec->ColumnIndex = (ImGuiTableColumnIdx)column_n;
            sort_spec->SortOrder = (ImGuiTableColumnIdx)column->SortOrder;
            sort_spec->SortDirection = column->SortDirection;
        }

    table->SortSpecs.Specs = sort_specs;
    table->SortSpecs.SpecsCount = table->SortSpecsCount;
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------



//-------------------------------------------------------------------------

float ImGui::TableGetHeaderRowHeight()
{




    float row_height = GetTextLineHeight();
    int columns_count = TableGetColumnCount();
    for (int column_n = 0; column_n < columns_count; column_n++)
    {
        ImGuiTableColumnFlags flags = TableGetColumnFlags(column_n);
        if ((flags & ImGuiTableColumnFlags_IsEnabled) && !(flags & ImGuiTableColumnFlags_NoHeaderLabel))
            row_height = ImMax(row_height, CalcTextSize(TableGetColumnName(column_n)).y);
    }
    row_height += GetStyle().CellPadding.y * 2.0f;
    return row_height;
}







void ImGui::TableHeadersRow()
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL && "Need to call TableHeadersRow() after BeginTable()!");


    if (!table->IsLayoutLocked)
        TableUpdateLayout(table);


    const float row_y1 = GetCursorScreenPos().y;
    const float row_height = TableGetHeaderRowHeight();
    TableNextRow(ImGuiTableRowFlags_Headers, row_height);
    if (table->HostSkipItems)
        return;

    const int columns_count = TableGetColumnCount();
    for (int column_n = 0; column_n < columns_count; column_n++)
    {
        if (!TableSetColumnIndex(column_n))
            continue;



        const char* name = (TableGetColumnFlags(column_n) & ImGuiTableColumnFlags_NoHeaderLabel) ? "" : TableGetColumnName(column_n);
        PushID(column_n);
        TableHeader(name);
        PopID();
    }


    ImVec2 mouse_pos = ImGui::GetMousePos();
    if (IsMouseReleased(1) && TableGetHoveredColumn() == columns_count)
        if (mouse_pos.y >= row_y1 && mouse_pos.y < row_y1 + row_height)
            TableOpenContextMenu(-1);
}




void ImGui::TableHeader(const char* label)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    if (window->SkipItems)
        return;

    ImGuiTable* table = g.CurrentTable;
    IM_ASSERT(table != NULL && "Need to call TableHeader() after BeginTable()!");
    IM_ASSERT(table->CurrentColumn != -1);
    const int column_n = table->CurrentColumn;
    ImGuiTableColumn* column = &table->Columns[column_n];


    if (label == NULL)
        label = "";
    const char* label_end = FindRenderedTextEnd(label);
    ImVec2 label_size = CalcTextSize(label, label_end, true);
    ImVec2 label_pos = window->DC.CursorPos;



    ImRect cell_r = TableGetCellBgRect(table, column_n);
    float label_height = ImMax(label_size.y, table->RowMinHeight - table->CellPaddingY * 2.0f);


    float w_arrow = 0.0f;
    float w_sort_text = 0.0f;
    char sort_order_suf[4] = "";
    const float ARROW_SCALE = 0.65f;
    if ((table->Flags & ImGuiTableFlags_Sortable) && !(column->Flags & ImGuiTableColumnFlags_NoSort))
    {
        w_arrow = ImFloor(g.FontSize * ARROW_SCALE + g.Style.FramePadding.x);
        if (column->SortOrder > 0)
        {
            ImFormatString(sort_order_suf, IM_ARRAYSIZE(sort_order_suf), "%d", column->SortOrder + 1);
            w_sort_text = g.Style.ItemInnerSpacing.x + CalcTextSize(sort_order_suf).x;
        }
    }


    float max_pos_x = label_pos.x + label_size.x + w_sort_text + w_arrow;
    column->ContentMaxXHeadersUsed = ImMax(column->ContentMaxXHeadersUsed, column->WorkMaxX);
    column->ContentMaxXHeadersIdeal = ImMax(column->ContentMaxXHeadersIdeal, max_pos_x);


    const bool selected = (table->IsContextPopupOpen && table->ContextPopupColumn == column_n && table->InstanceInteracted == table->InstanceCurrent);
    ImGuiID id = window->GetID(label);
    ImRect bb(cell_r.Min.x, cell_r.Min.y, cell_r.Max.x, ImMax(cell_r.Max.y, cell_r.Min.y + label_height + g.Style.CellPadding.y * 2.0f));
    ItemSize(ImVec2(0.0f, label_height));
    if (!ItemAdd(bb, id))
        return;

    //GetForegroundDrawList()->AddRect(cell_r.Min, cell_r.Max, IM_COL32(255, 0, 0, 255));
    //GetForegroundDrawList()->AddRect(bb.Min, bb.Max, IM_COL32(255, 0, 0, 255));


    bool hovered, held;
    bool pressed = ButtonBehavior(bb, id, &hovered, &held, ImGuiButtonFlags_AllowOverlap);
    if (held || hovered || selected)
    {
        const ImU32 col = GetColorU32(held ? ImGuiCol_HeaderActive : hovered ? ImGuiCol_HeaderHovered : ImGuiCol_Header);
        //RenderFrame(bb.Min, bb.Max, col, false, 0.0f);
        TableSetBgColor(ImGuiTableBgTarget_CellBg, col, table->CurrentColumn);
    }
    else
    {

        if ((table->RowFlags & ImGuiTableRowFlags_Headers) == 0)
            TableSetBgColor(ImGuiTableBgTarget_CellBg, GetColorU32(ImGuiCol_TableHeaderBg), table->CurrentColumn);
    }
    RenderNavHighlight(bb, id, ImGuiNavHighlightFlags_TypeThin | ImGuiNavHighlightFlags_NoRounding);
    if (held)
        table->HeldHeaderColumn = (ImGuiTableColumnIdx)column_n;
    window->DC.CursorPos.y -= g.Style.ItemSpacing.y * 0.5f;



    if (held && (table->Flags & ImGuiTableFlags_Reorderable) && IsMouseDragging(0) && !g.DragDropActive)
    {

        table->ReorderColumn = (ImGuiTableColumnIdx)column_n;
        table->InstanceInteracted = table->InstanceCurrent;


        if (g.IO.MouseDelta.x < 0.0f && g.IO.MousePos.x < cell_r.Min.x)
            if (ImGuiTableColumn* prev_column = (column->PrevEnabledColumn != -1) ? &table->Columns[column->PrevEnabledColumn] : NULL)
                if (!((column->Flags | prev_column->Flags) & ImGuiTableColumnFlags_NoReorder))
                    if ((column->IndexWithinEnabledSet < table->FreezeColumnsRequest) == (prev_column->IndexWithinEnabledSet < table->FreezeColumnsRequest))
                        table->ReorderColumnDir = -1;
        if (g.IO.MouseDelta.x > 0.0f && g.IO.MousePos.x > cell_r.Max.x)
            if (ImGuiTableColumn* next_column = (column->NextEnabledColumn != -1) ? &table->Columns[column->NextEnabledColumn] : NULL)
                if (!((column->Flags | next_column->Flags) & ImGuiTableColumnFlags_NoReorder))
                    if ((column->IndexWithinEnabledSet < table->FreezeColumnsRequest) == (next_column->IndexWithinEnabledSet < table->FreezeColumnsRequest))
                        table->ReorderColumnDir = +1;
    }


    const float ellipsis_max = ImMax(cell_r.Max.x - w_arrow - w_sort_text, label_pos.x);
    if ((table->Flags & ImGuiTableFlags_Sortable) && !(column->Flags & ImGuiTableColumnFlags_NoSort))
    {
        if (column->SortOrder != -1)
        {
            float x = ImMax(cell_r.Min.x, cell_r.Max.x - w_arrow - w_sort_text);
            float y = label_pos.y;
            if (column->SortOrder > 0)
            {
                PushStyleColor(ImGuiCol_Text, GetColorU32(ImGuiCol_Text, 0.70f));
                RenderText(ImVec2(x + g.Style.ItemInnerSpacing.x, y), sort_order_suf);
                PopStyleColor();
                x += w_sort_text;
            }
            RenderArrow(window->DrawList, ImVec2(x, y), GetColorU32(ImGuiCol_Text), column->SortDirection == ImGuiSortDirection_Ascending ? ImGuiDir_Up : ImGuiDir_Down, ARROW_SCALE);
        }


        if (pressed && table->ReorderColumn != column_n)
        {
            ImGuiSortDirection sort_direction = TableGetColumnNextSortDirection(column);
            TableSetColumnSortDirection(column_n, sort_direction, g.IO.KeyShift);
        }
    }



    //window->DrawList->AddCircleFilled(ImVec2(ellipsis_max, label_pos.y), 40, IM_COL32_WHITE);
    RenderTextEllipsis(window->DrawList, label_pos, ImVec2(ellipsis_max, label_pos.y + label_height + g.Style.FramePadding.y), ellipsis_max, ellipsis_max, label, label_end, &label_size);

    const bool text_clipped = label_size.x > (ellipsis_max - label_pos.x);
    if (text_clipped && hovered && g.ActiveId == 0)
        SetItemTooltip("%.*s", (int)(label_end - label), label);


    if (IsMouseReleased(1) && IsItemHovered())
        TableOpenContextMenu(column_n);
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------


//-------------------------------------------------------------------------


void ImGui::TableOpenContextMenu(int column_n)
{
    ImGuiContext& g = *GImGui;
    ImGuiTable* table = g.CurrentTable;
    if (column_n == -1 && table->CurrentColumn != -1)
        column_n = table->CurrentColumn;
    if (column_n == table->ColumnsCount)
        column_n = -1;
    IM_ASSERT(column_n >= -1 && column_n < table->ColumnsCount);
    if (table->Flags & (ImGuiTableFlags_Resizable | ImGuiTableFlags_Reorderable | ImGuiTableFlags_Hideable))
    {
        table->IsContextPopupOpen = true;
        table->ContextPopupColumn = (ImGuiTableColumnIdx)column_n;
        table->InstanceInteracted = table->InstanceCurrent;
        const ImGuiID context_menu_id = ImHashStr("##ContextMenu", 0, table->ID);
        OpenPopupEx(context_menu_id, ImGuiPopupFlags_None);
    }
}

bool ImGui::TableBeginContextMenuPopup(ImGuiTable* table)
{
    if (!table->IsContextPopupOpen || table->InstanceCurrent != table->InstanceInteracted)
        return false;
    const ImGuiID context_menu_id = ImHashStr("##ContextMenu", 0, table->ID);
    if (BeginPopupEx(context_menu_id, ImGuiWindowFlags_AlwaysAutoResize | ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoSavedSettings))
        return true;
    table->IsContextPopupOpen = false;
    return false;
}



void ImGui::TableDrawContextMenu(ImGuiTable* table)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    if (window->SkipItems)
        return;

    bool want_separator = false;
    const int column_n = (table->ContextPopupColumn >= 0 && table->ContextPopupColumn < table->ColumnsCount) ? table->ContextPopupColumn : -1;
    ImGuiTableColumn* column = (column_n != -1) ? &table->Columns[column_n] : NULL;


    if (table->Flags & ImGuiTableFlags_Resizable)
    {
        if (column != NULL)
        {
            const bool can_resize = !(column->Flags & ImGuiTableColumnFlags_NoResize) && column->IsEnabled;
            if (MenuItem(LocalizeGetMsg(ImGuiLocKey_TableSizeOne), NULL, false, can_resize))
                TableSetColumnWidthAutoSingle(table, column_n);
        }

        const char* size_all_desc;
        if (table->ColumnsEnabledFixedCount == table->ColumnsEnabledCount && (table->Flags & ImGuiTableFlags_SizingMask_) != ImGuiTableFlags_SizingFixedSame)
            size_all_desc = LocalizeGetMsg(ImGuiLocKey_TableSizeAllFit);
        else
            size_all_desc = LocalizeGetMsg(ImGuiLocKey_TableSizeAllDefault);
        if (MenuItem(size_all_desc, NULL))
            TableSetColumnWidthAutoAll(table);
        want_separator = true;
    }


    if (table->Flags & ImGuiTableFlags_Reorderable)
    {
        if (MenuItem(LocalizeGetMsg(ImGuiLocKey_TableResetOrder), NULL, false, !table->IsDefaultDisplayOrder))
            table->IsResetDisplayOrderRequest = true;
        want_separator = true;
    }


    //if (MenuItem("Reset all"))




#if 0
    if ((table->Flags & ImGuiTableFlags_Sortable) && column != NULL && (column->Flags & ImGuiTableColumnFlags_NoSort) == 0)
    {
        if (want_separator)
            Separator();
        want_separator = true;

        bool append_to_sort_specs = g.IO.KeyShift;
        if (MenuItem("Sort in Ascending Order", NULL, column->SortOrder != -1 && column->SortDirection == ImGuiSortDirection_Ascending, (column->Flags & ImGuiTableColumnFlags_NoSortAscending) == 0))
            TableSetColumnSortDirection(table, column_n, ImGuiSortDirection_Ascending, append_to_sort_specs);
        if (MenuItem("Sort in Descending Order", NULL, column->SortOrder != -1 && column->SortDirection == ImGuiSortDirection_Descending, (column->Flags & ImGuiTableColumnFlags_NoSortDescending) == 0))
            TableSetColumnSortDirection(table, column_n, ImGuiSortDirection_Descending, append_to_sort_specs);
    }
#endif


    if (table->Flags & ImGuiTableFlags_Hideable)
    {
        if (want_separator)
            Separator();
        want_separator = true;

        PushItemFlag(ImGuiItemFlags_SelectableDontClosePopup, true);
        for (int other_column_n = 0; other_column_n < table->ColumnsCount; other_column_n++)
        {
            ImGuiTableColumn* other_column = &table->Columns[other_column_n];
            if (other_column->Flags & ImGuiTableColumnFlags_Disabled)
                continue;

            const char* name = TableGetColumnName(table, other_column_n);
            if (name == NULL || name[0] == 0)
                name = "<Unknown>";


            bool menu_item_active = (other_column->Flags & ImGuiTableColumnFlags_NoHide) ? false : true;
            if (other_column->IsUserEnabled && table->ColumnsEnabledCount <= 1)
                menu_item_active = false;
            if (MenuItem(name, NULL, other_column->IsUserEnabled, menu_item_active))
                other_column->IsUserEnabledNextFrame = !other_column->IsUserEnabled;
        }
        PopItemFlag();
    }
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------














//-------------------------------------------------------------------------




//-------------------------------------------------------------------------


static void TableSettingsInit(ImGuiTableSettings* settings, ImGuiID id, int columns_count, int columns_count_max)
{
    IM_PLACEMENT_NEW(settings) ImGuiTableSettings();
    ImGuiTableColumnSettings* settings_column = settings->GetColumnSettings();
    for (int n = 0; n < columns_count_max; n++, settings_column++)
        IM_PLACEMENT_NEW(settings_column) ImGuiTableColumnSettings();
    settings->ID = id;
    settings->ColumnsCount = (ImGuiTableColumnIdx)columns_count;
    settings->ColumnsCountMax = (ImGuiTableColumnIdx)columns_count_max;
    settings->WantApply = true;
}

static size_t TableSettingsCalcChunkSize(int columns_count)
{
    return sizeof(ImGuiTableSettings) + (size_t)columns_count * sizeof(ImGuiTableColumnSettings);
}

ImGuiTableSettings* ImGui::TableSettingsCreate(ImGuiID id, int columns_count)
{
    ImGuiContext& g = *GImGui;
    ImGuiTableSettings* settings = g.SettingsTables.alloc_chunk(TableSettingsCalcChunkSize(columns_count));
    TableSettingsInit(settings, id, columns_count, columns_count);
    return settings;
}


ImGuiTableSettings* ImGui::TableSettingsFindByID(ImGuiID id)
{

    ImGuiContext& g = *GImGui;
    for (ImGuiTableSettings* settings = g.SettingsTables.begin(); settings != NULL; settings = g.SettingsTables.next_chunk(settings))
        if (settings->ID == id)
            return settings;
    return NULL;
}


ImGuiTableSettings* ImGui::TableGetBoundSettings(ImGuiTable* table)
{
    if (table->SettingsOffset != -1)
    {
        ImGuiContext& g = *GImGui;
        ImGuiTableSettings* settings = g.SettingsTables.ptr_from_offset(table->SettingsOffset);
        IM_ASSERT(settings->ID == table->ID);
        if (settings->ColumnsCountMax >= table->ColumnsCount)
            return settings;
        settings->ID = 0;
    }
    return NULL;
}


void ImGui::TableResetSettings(ImGuiTable* table)
{
    table->IsInitializing = table->IsSettingsDirty = true;
    table->IsResetAllRequest = false;
    table->IsSettingsRequestLoad = false;
    table->SettingsLoadedFlags = ImGuiTableFlags_None;
}

void ImGui::TableSaveSettings(ImGuiTable* table)
{
    table->IsSettingsDirty = false;
    if (table->Flags & ImGuiTableFlags_NoSavedSettings)
        return;


    ImGuiContext& g = *GImGui;
    ImGuiTableSettings* settings = TableGetBoundSettings(table);
    if (settings == NULL)
    {
        settings = TableSettingsCreate(table->ID, table->ColumnsCount);
        table->SettingsOffset = g.SettingsTables.offset_from_ptr(settings);
    }
    settings->ColumnsCount = (ImGuiTableColumnIdx)table->ColumnsCount;


    IM_ASSERT(settings->ID == table->ID);
    IM_ASSERT(settings->ColumnsCount == table->ColumnsCount && settings->ColumnsCountMax >= settings->ColumnsCount);
    ImGuiTableColumn* column = table->Columns.Data;
    ImGuiTableColumnSettings* column_settings = settings->GetColumnSettings();

    bool save_ref_scale = false;
    settings->SaveFlags = ImGuiTableFlags_None;
    for (int n = 0; n < table->ColumnsCount; n++, column++, column_settings++)
    {
        const float width_or_weight = (column->Flags & ImGuiTableColumnFlags_WidthStretch) ? column->StretchWeight : column->WidthRequest;
        column_settings->WidthOrWeight = width_or_weight;
        column_settings->Index = (ImGuiTableColumnIdx)n;
        column_settings->DisplayOrder = column->DisplayOrder;
        column_settings->SortOrder = column->SortOrder;
        column_settings->SortDirection = column->SortDirection;
        column_settings->IsEnabled = column->IsUserEnabled;
        column_settings->IsStretch = (column->Flags & ImGuiTableColumnFlags_WidthStretch) ? 1 : 0;
        if ((column->Flags & ImGuiTableColumnFlags_WidthStretch) == 0)
            save_ref_scale = true;




        if (width_or_weight != column->InitStretchWeightOrWidth)
            settings->SaveFlags |= ImGuiTableFlags_Resizable;
        if (column->DisplayOrder != n)
            settings->SaveFlags |= ImGuiTableFlags_Reorderable;
        if (column->SortOrder != -1)
            settings->SaveFlags |= ImGuiTableFlags_Sortable;
        if (column->IsUserEnabled != ((column->Flags & ImGuiTableColumnFlags_DefaultHide) == 0))
            settings->SaveFlags |= ImGuiTableFlags_Hideable;
    }
    settings->SaveFlags &= table->Flags;
    settings->RefScale = save_ref_scale ? table->RefScale : 0.0f;

    MarkIniSettingsDirty();
}

void ImGui::TableLoadSettings(ImGuiTable* table)
{
    ImGuiContext& g = *GImGui;
    table->IsSettingsRequestLoad = false;
    if (table->Flags & ImGuiTableFlags_NoSavedSettings)
        return;


    ImGuiTableSettings* settings;
    if (table->SettingsOffset == -1)
    {
        settings = TableSettingsFindByID(table->ID);
        if (settings == NULL)
            return;
        if (settings->ColumnsCount != table->ColumnsCount)
            table->IsSettingsDirty = true;
        table->SettingsOffset = g.SettingsTables.offset_from_ptr(settings);
    }
    else
    {
        settings = TableGetBoundSettings(table);
    }

    table->SettingsLoadedFlags = settings->SaveFlags;
    table->RefScale = settings->RefScale;


    ImGuiTableColumnSettings* column_settings = settings->GetColumnSettings();
    ImU64 display_order_mask = 0;
    for (int data_n = 0; data_n < settings->ColumnsCount; data_n++, column_settings++)
    {
        int column_n = column_settings->Index;
        if (column_n < 0 || column_n >= table->ColumnsCount)
            continue;

        ImGuiTableColumn* column = &table->Columns[column_n];
        if (settings->SaveFlags & ImGuiTableFlags_Resizable)
        {
            if (column_settings->IsStretch)
                column->StretchWeight = column_settings->WidthOrWeight;
            else
                column->WidthRequest = column_settings->WidthOrWeight;
            column->AutoFitQueue = 0x00;
        }
        if (settings->SaveFlags & ImGuiTableFlags_Reorderable)
            column->DisplayOrder = column_settings->DisplayOrder;
        else
            column->DisplayOrder = (ImGuiTableColumnIdx)column_n;
        display_order_mask |= (ImU64)1 << column->DisplayOrder;
        column->IsUserEnabled = column->IsUserEnabledNextFrame = column_settings->IsEnabled;
        column->SortOrder = column_settings->SortOrder;
        column->SortDirection = column_settings->SortDirection;
    }


    const ImU64 expected_display_order_mask = (settings->ColumnsCount == 64) ? ~0 : ((ImU64)1 << settings->ColumnsCount) - 1;
    if (display_order_mask != expected_display_order_mask)
        for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
            table->Columns[column_n].DisplayOrder = (ImGuiTableColumnIdx)column_n;


    for (int column_n = 0; column_n < table->ColumnsCount; column_n++)
        table->DisplayOrderToIndex[table->Columns[column_n].DisplayOrder] = (ImGuiTableColumnIdx)column_n;
}

static void TableSettingsHandler_ClearAll(ImGuiContext* ctx, ImGuiSettingsHandler*)
{
    ImGuiContext& g = *ctx;
    for (int i = 0; i != g.Tables.GetMapSize(); i++)
        if (ImGuiTable* table = g.Tables.TryGetMapData(i))
            table->SettingsOffset = -1;
    g.SettingsTables.clear();
}


static void TableSettingsHandler_ApplyAll(ImGuiContext* ctx, ImGuiSettingsHandler*)
{
    ImGuiContext& g = *ctx;
    for (int i = 0; i != g.Tables.GetMapSize(); i++)
        if (ImGuiTable* table = g.Tables.TryGetMapData(i))
        {
            table->IsSettingsRequestLoad = true;
            table->SettingsOffset = -1;
        }
}

static void* TableSettingsHandler_ReadOpen(ImGuiContext*, ImGuiSettingsHandler*, const char* name)
{
    ImGuiID id = 0;
    int columns_count = 0;
    if (sscanf(name, "0x%08X,%d", &id, &columns_count) < 2)
        return NULL;

    if (ImGuiTableSettings* settings = ImGui::TableSettingsFindByID(id))
    {
        if (settings->ColumnsCountMax >= columns_count)
        {
            TableSettingsInit(settings, id, columns_count, settings->ColumnsCountMax);
            return settings;
        }
        settings->ID = 0;
    }
    return ImGui::TableSettingsCreate(id, columns_count);
}

static void TableSettingsHandler_ReadLine(ImGuiContext*, ImGuiSettingsHandler*, void* entry, const char* line)
{

    ImGuiTableSettings* settings = (ImGuiTableSettings*)entry;
    float f = 0.0f;
    int column_n = 0, r = 0, n = 0;

    if (sscanf(line, "RefScale=%f", &f) == 1) { settings->RefScale = f; return; }

    if (sscanf(line, "Column %d%n", &column_n, &r) == 1)
    {
        if (column_n < 0 || column_n >= settings->ColumnsCount)
            return;
        line = ImStrSkipBlank(line + r);
        char c = 0;
        ImGuiTableColumnSettings* column = settings->GetColumnSettings() + column_n;
        column->Index = (ImGuiTableColumnIdx)column_n;
        if (sscanf(line, "UserID=0x%08X%n", (ImU32*)&n, &r)==1) { line = ImStrSkipBlank(line + r); column->UserID = (ImGuiID)n; }
        if (sscanf(line, "Width=%d%n", &n, &r) == 1)            { line = ImStrSkipBlank(line + r); column->WidthOrWeight = (float)n; column->IsStretch = 0; settings->SaveFlags |= ImGuiTableFlags_Resizable; }
        if (sscanf(line, "Weight=%f%n", &f, &r) == 1)           { line = ImStrSkipBlank(line + r); column->WidthOrWeight = f; column->IsStretch = 1; settings->SaveFlags |= ImGuiTableFlags_Resizable; }
        if (sscanf(line, "Visible=%d%n", &n, &r) == 1)          { line = ImStrSkipBlank(line + r); column->IsEnabled = (ImU8)n; settings->SaveFlags |= ImGuiTableFlags_Hideable; }
        if (sscanf(line, "Order=%d%n", &n, &r) == 1)            { line = ImStrSkipBlank(line + r); column->DisplayOrder = (ImGuiTableColumnIdx)n; settings->SaveFlags |= ImGuiTableFlags_Reorderable; }
        if (sscanf(line, "Sort=%d%c%n", &n, &c, &r) == 2)       { line = ImStrSkipBlank(line + r); column->SortOrder = (ImGuiTableColumnIdx)n; column->SortDirection = (c == '^') ? ImGuiSortDirection_Descending : ImGuiSortDirection_Ascending; settings->SaveFlags |= ImGuiTableFlags_Sortable; }
    }
}

static void TableSettingsHandler_WriteAll(ImGuiContext* ctx, ImGuiSettingsHandler* handler, ImGuiTextBuffer* buf)
{
    ImGuiContext& g = *ctx;
    for (ImGuiTableSettings* settings = g.SettingsTables.begin(); settings != NULL; settings = g.SettingsTables.next_chunk(settings))
    {
        if (settings->ID == 0)
            continue;



        const bool save_size    = (settings->SaveFlags & ImGuiTableFlags_Resizable) != 0;
        const bool save_visible = (settings->SaveFlags & ImGuiTableFlags_Hideable) != 0;
        const bool save_order   = (settings->SaveFlags & ImGuiTableFlags_Reorderable) != 0;
        const bool save_sort    = (settings->SaveFlags & ImGuiTableFlags_Sortable) != 0;
        if (!save_size && !save_visible && !save_order && !save_sort)
            continue;

        buf->reserve(buf->size() + 30 + settings->ColumnsCount * 50);
        buf->appendf("[%s][0x%08X,%d]\n", handler->TypeName, settings->ID, settings->ColumnsCount);
        if (settings->RefScale != 0.0f)
            buf->appendf("RefScale=%g\n", settings->RefScale);
        ImGuiTableColumnSettings* column = settings->GetColumnSettings();
        for (int column_n = 0; column_n < settings->ColumnsCount; column_n++, column++)
        {

            bool save_column = column->UserID != 0 || save_size || save_visible || save_order || (save_sort && column->SortOrder != -1);
            if (!save_column)
                continue;
            buf->appendf("Column %-2d", column_n);
            if (column->UserID != 0)                    { buf->appendf(" UserID=%08X", column->UserID); }
            if (save_size && column->IsStretch)         { buf->appendf(" Weight=%.4f", column->WidthOrWeight); }
            if (save_size && !column->IsStretch)        { buf->appendf(" Width=%d", (int)column->WidthOrWeight); }
            if (save_visible)                           { buf->appendf(" Visible=%d", column->IsEnabled); }
            if (save_order)                             { buf->appendf(" Order=%d", column->DisplayOrder); }
            if (save_sort && column->SortOrder != -1)   { buf->appendf(" Sort=%d%c", column->SortOrder, (column->SortDirection == ImGuiSortDirection_Ascending) ? 'v' : '^'); }
            buf->append("\n");
        }
        buf->append("\n");
    }
}

void ImGui::TableSettingsAddSettingsHandler()
{
    ImGuiSettingsHandler ini_handler;
    ini_handler.TypeName = "Table";
    ini_handler.TypeHash = ImHashStr("Table");
    ini_handler.ClearAllFn = TableSettingsHandler_ClearAll;
    ini_handler.ReadOpenFn = TableSettingsHandler_ReadOpen;
    ini_handler.ReadLineFn = TableSettingsHandler_ReadLine;
    ini_handler.ApplyAllFn = TableSettingsHandler_ApplyAll;
    ini_handler.WriteAllFn = TableSettingsHandler_WriteAll;
    AddSettingsHandler(&ini_handler);
}

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------



//-------------------------------------------------------------------------


void ImGui::TableRemove(ImGuiTable* table)
{
    //IMGUI_DEBUG_PRINT("TableRemove() id=0x%08X\n", table->ID);
    ImGuiContext& g = *GImGui;
    int table_idx = g.Tables.GetIndex(table);
    //memset(table->RawData.Data, 0, table->RawData.size_in_bytes());
    //memset(table, 0, sizeof(ImGuiTable));
    g.Tables.Remove(table->ID, table);
    g.TablesLastTimeActive[table_idx] = -1.0f;
}


void ImGui::TableGcCompactTransientBuffers(ImGuiTable* table)
{
    //IMGUI_DEBUG_PRINT("TableGcCompactTransientBuffers() id=0x%08X\n", table->ID);
    ImGuiContext& g = *GImGui;
    IM_ASSERT(table->MemoryCompacted == false);
    table->SortSpecs.Specs = NULL;
    table->SortSpecsMulti.clear();
    table->IsSortSpecsDirty = true;
    table->ColumnsNames.clear();
    table->MemoryCompacted = true;
    for (int n = 0; n < table->ColumnsCount; n++)
        table->Columns[n].NameOffset = -1;
    g.TablesLastTimeActive[g.Tables.GetIndex(table)] = -1.0f;
}

void ImGui::TableGcCompactTransientBuffers(ImGuiTableTempData* temp_data)
{
    temp_data->DrawSplitter.ClearFreeMemory();
    temp_data->LastTimeActive = -1.0f;
}


void ImGui::TableGcCompactSettings()
{
    ImGuiContext& g = *GImGui;
    int required_memory = 0;
    for (ImGuiTableSettings* settings = g.SettingsTables.begin(); settings != NULL; settings = g.SettingsTables.next_chunk(settings))
        if (settings->ID != 0)
            required_memory += (int)TableSettingsCalcChunkSize(settings->ColumnsCount);
    if (required_memory == g.SettingsTables.Buf.Size)
        return;
    ImChunkStream<ImGuiTableSettings> new_chunk_stream;
    new_chunk_stream.Buf.reserve(required_memory);
    for (ImGuiTableSettings* settings = g.SettingsTables.begin(); settings != NULL; settings = g.SettingsTables.next_chunk(settings))
        if (settings->ID != 0)
            memcpy(new_chunk_stream.alloc_chunk(TableSettingsCalcChunkSize(settings->ColumnsCount)), settings, TableSettingsCalcChunkSize(settings->ColumnsCount));
    g.SettingsTables.swap(new_chunk_stream);
}


//-------------------------------------------------------------------------

//-------------------------------------------------------------------------

//-------------------------------------------------------------------------

#ifndef IMGUI_DISABLE_DEBUG_TOOLS

static const char* DebugNodeTableGetSizingPolicyDesc(ImGuiTableFlags sizing_policy)
{
    sizing_policy &= ImGuiTableFlags_SizingMask_;
    if (sizing_policy == ImGuiTableFlags_SizingFixedFit)    { return "FixedFit"; }
    if (sizing_policy == ImGuiTableFlags_SizingFixedSame)   { return "FixedSame"; }
    if (sizing_policy == ImGuiTableFlags_SizingStretchProp) { return "StretchProp"; }
    if (sizing_policy == ImGuiTableFlags_SizingStretchSame) { return "StretchSame"; }
    return "N/A";
}

void ImGui::DebugNodeTable(ImGuiTable* table)
{
    const bool is_active = (table->LastFrameActive >= GetFrameCount() - 2);
    if (!is_active) { PushStyleColor(ImGuiCol_Text, GetStyleColorVec4(ImGuiCol_TextDisabled)); }
    bool open = TreeNode(table, "Table 0x%08X (%d columns, in '%s')%s", table->ID, table->ColumnsCount, table->OuterWindow->Name, is_active ? "" : " *Inactive*");
    if (!is_active) { PopStyleColor(); }
    if (IsItemHovered())
        GetForegroundDrawList()->AddRect(table->OuterRect.Min, table->OuterRect.Max, IM_COL32(255, 255, 0, 255));
    if (IsItemVisible() && table->HoveredColumnBody != -1)
        GetForegroundDrawList()->AddRect(GetItemRectMin(), GetItemRectMax(), IM_COL32(255, 255, 0, 255));
    if (!open)
        return;
    if (table->InstanceCurrent > 0)
        Text("** %d instances of same table! Some data below will refer to last instance.", table->InstanceCurrent + 1);
    bool clear_settings = SmallButton("Clear settings");
    BulletText("OuterRect: Pos: (%.1f,%.1f) Size: (%.1f,%.1f) Sizing: '%s'", table->OuterRect.Min.x, table->OuterRect.Min.y, table->OuterRect.GetWidth(), table->OuterRect.GetHeight(), DebugNodeTableGetSizingPolicyDesc(table->Flags));
    BulletText("ColumnsGivenWidth: %.1f, ColumnsAutoFitWidth: %.1f, InnerWidth: %.1f%s", table->ColumnsGivenWidth, table->ColumnsAutoFitWidth, table->InnerWidth, table->InnerWidth == 0.0f ? " (auto)" : "");
    BulletText("CellPaddingX: %.1f, CellSpacingX: %.1f/%.1f, OuterPaddingX: %.1f", table->CellPaddingX, table->CellSpacingX1, table->CellSpacingX2, table->OuterPaddingX);
    BulletText("HoveredColumnBody: %d, HoveredColumnBorder: %d", table->HoveredColumnBody, table->HoveredColumnBorder);
    BulletText("ResizedColumn: %d, ReorderColumn: %d, HeldHeaderColumn: %d", table->ResizedColumn, table->ReorderColumn, table->HeldHeaderColumn);
    for (int n = 0; n < table->InstanceCurrent + 1; n++)
    {
        ImGuiTableInstanceData* table_instance = TableGetInstanceData(table, n);
        BulletText("Instance %d: HoveredRow: %d, LastOuterHeight: %.2f", n, table_instance->HoveredRowLast, table_instance->LastOuterHeight);
    }
    //BulletText("BgDrawChannels: %d/%d", 0, table->BgDrawChannelUnfrozen);
    float sum_weights = 0.0f;
    for (int n = 0; n < table->ColumnsCount; n++)
        if (table->Columns[n].Flags & ImGuiTableColumnFlags_WidthStretch)
            sum_weights += table->Columns[n].StretchWeight;
    for (int n = 0; n < table->ColumnsCount; n++)
    {
        ImGuiTableColumn* column = &table->Columns[n];
        const char* name = TableGetColumnName(table, n);
        char buf[512];
        ImFormatString(buf, IM_ARRAYSIZE(buf),
            "Column %d order %d '%s': offset %+.2f to %+.2f%s\n"
            "Enabled: %d, VisibleX/Y: %d/%d, RequestOutput: %d, SkipItems: %d, DrawChannels: %d,%d\n"
            "WidthGiven: %.1f, Request/Auto: %.1f/%.1f, StretchWeight: %.3f (%.1f%%)\n"
            "MinX: %.1f, MaxX: %.1f (%+.1f), ClipRect: %.1f to %.1f (+%.1f)\n"
            "ContentWidth: %.1f,%.1f, HeadersUsed/Ideal %.1f/%.1f\n"
            "Sort: %d%s, UserID: 0x%08X, Flags: 0x%04X: %s%s%s..",
            n, column->DisplayOrder, name, column->MinX - table->WorkRect.Min.x, column->MaxX - table->WorkRect.Min.x, (n < table->FreezeColumnsRequest) ? " (Frozen)" : "",
            column->IsEnabled, column->IsVisibleX, column->IsVisibleY, column->IsRequestOutput, column->IsSkipItems, column->DrawChannelFrozen, column->DrawChannelUnfrozen,
            column->WidthGiven, column->WidthRequest, column->WidthAuto, column->StretchWeight, column->StretchWeight > 0.0f ? (column->StretchWeight / sum_weights) * 100.0f : 0.0f,
            column->MinX, column->MaxX, column->MaxX - column->MinX, column->ClipRect.Min.x, column->ClipRect.Max.x, column->ClipRect.Max.x - column->ClipRect.Min.x,
            column->ContentMaxXFrozen - column->WorkMinX, column->ContentMaxXUnfrozen - column->WorkMinX, column->ContentMaxXHeadersUsed - column->WorkMinX, column->ContentMaxXHeadersIdeal - column->WorkMinX,
            column->SortOrder, (column->SortDirection == ImGuiSortDirection_Ascending) ? " (Asc)" : (column->SortDirection == ImGuiSortDirection_Descending) ? " (Des)" : "", column->UserID, column->Flags,
            (column->Flags & ImGuiTableColumnFlags_WidthStretch) ? "WidthStretch " : "",
            (column->Flags & ImGuiTableColumnFlags_WidthFixed) ? "WidthFixed " : "",
            (column->Flags & ImGuiTableColumnFlags_NoResize) ? "NoResize " : "");
        Bullet();
        Selectable(buf);
        if (IsItemHovered())
        {
            ImRect r(column->MinX, table->OuterRect.Min.y, column->MaxX, table->OuterRect.Max.y);
            GetForegroundDrawList()->AddRect(r.Min, r.Max, IM_COL32(255, 255, 0, 255));
        }
    }
    if (ImGuiTableSettings* settings = TableGetBoundSettings(table))
        DebugNodeTableSettings(settings);
    if (clear_settings)
        table->IsResetAllRequest = true;
    TreePop();
}

void ImGui::DebugNodeTableSettings(ImGuiTableSettings* settings)
{
    if (!TreeNode((void*)(intptr_t)settings->ID, "Settings 0x%08X (%d columns)", settings->ID, settings->ColumnsCount))
        return;
    BulletText("SaveFlags: 0x%08X", settings->SaveFlags);
    BulletText("ColumnsCount: %d (max %d)", settings->ColumnsCount, settings->ColumnsCountMax);
    for (int n = 0; n < settings->ColumnsCount; n++)
    {
        ImGuiTableColumnSettings* column_settings = &settings->GetColumnSettings()[n];
        ImGuiSortDirection sort_dir = (column_settings->SortOrder != -1) ? (ImGuiSortDirection)column_settings->SortDirection : ImGuiSortDirection_None;
        BulletText("Column %d Order %d SortOrder %d %s Vis %d %s %7.3f UserID 0x%08X",
            n, column_settings->DisplayOrder, column_settings->SortOrder,
            (sort_dir == ImGuiSortDirection_Ascending) ? "Asc" : (sort_dir == ImGuiSortDirection_Descending) ? "Des" : "---",
            column_settings->IsEnabled, column_settings->IsStretch ? "Weight" : "Width ", column_settings->WidthOrWeight, column_settings->UserID);
    }
    TreePop();
}

#else

void ImGui::DebugNodeTable(ImGuiTable*) {}
void ImGui::DebugNodeTableSettings(ImGuiTableSettings*) {}

#endif


//-------------------------------------------------------------------------


//-------------------------------------------------------------------------

//-------------------------------------------------------------------------
















//-------------------------------------------------------------------------





void ImGui::SetWindowClipRectBeforeSetChannel(ImGuiWindow* window, const ImRect& clip_rect)
{
    ImVec4 clip_rect_vec4 = clip_rect.ToVec4();
    window->ClipRect = clip_rect;
    window->DrawList->_CmdHeader.ClipRect = clip_rect_vec4;
    window->DrawList->_ClipRectStack.Data[window->DrawList->_ClipRectStack.Size - 1] = clip_rect_vec4;
}

int ImGui::GetColumnIndex()
{
    ImGuiWindow* window = GetCurrentWindowRead();
    return window->DC.CurrentColumns ? window->DC.CurrentColumns->Current : 0;
}

int ImGui::GetColumnsCount()
{
    ImGuiWindow* window = GetCurrentWindowRead();
    return window->DC.CurrentColumns ? window->DC.CurrentColumns->Count : 1;
}

float ImGui::GetColumnOffsetFromNorm(const ImGuiOldColumns* columns, float offset_norm)
{
    return offset_norm * (columns->OffMaxX - columns->OffMinX);
}

float ImGui::GetColumnNormFromOffset(const ImGuiOldColumns* columns, float offset)
{
    return offset / (columns->OffMaxX - columns->OffMinX);
}

static const float COLUMNS_HIT_RECT_HALF_WIDTH = 4.0f;

static float GetDraggedColumnOffset(ImGuiOldColumns* columns, int column_index)
{


    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    IM_ASSERT(column_index > 0);
    IM_ASSERT(g.ActiveId == columns->ID + ImGuiID(column_index));

    float x = g.IO.MousePos.x - g.ActiveIdClickOffset.x + COLUMNS_HIT_RECT_HALF_WIDTH - window->Pos.x;
    x = ImMax(x, ImGui::GetColumnOffset(column_index - 1) + g.Style.ColumnsMinSpacing);
    if ((columns->Flags & ImGuiOldColumnFlags_NoPreserveWidths))
        x = ImMin(x, ImGui::GetColumnOffset(column_index + 1) - g.Style.ColumnsMinSpacing);

    return x;
}

float ImGui::GetColumnOffset(int column_index)
{
    ImGuiWindow* window = GetCurrentWindowRead();
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    if (columns == NULL)
        return 0.0f;

    if (column_index < 0)
        column_index = columns->Current;
    IM_ASSERT(column_index < columns->Columns.Size);

    const float t = columns->Columns[column_index].OffsetNorm;
    const float x_offset = ImLerp(columns->OffMinX, columns->OffMaxX, t);
    return x_offset;
}

static float GetColumnWidthEx(ImGuiOldColumns* columns, int column_index, bool before_resize = false)
{
    if (column_index < 0)
        column_index = columns->Current;

    float offset_norm;
    if (before_resize)
        offset_norm = columns->Columns[column_index + 1].OffsetNormBeforeResize - columns->Columns[column_index].OffsetNormBeforeResize;
    else
        offset_norm = columns->Columns[column_index + 1].OffsetNorm - columns->Columns[column_index].OffsetNorm;
    return ImGui::GetColumnOffsetFromNorm(columns, offset_norm);
}

float ImGui::GetColumnWidth(int column_index)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    if (columns == NULL)
        return GetContentRegionAvail().x;

    if (column_index < 0)
        column_index = columns->Current;
    return GetColumnOffsetFromNorm(columns, columns->Columns[column_index + 1].OffsetNorm - columns->Columns[column_index].OffsetNorm);
}

void ImGui::SetColumnOffset(int column_index, float offset)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = g.CurrentWindow;
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    IM_ASSERT(columns != NULL);

    if (column_index < 0)
        column_index = columns->Current;
    IM_ASSERT(column_index < columns->Columns.Size);

    const bool preserve_width = !(columns->Flags & ImGuiOldColumnFlags_NoPreserveWidths) && (column_index < columns->Count - 1);
    const float width = preserve_width ? GetColumnWidthEx(columns, column_index, columns->IsBeingResized) : 0.0f;

    if (!(columns->Flags & ImGuiOldColumnFlags_NoForceWithinWindow))
        offset = ImMin(offset, columns->OffMaxX - g.Style.ColumnsMinSpacing * (columns->Count - column_index));
    columns->Columns[column_index].OffsetNorm = GetColumnNormFromOffset(columns, offset - columns->OffMinX);

    if (preserve_width)
        SetColumnOffset(column_index + 1, offset + ImMax(g.Style.ColumnsMinSpacing, width));
}

void ImGui::SetColumnWidth(int column_index, float width)
{
    ImGuiWindow* window = GetCurrentWindowRead();
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    IM_ASSERT(columns != NULL);

    if (column_index < 0)
        column_index = columns->Current;
    SetColumnOffset(column_index + 1, GetColumnOffset(column_index) + width);
}

void ImGui::PushColumnClipRect(int column_index)
{
    ImGuiWindow* window = GetCurrentWindowRead();
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    if (column_index < 0)
        column_index = columns->Current;

    ImGuiOldColumnData* column = &columns->Columns[column_index];
    PushClipRect(column->ClipRect.Min, column->ClipRect.Max, false);
}


void ImGui::PushColumnsBackground()
{
    ImGuiWindow* window = GetCurrentWindowRead();
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    if (columns->Count == 1)
        return;


    columns->HostBackupClipRect = window->ClipRect;
    SetWindowClipRectBeforeSetChannel(window, columns->HostInitialClipRect);
    columns->Splitter.SetCurrentChannel(window->DrawList, 0);
}

void ImGui::PopColumnsBackground()
{
    ImGuiWindow* window = GetCurrentWindowRead();
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    if (columns->Count == 1)
        return;


    SetWindowClipRectBeforeSetChannel(window, columns->HostBackupClipRect);
    columns->Splitter.SetCurrentChannel(window->DrawList, columns->Current + 1);
}

ImGuiOldColumns* ImGui::FindOrCreateColumns(ImGuiWindow* window, ImGuiID id)
{

    for (int n = 0; n < window->ColumnsStorage.Size; n++)
        if (window->ColumnsStorage[n].ID == id)
            return &window->ColumnsStorage[n];

    window->ColumnsStorage.push_back(ImGuiOldColumns());
    ImGuiOldColumns* columns = &window->ColumnsStorage.back();
    columns->ID = id;
    return columns;
}

ImGuiID ImGui::GetColumnsID(const char* str_id, int columns_count)
{
    ImGuiWindow* window = GetCurrentWindow();



    PushID(0x11223347 + (str_id ? 0 : columns_count));
    ImGuiID id = window->GetID(str_id ? str_id : "columns");
    PopID();

    return id;
}

void ImGui::BeginColumns(const char* str_id, int columns_count, ImGuiOldColumnFlags flags)
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = GetCurrentWindow();

    IM_ASSERT(columns_count >= 1);
    IM_ASSERT(window->DC.CurrentColumns == NULL);


    ImGuiID id = GetColumnsID(str_id, columns_count);
    ImGuiOldColumns* columns = FindOrCreateColumns(window, id);
    IM_ASSERT(columns->ID == id);
    columns->Current = 0;
    columns->Count = columns_count;
    columns->Flags = flags;
    window->DC.CurrentColumns = columns;
    window->DC.NavIsScrollPushableX = false;

    columns->HostCursorPosY = window->DC.CursorPos.y;
    columns->HostCursorMaxPosX = window->DC.CursorMaxPos.x;
    columns->HostInitialClipRect = window->ClipRect;
    columns->HostBackupParentWorkRect = window->ParentWorkRect;
    window->ParentWorkRect = window->WorkRect;



    const float column_padding = g.Style.ItemSpacing.x;
    const float half_clip_extend_x = ImFloor(ImMax(window->WindowPadding.x * 0.5f, window->WindowBorderSize));
    const float max_1 = window->WorkRect.Max.x + column_padding - ImMax(column_padding - window->WindowPadding.x, 0.0f);
    const float max_2 = window->WorkRect.Max.x + half_clip_extend_x;
    columns->OffMinX = window->DC.Indent.x - column_padding + ImMax(column_padding - window->WindowPadding.x, 0.0f);
    columns->OffMaxX = ImMax(ImMin(max_1, max_2) - window->Pos.x, columns->OffMinX + 1.0f);
    columns->LineMinY = columns->LineMaxY = window->DC.CursorPos.y;


    if (columns->Columns.Size != 0 && columns->Columns.Size != columns_count + 1)
        columns->Columns.resize(0);


    columns->IsFirstFrame = (columns->Columns.Size == 0);
    if (columns->Columns.Size == 0)
    {
        columns->Columns.reserve(columns_count + 1);
        for (int n = 0; n < columns_count + 1; n++)
        {
            ImGuiOldColumnData column;
            column.OffsetNorm = n / (float)columns_count;
            columns->Columns.push_back(column);
        }
    }

    for (int n = 0; n < columns_count; n++)
    {

        ImGuiOldColumnData* column = &columns->Columns[n];
        float clip_x1 = IM_ROUND(window->Pos.x + GetColumnOffset(n));
        float clip_x2 = IM_ROUND(window->Pos.x + GetColumnOffset(n + 1) - 1.0f);
        column->ClipRect = ImRect(clip_x1, -FLT_MAX, clip_x2, +FLT_MAX);
        column->ClipRect.ClipWithFull(window->ClipRect);
    }

    if (columns->Count > 1)
    {
        columns->Splitter.Split(window->DrawList, 1 + columns->Count);
        columns->Splitter.SetCurrentChannel(window->DrawList, 1);
        PushColumnClipRect(0);
    }


    float offset_0 = GetColumnOffset(columns->Current);
    float offset_1 = GetColumnOffset(columns->Current + 1);
    float width = offset_1 - offset_0;
    PushItemWidth(width * 0.65f);
    window->DC.ColumnsOffset.x = ImMax(column_padding - window->WindowPadding.x, 0.0f);
    window->DC.CursorPos.x = IM_FLOOR(window->Pos.x + window->DC.Indent.x + window->DC.ColumnsOffset.x);
    window->WorkRect.Max.x = window->Pos.x + offset_1 - column_padding;
    window->WorkRect.Max.y = window->ContentRegionRect.Max.y;
}

void ImGui::NextColumn()
{
    ImGuiWindow* window = GetCurrentWindow();
    if (window->SkipItems || window->DC.CurrentColumns == NULL)
        return;

    ImGuiContext& g = *GImGui;
    ImGuiOldColumns* columns = window->DC.CurrentColumns;

    if (columns->Count == 1)
    {
        window->DC.CursorPos.x = IM_FLOOR(window->Pos.x + window->DC.Indent.x + window->DC.ColumnsOffset.x);
        IM_ASSERT(columns->Current == 0);
        return;
    }


    if (++columns->Current == columns->Count)
        columns->Current = 0;

    PopItemWidth();



    ImGuiOldColumnData* column = &columns->Columns[columns->Current];
    SetWindowClipRectBeforeSetChannel(window, column->ClipRect);
    columns->Splitter.SetCurrentChannel(window->DrawList, columns->Current + 1);

    const float column_padding = g.Style.ItemSpacing.x;
    columns->LineMaxY = ImMax(columns->LineMaxY, window->DC.CursorPos.y);
    if (columns->Current > 0)
    {


        window->DC.ColumnsOffset.x = GetColumnOffset(columns->Current) - window->DC.Indent.x + column_padding;
    }
    else
    {

        window->DC.ColumnsOffset.x = ImMax(column_padding - window->WindowPadding.x, 0.0f);
        window->DC.IsSameLine = false;
        columns->LineMinY = columns->LineMaxY;
    }
    window->DC.CursorPos.x = IM_FLOOR(window->Pos.x + window->DC.Indent.x + window->DC.ColumnsOffset.x);
    window->DC.CursorPos.y = columns->LineMinY;
    window->DC.CurrLineSize = ImVec2(0.0f, 0.0f);
    window->DC.CurrLineTextBaseOffset = 0.0f;


    float offset_0 = GetColumnOffset(columns->Current);
    float offset_1 = GetColumnOffset(columns->Current + 1);
    float width = offset_1 - offset_0;
    PushItemWidth(width * 0.65f);
    window->WorkRect.Max.x = window->Pos.x + offset_1 - column_padding;
}

void ImGui::EndColumns()
{
    ImGuiContext& g = *GImGui;
    ImGuiWindow* window = GetCurrentWindow();
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    IM_ASSERT(columns != NULL);

    PopItemWidth();
    if (columns->Count > 1)
    {
        PopClipRect();
        columns->Splitter.Merge(window->DrawList);
    }

    const ImGuiOldColumnFlags flags = columns->Flags;
    columns->LineMaxY = ImMax(columns->LineMaxY, window->DC.CursorPos.y);
    window->DC.CursorPos.y = columns->LineMaxY;
    if (!(flags & ImGuiOldColumnFlags_GrowParentContentsSize))
        window->DC.CursorMaxPos.x = columns->HostCursorMaxPosX;



    bool is_being_resized = false;
    if (!(flags & ImGuiOldColumnFlags_NoBorder) && !window->SkipItems)
    {

        const float y1 = ImMax(columns->HostCursorPosY, window->ClipRect.Min.y);
        const float y2 = ImMin(window->DC.CursorPos.y, window->ClipRect.Max.y);
        int dragging_column = -1;
        for (int n = 1; n < columns->Count; n++)
        {
            ImGuiOldColumnData* column = &columns->Columns[n];
            float x = window->Pos.x + GetColumnOffset(n);
            const ImGuiID column_id = columns->ID + ImGuiID(n);
            const float column_hit_hw = COLUMNS_HIT_RECT_HALF_WIDTH;
            const ImRect column_hit_rect(ImVec2(x - column_hit_hw, y1), ImVec2(x + column_hit_hw, y2));
            if (!ItemAdd(column_hit_rect, column_id, NULL, ImGuiItemFlags_NoNav))
                continue;

            bool hovered = false, held = false;
            if (!(flags & ImGuiOldColumnFlags_NoResize))
            {
                ButtonBehavior(column_hit_rect, column_id, &hovered, &held);
                if (hovered || held)
                    g.MouseCursor = ImGuiMouseCursor_ResizeEW;
                if (held && !(column->Flags & ImGuiOldColumnFlags_NoResize))
                    dragging_column = n;
            }


            const ImU32 col = GetColorU32(held ? ImGuiCol_SeparatorActive : hovered ? ImGuiCol_SeparatorHovered : ImGuiCol_Separator);
            const float xi = IM_FLOOR(x);
            window->DrawList->AddLine(ImVec2(xi, y1 + 1.0f), ImVec2(xi, y2), col);
        }


        if (dragging_column != -1)
        {
            if (!columns->IsBeingResized)
                for (int n = 0; n < columns->Count + 1; n++)
                    columns->Columns[n].OffsetNormBeforeResize = columns->Columns[n].OffsetNorm;
            columns->IsBeingResized = is_being_resized = true;
            float x = GetDraggedColumnOffset(columns, dragging_column);
            SetColumnOffset(dragging_column, x);
        }
    }
    columns->IsBeingResized = is_being_resized;

    window->WorkRect = window->ParentWorkRect;
    window->ParentWorkRect = columns->HostBackupParentWorkRect;
    window->DC.CurrentColumns = NULL;
    window->DC.ColumnsOffset.x = 0.0f;
    window->DC.CursorPos.x = IM_FLOOR(window->Pos.x + window->DC.Indent.x + window->DC.ColumnsOffset.x);
    NavUpdateCurrentWindowIsScrollPushableX();
}

void ImGui::Columns(int columns_count, const char* id, bool border)
{
    ImGuiWindow* window = GetCurrentWindow();
    IM_ASSERT(columns_count >= 1);

    ImGuiOldColumnFlags flags = (border ? 0 : ImGuiOldColumnFlags_NoBorder);
    //flags |= ImGuiOldColumnFlags_NoPreserveWidths;
    ImGuiOldColumns* columns = window->DC.CurrentColumns;
    if (columns != NULL && columns->Count == columns_count && columns->Flags == flags)
        return;

    if (columns != NULL)
        EndColumns();

    if (columns_count != 1)
        BeginColumns(id, columns_count, flags);
}

//-------------------------------------------------------------------------

#endif
