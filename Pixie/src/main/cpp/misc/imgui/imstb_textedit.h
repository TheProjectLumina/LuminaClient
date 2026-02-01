








//





//




//

//
//

//

//
//

//



//
//

//


















//

//




//





//

//


//
//

//





//


//


//



//



//


//
//

//



//



//






//

//




//

//


//












//


//

//














//












//




//








//





//



//

//

//





//


//



//




//



//




//



//









//
//


//
//

//










//





////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////
//
////
////

#ifndef INCLUDE_STB_TEXTEDIT_H
#define INCLUDE_STB_TEXTEDIT_H

////////////////////////////////////////////////////////////////////////
//

//



//

#ifndef STB_TEXTEDIT_UNDOSTATECOUNT
#define STB_TEXTEDIT_UNDOSTATECOUNT   99
#endif
#ifndef STB_TEXTEDIT_UNDOCHARCOUNT
#define STB_TEXTEDIT_UNDOCHARCOUNT   999
#endif
#ifndef STB_TEXTEDIT_CHARTYPE
#define STB_TEXTEDIT_CHARTYPE        int
#endif
#ifndef STB_TEXTEDIT_POSITIONTYPE
#define STB_TEXTEDIT_POSITIONTYPE    int
#endif

typedef struct
{

   STB_TEXTEDIT_POSITIONTYPE  where;
   STB_TEXTEDIT_POSITIONTYPE  insert_length;
   STB_TEXTEDIT_POSITIONTYPE  delete_length;
   int                        char_storage;
} StbUndoRecord;

typedef struct
{

   StbUndoRecord          undo_rec [STB_TEXTEDIT_UNDOSTATECOUNT];
   STB_TEXTEDIT_CHARTYPE  undo_char[STB_TEXTEDIT_UNDOCHARCOUNT];
   short undo_point, redo_point;
   int undo_char_point, redo_char_point;
} StbUndoState;

typedef struct
{
   /////////////////////
   //

   //

   int cursor;


   int select_start;
   int select_end;





   unsigned char insert_mode;



   int row_count_per_page;



   /////////////////////
   //

   //
   unsigned char cursor_at_end_of_line;
   unsigned char initialized;
   unsigned char has_preferred_x;
   unsigned char single_line;
   unsigned char padding1, padding2, padding3;
   float preferred_x;
   StbUndoState undostate;
} STB_TexteditState;


////////////////////////////////////////////////////////////////////////
//

//




typedef struct
{
   float x0,x1;
   float baseline_y_delta;
   float ymin,ymax;
   int num_chars;
} StbTexteditRow;
#endif //INCLUDE_STB_TEXTEDIT_H


////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////
//
////
////




#ifdef STB_TEXTEDIT_IMPLEMENTATION

#ifndef STB_TEXTEDIT_memmove
#include <string.h>
#define STB_TEXTEDIT_memmove memmove
#endif


/////////////////////////////////////////////////////////////////////////////
//

//


static int stb_text_locate_coord(STB_TEXTEDIT_STRING *str, float x, float y)
{
   StbTexteditRow r;
   int n = STB_TEXTEDIT_STRINGLEN(str);
   float base_y = 0, prev_x;
   int i=0, k;

   r.x0 = r.x1 = 0;
   r.ymin = r.ymax = 0;
   r.num_chars = 0;


   while (i < n) {
      STB_TEXTEDIT_LAYOUTROW(&r, str, i);
      if (r.num_chars <= 0)
         return n;

      if (i==0 && y < base_y + r.ymin)
         return 0;

      if (y < base_y + r.ymax)
         break;

      i += r.num_chars;
      base_y += r.baseline_y_delta;
   }


   if (i >= n)
      return n;


   if (x < r.x0)
      return i;


   if (x < r.x1) {

      prev_x = r.x0;
      for (k=0; k < r.num_chars; ++k) {
         float w = STB_TEXTEDIT_GETWIDTH(str, i, k);
         if (x < prev_x+w) {
            if (x < prev_x+w/2)
               return k+i;
            else
               return k+i+1;
         }
         prev_x += w;
      }

   }


   if (STB_TEXTEDIT_GETCHAR(str, i+r.num_chars-1) == STB_TEXTEDIT_NEWLINE)
      return i+r.num_chars-1;
   else
      return i+r.num_chars;
}


static void stb_textedit_click(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, float x, float y)
{


   if( state->single_line )
   {
      StbTexteditRow r;
      STB_TEXTEDIT_LAYOUTROW(&r, str, 0);
      y = r.ymin;
   }

   state->cursor = stb_text_locate_coord(str, x, y);
   state->select_start = state->cursor;
   state->select_end = state->cursor;
   state->has_preferred_x = 0;
}


static void stb_textedit_drag(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, float x, float y)
{
   int p = 0;



   if( state->single_line )
   {
      StbTexteditRow r;
      STB_TEXTEDIT_LAYOUTROW(&r, str, 0);
      y = r.ymin;
   }

   if (state->select_start == state->select_end)
      state->select_start = state->cursor;

   p = stb_text_locate_coord(str, x, y);
   state->cursor = state->select_end = p;
}

/////////////////////////////////////////////////////////////////////////////
//

//


static void stb_text_undo(STB_TEXTEDIT_STRING *str, STB_TexteditState *state);
static void stb_text_redo(STB_TEXTEDIT_STRING *str, STB_TexteditState *state);
static void stb_text_makeundo_delete(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, int where, int length);
static void stb_text_makeundo_insert(STB_TexteditState *state, int where, int length);
static void stb_text_makeundo_replace(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, int where, int old_length, int new_length);

typedef struct
{
   float x,y;
   float height;
   int first_char, length;
   int prev_first;
} StbFindState;



static void stb_textedit_find_charpos(StbFindState *find, STB_TEXTEDIT_STRING *str, int n, int single_line)
{
   StbTexteditRow r;
   int prev_start = 0;
   int z = STB_TEXTEDIT_STRINGLEN(str);
   int i=0, first;

   if (n == z && single_line) {

      STB_TEXTEDIT_LAYOUTROW(&r, str, 0);
      find->y = 0;
      find->first_char = 0;
      find->length = z;
      find->height = r.ymax - r.ymin;
      find->x = r.x1;
      return;
   }


   find->y = 0;

   for(;;) {
      STB_TEXTEDIT_LAYOUTROW(&r, str, i);
      if (n < i + r.num_chars)
         break;
      if (i + r.num_chars == z && z > 0 && STB_TEXTEDIT_GETCHAR(str, z - 1) != STB_TEXTEDIT_NEWLINE)
         break;
      prev_start = i;
      i += r.num_chars;
      find->y += r.baseline_y_delta;
      if (i == z)
         break;
   }

   find->first_char = first = i;
   find->length = r.num_chars;
   find->height = r.ymax - r.ymin;
   find->prev_first = prev_start;


   find->x = r.x0;
   for (i=0; first+i < n; ++i)
      find->x += STB_TEXTEDIT_GETWIDTH(str, first, i);
}

#define STB_TEXT_HAS_SELECTION(s)   ((s)->select_start != (s)->select_end)


static void stb_textedit_clamp(STB_TEXTEDIT_STRING *str, STB_TexteditState *state)
{
   int n = STB_TEXTEDIT_STRINGLEN(str);
   if (STB_TEXT_HAS_SELECTION(state)) {
      if (state->select_start > n) state->select_start = n;
      if (state->select_end   > n) state->select_end = n;

      if (state->select_start == state->select_end)
         state->cursor = state->select_start;
   }
   if (state->cursor > n) state->cursor = n;
}


static void stb_textedit_delete(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, int where, int len)
{
   stb_text_makeundo_delete(str, state, where, len);
   STB_TEXTEDIT_DELETECHARS(str, where, len);
   state->has_preferred_x = 0;
}


static void stb_textedit_delete_selection(STB_TEXTEDIT_STRING *str, STB_TexteditState *state)
{
   stb_textedit_clamp(str, state);
   if (STB_TEXT_HAS_SELECTION(state)) {
      if (state->select_start < state->select_end) {
         stb_textedit_delete(str, state, state->select_start, state->select_end - state->select_start);
         state->select_end = state->cursor = state->select_start;
      } else {
         stb_textedit_delete(str, state, state->select_end, state->select_start - state->select_end);
         state->select_start = state->cursor = state->select_end;
      }
      state->has_preferred_x = 0;
   }
}


static void stb_textedit_sortselection(STB_TexteditState *state)
{
   if (state->select_end < state->select_start) {
      int temp = state->select_end;
      state->select_end = state->select_start;
      state->select_start = temp;
   }
}


static void stb_textedit_move_to_first(STB_TexteditState *state)
{
   if (STB_TEXT_HAS_SELECTION(state)) {
      stb_textedit_sortselection(state);
      state->cursor = state->select_start;
      state->select_end = state->select_start;
      state->has_preferred_x = 0;
   }
}


static void stb_textedit_move_to_last(STB_TEXTEDIT_STRING *str, STB_TexteditState *state)
{
   if (STB_TEXT_HAS_SELECTION(state)) {
      stb_textedit_sortselection(state);
      stb_textedit_clamp(str, state);
      state->cursor = state->select_end;
      state->select_start = state->select_end;
      state->has_preferred_x = 0;
   }
}

#ifdef STB_TEXTEDIT_IS_SPACE
static int is_word_boundary( STB_TEXTEDIT_STRING *str, int idx )
{
   return idx > 0 ? (STB_TEXTEDIT_IS_SPACE( STB_TEXTEDIT_GETCHAR(str,idx-1) ) && !STB_TEXTEDIT_IS_SPACE( STB_TEXTEDIT_GETCHAR(str, idx) ) ) : 1;
}

#ifndef STB_TEXTEDIT_MOVEWORDLEFT
static int stb_textedit_move_to_word_previous( STB_TEXTEDIT_STRING *str, int c )
{
   --c;
   while( c >= 0 && !is_word_boundary( str, c ) )
      --c;

   if( c < 0 )
      c = 0;

   return c;
}
#define STB_TEXTEDIT_MOVEWORDLEFT stb_textedit_move_to_word_previous
#endif

#ifndef STB_TEXTEDIT_MOVEWORDRIGHT
static int stb_textedit_move_to_word_next( STB_TEXTEDIT_STRING *str, int c )
{
   const int len = STB_TEXTEDIT_STRINGLEN(str);
   ++c;
   while( c < len && !is_word_boundary( str, c ) )
      ++c;

   if( c > len )
      c = len;

   return c;
}
#define STB_TEXTEDIT_MOVEWORDRIGHT stb_textedit_move_to_word_next
#endif

#endif


static void stb_textedit_prep_selection_at_cursor(STB_TexteditState *state)
{
   if (!STB_TEXT_HAS_SELECTION(state))
      state->select_start = state->select_end = state->cursor;
   else
      state->cursor = state->select_end;
}


static int stb_textedit_cut(STB_TEXTEDIT_STRING *str, STB_TexteditState *state)
{
   if (STB_TEXT_HAS_SELECTION(state)) {
      stb_textedit_delete_selection(str,state);
      state->has_preferred_x = 0;
      return 1;
   }
   return 0;
}


static int stb_textedit_paste_internal(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, STB_TEXTEDIT_CHARTYPE *text, int len)
{

   stb_textedit_clamp(str, state);
   stb_textedit_delete_selection(str,state);

   if (STB_TEXTEDIT_INSERTCHARS(str, state->cursor, text, len)) {
      stb_text_makeundo_insert(state, state->cursor, len);
      state->cursor += len;
      state->has_preferred_x = 0;
      return 1;
   }

   return 0;
}

#ifndef STB_TEXTEDIT_KEYTYPE
#define STB_TEXTEDIT_KEYTYPE int
#endif


static void stb_textedit_key(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, STB_TEXTEDIT_KEYTYPE key)
{
retry:
   switch (key) {
      default: {
         int c = STB_TEXTEDIT_KEYTOTEXT(key);
         if (c > 0) {
            STB_TEXTEDIT_CHARTYPE ch = (STB_TEXTEDIT_CHARTYPE) c;


            if (c == '\n' && state->single_line)
               break;

            if (state->insert_mode && !STB_TEXT_HAS_SELECTION(state) && state->cursor < STB_TEXTEDIT_STRINGLEN(str)) {
               stb_text_makeundo_replace(str, state, state->cursor, 1, 1);
               STB_TEXTEDIT_DELETECHARS(str, state->cursor, 1);
               if (STB_TEXTEDIT_INSERTCHARS(str, state->cursor, &ch, 1)) {
                  ++state->cursor;
                  state->has_preferred_x = 0;
               }
            } else {
               stb_textedit_delete_selection(str,state);
               if (STB_TEXTEDIT_INSERTCHARS(str, state->cursor, &ch, 1)) {
                  stb_text_makeundo_insert(state, state->cursor, 1);
                  ++state->cursor;
                  state->has_preferred_x = 0;
               }
            }
         }
         break;
      }

#ifdef STB_TEXTEDIT_K_INSERT
      case STB_TEXTEDIT_K_INSERT:
         state->insert_mode = !state->insert_mode;
         break;
#endif

      case STB_TEXTEDIT_K_UNDO:
         stb_text_undo(str, state);
         state->has_preferred_x = 0;
         break;

      case STB_TEXTEDIT_K_REDO:
         stb_text_redo(str, state);
         state->has_preferred_x = 0;
         break;

      case STB_TEXTEDIT_K_LEFT:

         if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_move_to_first(state);
         else
            if (state->cursor > 0)
               --state->cursor;
         state->has_preferred_x = 0;
         break;

      case STB_TEXTEDIT_K_RIGHT:

         if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_move_to_last(str, state);
         else
            ++state->cursor;
         stb_textedit_clamp(str, state);
         state->has_preferred_x = 0;
         break;

      case STB_TEXTEDIT_K_LEFT | STB_TEXTEDIT_K_SHIFT:
         stb_textedit_clamp(str, state);
         stb_textedit_prep_selection_at_cursor(state);

         if (state->select_end > 0)
            --state->select_end;
         state->cursor = state->select_end;
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_MOVEWORDLEFT
      case STB_TEXTEDIT_K_WORDLEFT:
         if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_move_to_first(state);
         else {
            state->cursor = STB_TEXTEDIT_MOVEWORDLEFT(str, state->cursor);
            stb_textedit_clamp( str, state );
         }
         break;

      case STB_TEXTEDIT_K_WORDLEFT | STB_TEXTEDIT_K_SHIFT:
         if( !STB_TEXT_HAS_SELECTION( state ) )
            stb_textedit_prep_selection_at_cursor(state);

         state->cursor = STB_TEXTEDIT_MOVEWORDLEFT(str, state->cursor);
         state->select_end = state->cursor;

         stb_textedit_clamp( str, state );
         break;
#endif

#ifdef STB_TEXTEDIT_MOVEWORDRIGHT
      case STB_TEXTEDIT_K_WORDRIGHT:
         if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_move_to_last(str, state);
         else {
            state->cursor = STB_TEXTEDIT_MOVEWORDRIGHT(str, state->cursor);
            stb_textedit_clamp( str, state );
         }
         break;

      case STB_TEXTEDIT_K_WORDRIGHT | STB_TEXTEDIT_K_SHIFT:
         if( !STB_TEXT_HAS_SELECTION( state ) )
            stb_textedit_prep_selection_at_cursor(state);

         state->cursor = STB_TEXTEDIT_MOVEWORDRIGHT(str, state->cursor);
         state->select_end = state->cursor;

         stb_textedit_clamp( str, state );
         break;
#endif

      case STB_TEXTEDIT_K_RIGHT | STB_TEXTEDIT_K_SHIFT:
         stb_textedit_prep_selection_at_cursor(state);

         ++state->select_end;
         stb_textedit_clamp(str, state);
         state->cursor = state->select_end;
         state->has_preferred_x = 0;
         break;

      case STB_TEXTEDIT_K_DOWN:
      case STB_TEXTEDIT_K_DOWN | STB_TEXTEDIT_K_SHIFT:
      case STB_TEXTEDIT_K_PGDOWN:
      case STB_TEXTEDIT_K_PGDOWN | STB_TEXTEDIT_K_SHIFT: {
         StbFindState find;
         StbTexteditRow row;
         int i, j, sel = (key & STB_TEXTEDIT_K_SHIFT) != 0;
         int is_page = (key & ~STB_TEXTEDIT_K_SHIFT) == STB_TEXTEDIT_K_PGDOWN;
         int row_count = is_page ? state->row_count_per_page : 1;

         if (!is_page && state->single_line) {

            key = STB_TEXTEDIT_K_RIGHT | (key & STB_TEXTEDIT_K_SHIFT);
            goto retry;
         }

         if (sel)
            stb_textedit_prep_selection_at_cursor(state);
         else if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_move_to_last(str, state);


         stb_textedit_clamp(str, state);
         stb_textedit_find_charpos(&find, str, state->cursor, state->single_line);

         for (j = 0; j < row_count; ++j) {
            float x, goal_x = state->has_preferred_x ? state->preferred_x : find.x;
            int start = find.first_char + find.length;

            if (find.length == 0)
               break;



            if (STB_TEXTEDIT_GETCHAR(str, find.first_char + find.length - 1) != STB_TEXTEDIT_NEWLINE)
               break;


            state->cursor = start;
            STB_TEXTEDIT_LAYOUTROW(&row, str, state->cursor);
            x = row.x0;
            for (i=0; i < row.num_chars; ++i) {
               float dx = STB_TEXTEDIT_GETWIDTH(str, start, i);
               #ifdef STB_TEXTEDIT_GETWIDTH_NEWLINE
               if (dx == STB_TEXTEDIT_GETWIDTH_NEWLINE)
                  break;
               #endif
               x += dx;
               if (x > goal_x)
                  break;
               ++state->cursor;
            }
            stb_textedit_clamp(str, state);

            state->has_preferred_x = 1;
            state->preferred_x = goal_x;

            if (sel)
               state->select_end = state->cursor;


            find.first_char = find.first_char + find.length;
            find.length = row.num_chars;
         }
         break;
      }

      case STB_TEXTEDIT_K_UP:
      case STB_TEXTEDIT_K_UP | STB_TEXTEDIT_K_SHIFT:
      case STB_TEXTEDIT_K_PGUP:
      case STB_TEXTEDIT_K_PGUP | STB_TEXTEDIT_K_SHIFT: {
         StbFindState find;
         StbTexteditRow row;
         int i, j, prev_scan, sel = (key & STB_TEXTEDIT_K_SHIFT) != 0;
         int is_page = (key & ~STB_TEXTEDIT_K_SHIFT) == STB_TEXTEDIT_K_PGUP;
         int row_count = is_page ? state->row_count_per_page : 1;

         if (!is_page && state->single_line) {

            key = STB_TEXTEDIT_K_LEFT | (key & STB_TEXTEDIT_K_SHIFT);
            goto retry;
         }

         if (sel)
            stb_textedit_prep_selection_at_cursor(state);
         else if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_move_to_first(state);


         stb_textedit_clamp(str, state);
         stb_textedit_find_charpos(&find, str, state->cursor, state->single_line);

         for (j = 0; j < row_count; ++j) {
            float  x, goal_x = state->has_preferred_x ? state->preferred_x : find.x;


            if (find.prev_first == find.first_char)
               break;


            state->cursor = find.prev_first;
            STB_TEXTEDIT_LAYOUTROW(&row, str, state->cursor);
            x = row.x0;
            for (i=0; i < row.num_chars; ++i) {
               float dx = STB_TEXTEDIT_GETWIDTH(str, find.prev_first, i);
               #ifdef STB_TEXTEDIT_GETWIDTH_NEWLINE
               if (dx == STB_TEXTEDIT_GETWIDTH_NEWLINE)
                  break;
               #endif
               x += dx;
               if (x > goal_x)
                  break;
               ++state->cursor;
            }
            stb_textedit_clamp(str, state);

            state->has_preferred_x = 1;
            state->preferred_x = goal_x;

            if (sel)
               state->select_end = state->cursor;



            prev_scan = find.prev_first > 0 ? find.prev_first - 1 : 0;
            while (prev_scan > 0 && STB_TEXTEDIT_GETCHAR(str, prev_scan - 1) != STB_TEXTEDIT_NEWLINE)
               --prev_scan;
            find.first_char = find.prev_first;
            find.prev_first = prev_scan;
         }
         break;
      }

      case STB_TEXTEDIT_K_DELETE:
      case STB_TEXTEDIT_K_DELETE | STB_TEXTEDIT_K_SHIFT:
         if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_delete_selection(str, state);
         else {
            int n = STB_TEXTEDIT_STRINGLEN(str);
            if (state->cursor < n)
               stb_textedit_delete(str, state, state->cursor, 1);
         }
         state->has_preferred_x = 0;
         break;

      case STB_TEXTEDIT_K_BACKSPACE:
      case STB_TEXTEDIT_K_BACKSPACE | STB_TEXTEDIT_K_SHIFT:
         if (STB_TEXT_HAS_SELECTION(state))
            stb_textedit_delete_selection(str, state);
         else {
            stb_textedit_clamp(str, state);
            if (state->cursor > 0) {
               stb_textedit_delete(str, state, state->cursor-1, 1);
               --state->cursor;
            }
         }
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_K_TEXTSTART2
      case STB_TEXTEDIT_K_TEXTSTART2:
#endif
      case STB_TEXTEDIT_K_TEXTSTART:
         state->cursor = state->select_start = state->select_end = 0;
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_K_TEXTEND2
      case STB_TEXTEDIT_K_TEXTEND2:
#endif
      case STB_TEXTEDIT_K_TEXTEND:
         state->cursor = STB_TEXTEDIT_STRINGLEN(str);
         state->select_start = state->select_end = 0;
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_K_TEXTSTART2
      case STB_TEXTEDIT_K_TEXTSTART2 | STB_TEXTEDIT_K_SHIFT:
#endif
      case STB_TEXTEDIT_K_TEXTSTART | STB_TEXTEDIT_K_SHIFT:
         stb_textedit_prep_selection_at_cursor(state);
         state->cursor = state->select_end = 0;
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_K_TEXTEND2
      case STB_TEXTEDIT_K_TEXTEND2 | STB_TEXTEDIT_K_SHIFT:
#endif
      case STB_TEXTEDIT_K_TEXTEND | STB_TEXTEDIT_K_SHIFT:
         stb_textedit_prep_selection_at_cursor(state);
         state->cursor = state->select_end = STB_TEXTEDIT_STRINGLEN(str);
         state->has_preferred_x = 0;
         break;


#ifdef STB_TEXTEDIT_K_LINESTART2
      case STB_TEXTEDIT_K_LINESTART2:
#endif
      case STB_TEXTEDIT_K_LINESTART:
         stb_textedit_clamp(str, state);
         stb_textedit_move_to_first(state);
         if (state->single_line)
            state->cursor = 0;
         else while (state->cursor > 0 && STB_TEXTEDIT_GETCHAR(str, state->cursor-1) != STB_TEXTEDIT_NEWLINE)
            --state->cursor;
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_K_LINEEND2
      case STB_TEXTEDIT_K_LINEEND2:
#endif
      case STB_TEXTEDIT_K_LINEEND: {
         int n = STB_TEXTEDIT_STRINGLEN(str);
         stb_textedit_clamp(str, state);
         stb_textedit_move_to_first(state);
         if (state->single_line)
             state->cursor = n;
         else while (state->cursor < n && STB_TEXTEDIT_GETCHAR(str, state->cursor) != STB_TEXTEDIT_NEWLINE)
             ++state->cursor;
         state->has_preferred_x = 0;
         break;
      }

#ifdef STB_TEXTEDIT_K_LINESTART2
      case STB_TEXTEDIT_K_LINESTART2 | STB_TEXTEDIT_K_SHIFT:
#endif
      case STB_TEXTEDIT_K_LINESTART | STB_TEXTEDIT_K_SHIFT:
         stb_textedit_clamp(str, state);
         stb_textedit_prep_selection_at_cursor(state);
         if (state->single_line)
            state->cursor = 0;
         else while (state->cursor > 0 && STB_TEXTEDIT_GETCHAR(str, state->cursor-1) != STB_TEXTEDIT_NEWLINE)
            --state->cursor;
         state->select_end = state->cursor;
         state->has_preferred_x = 0;
         break;

#ifdef STB_TEXTEDIT_K_LINEEND2
      case STB_TEXTEDIT_K_LINEEND2 | STB_TEXTEDIT_K_SHIFT:
#endif
      case STB_TEXTEDIT_K_LINEEND | STB_TEXTEDIT_K_SHIFT: {
         int n = STB_TEXTEDIT_STRINGLEN(str);
         stb_textedit_clamp(str, state);
         stb_textedit_prep_selection_at_cursor(state);
         if (state->single_line)
             state->cursor = n;
         else while (state->cursor < n && STB_TEXTEDIT_GETCHAR(str, state->cursor) != STB_TEXTEDIT_NEWLINE)
            ++state->cursor;
         state->select_end = state->cursor;
         state->has_preferred_x = 0;
         break;
      }
   }
}

/////////////////////////////////////////////////////////////////////////////
//

//


static void stb_textedit_flush_redo(StbUndoState *state)
{
   state->redo_point = STB_TEXTEDIT_UNDOSTATECOUNT;
   state->redo_char_point = STB_TEXTEDIT_UNDOCHARCOUNT;
}


static void stb_textedit_discard_undo(StbUndoState *state)
{
   if (state->undo_point > 0) {

      if (state->undo_rec[0].char_storage >= 0) {
         int n = state->undo_rec[0].insert_length, i;

         state->undo_char_point -= n;
         STB_TEXTEDIT_memmove(state->undo_char, state->undo_char + n, (size_t) (state->undo_char_point*sizeof(STB_TEXTEDIT_CHARTYPE)));
         for (i=0; i < state->undo_point; ++i)
            if (state->undo_rec[i].char_storage >= 0)
               state->undo_rec[i].char_storage -= n;
      }
      --state->undo_point;
      STB_TEXTEDIT_memmove(state->undo_rec, state->undo_rec+1, (size_t) (state->undo_point*sizeof(state->undo_rec[0])));
   }
}





static void stb_textedit_discard_redo(StbUndoState *state)
{
   int k = STB_TEXTEDIT_UNDOSTATECOUNT-1;

   if (state->redo_point <= k) {

      if (state->undo_rec[k].char_storage >= 0) {
         int n = state->undo_rec[k].insert_length, i;

         state->redo_char_point += n;
         STB_TEXTEDIT_memmove(state->undo_char + state->redo_char_point, state->undo_char + state->redo_char_point-n, (size_t) ((STB_TEXTEDIT_UNDOCHARCOUNT - state->redo_char_point)*sizeof(STB_TEXTEDIT_CHARTYPE)));

         for (i=state->redo_point; i < k; ++i)
            if (state->undo_rec[i].char_storage >= 0)
               state->undo_rec[i].char_storage += n;
      }


      size_t move_size = (size_t)((STB_TEXTEDIT_UNDOSTATECOUNT - state->redo_point - 1) * sizeof(state->undo_rec[0]));
      const char* buf_begin = (char*)state->undo_rec; (void)buf_begin;
      const char* buf_end   = (char*)state->undo_rec + sizeof(state->undo_rec); (void)buf_end;
      IM_ASSERT(((char*)(state->undo_rec + state->redo_point)) >= buf_begin);
      IM_ASSERT(((char*)(state->undo_rec + state->redo_point + 1) + move_size) <= buf_end);
      STB_TEXTEDIT_memmove(state->undo_rec + state->redo_point+1, state->undo_rec + state->redo_point, move_size);


      ++state->redo_point;
   }
}

static StbUndoRecord *stb_text_create_undo_record(StbUndoState *state, int numchars)
{

   stb_textedit_flush_redo(state);



   if (state->undo_point == STB_TEXTEDIT_UNDOSTATECOUNT)
      stb_textedit_discard_undo(state);


   if (numchars > STB_TEXTEDIT_UNDOCHARCOUNT) {
      state->undo_point = 0;
      state->undo_char_point = 0;
      return NULL;
   }


   while (state->undo_char_point + numchars > STB_TEXTEDIT_UNDOCHARCOUNT)
      stb_textedit_discard_undo(state);

   return &state->undo_rec[state->undo_point++];
}

static STB_TEXTEDIT_CHARTYPE *stb_text_createundo(StbUndoState *state, int pos, int insert_len, int delete_len)
{
   StbUndoRecord *r = stb_text_create_undo_record(state, insert_len);
   if (r == NULL)
      return NULL;

   r->where = pos;
   r->insert_length = (STB_TEXTEDIT_POSITIONTYPE) insert_len;
   r->delete_length = (STB_TEXTEDIT_POSITIONTYPE) delete_len;

   if (insert_len == 0) {
      r->char_storage = -1;
      return NULL;
   } else {
      r->char_storage = state->undo_char_point;
      state->undo_char_point += insert_len;
      return &state->undo_char[r->char_storage];
   }
}

static void stb_text_undo(STB_TEXTEDIT_STRING *str, STB_TexteditState *state)
{
   StbUndoState *s = &state->undostate;
   StbUndoRecord u, *r;
   if (s->undo_point == 0)
      return;


   u = s->undo_rec[s->undo_point-1];
   r = &s->undo_rec[s->redo_point-1];
   r->char_storage = -1;

   r->insert_length = u.delete_length;
   r->delete_length = u.insert_length;
   r->where = u.where;

   if (u.delete_length) {










      if (s->undo_char_point + u.delete_length >= STB_TEXTEDIT_UNDOCHARCOUNT) {

         r->insert_length = 0;
      } else {
         int i;


         while (s->undo_char_point + u.delete_length > s->redo_char_point) {

            if (s->redo_point == STB_TEXTEDIT_UNDOSTATECOUNT)
               return;

            stb_textedit_discard_redo(s);
         }
         r = &s->undo_rec[s->redo_point-1];

         r->char_storage = s->redo_char_point - u.delete_length;
         s->redo_char_point = s->redo_char_point - u.delete_length;


         for (i=0; i < u.delete_length; ++i)
            s->undo_char[r->char_storage + i] = STB_TEXTEDIT_GETCHAR(str, u.where + i);
      }


      STB_TEXTEDIT_DELETECHARS(str, u.where, u.delete_length);
   }


   if (u.insert_length) {

      STB_TEXTEDIT_INSERTCHARS(str, u.where, &s->undo_char[u.char_storage], u.insert_length);
      s->undo_char_point -= u.insert_length;
   }

   state->cursor = u.where + u.insert_length;

   s->undo_point--;
   s->redo_point--;
}

static void stb_text_redo(STB_TEXTEDIT_STRING *str, STB_TexteditState *state)
{
   StbUndoState *s = &state->undostate;
   StbUndoRecord *u, r;
   if (s->redo_point == STB_TEXTEDIT_UNDOSTATECOUNT)
      return;


   u = &s->undo_rec[s->undo_point];
   r = s->undo_rec[s->redo_point];




   u->delete_length = r.insert_length;
   u->insert_length = r.delete_length;
   u->where = r.where;
   u->char_storage = -1;

   if (r.delete_length) {



      if (s->undo_char_point + u->insert_length > s->redo_char_point) {
         u->insert_length = 0;
         u->delete_length = 0;
      } else {
         int i;
         u->char_storage = s->undo_char_point;
         s->undo_char_point = s->undo_char_point + u->insert_length;


         for (i=0; i < u->insert_length; ++i)
            s->undo_char[u->char_storage + i] = STB_TEXTEDIT_GETCHAR(str, u->where + i);
      }

      STB_TEXTEDIT_DELETECHARS(str, r.where, r.delete_length);
   }

   if (r.insert_length) {

      STB_TEXTEDIT_INSERTCHARS(str, r.where, &s->undo_char[r.char_storage], r.insert_length);
      s->redo_char_point += r.insert_length;
   }

   state->cursor = r.where + r.insert_length;

   s->undo_point++;
   s->redo_point++;
}

static void stb_text_makeundo_insert(STB_TexteditState *state, int where, int length)
{
   stb_text_createundo(&state->undostate, where, 0, length);
}

static void stb_text_makeundo_delete(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, int where, int length)
{
   int i;
   STB_TEXTEDIT_CHARTYPE *p = stb_text_createundo(&state->undostate, where, length, 0);
   if (p) {
      for (i=0; i < length; ++i)
         p[i] = STB_TEXTEDIT_GETCHAR(str, where+i);
   }
}

static void stb_text_makeundo_replace(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, int where, int old_length, int new_length)
{
   int i;
   STB_TEXTEDIT_CHARTYPE *p = stb_text_createundo(&state->undostate, where, old_length, new_length);
   if (p) {
      for (i=0; i < old_length; ++i)
         p[i] = STB_TEXTEDIT_GETCHAR(str, where+i);
   }
}


static void stb_textedit_clear_state(STB_TexteditState *state, int is_single_line)
{
   state->undostate.undo_point = 0;
   state->undostate.undo_char_point = 0;
   state->undostate.redo_point = STB_TEXTEDIT_UNDOSTATECOUNT;
   state->undostate.redo_char_point = STB_TEXTEDIT_UNDOCHARCOUNT;
   state->select_end = state->select_start = 0;
   state->cursor = 0;
   state->has_preferred_x = 0;
   state->preferred_x = 0;
   state->cursor_at_end_of_line = 0;
   state->initialized = 1;
   state->single_line = (unsigned char) is_single_line;
   state->insert_mode = 0;
   state->row_count_per_page = 0;
}


static void stb_textedit_initialize_state(STB_TexteditState *state, int is_single_line)
{
   stb_textedit_clear_state(state, is_single_line);
}

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wcast-qual"
#endif

static int stb_textedit_paste(STB_TEXTEDIT_STRING *str, STB_TexteditState *state, STB_TEXTEDIT_CHARTYPE const *ctext, int len)
{
   return stb_textedit_paste_internal(str, state, (STB_TEXTEDIT_CHARTYPE *) ctext, len);
}

#if defined(__GNUC__) || defined(__clang__)
#pragma GCC diagnostic pop
#endif

#endif//STB_TEXTEDIT_IMPLEMENTATION

/*
------------------------------------------------------------------------------
This software is available under 2 licenses -- choose whichever you prefer.
------------------------------------------------------------------------------
ALTERNATIVE A - MIT License
Copyright (c) 2017 Sean Barrett
Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
------------------------------------------------------------------------------
ALTERNATIVE B - Public Domain (www.unlicense.org)
This is free and unencumbered software released into the public domain.
Anyone is free to copy, modify, publish, use, compile, sell, or distribute this
software, either in source code form or as a compiled binary, for any purpose,
commercial or non-commercial, and by any means.
In jurisdictions that recognize copyright laws, the author or authors of this
software dedicate any and all copyright interest in the software to the public
domain. We make this dedication for the benefit of the public at large and to
the detriment of our heirs and successors. We intend this dedication to be an
overt act of relinquishment in perpetuity of all present and future rights to
this software under copyright law.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
------------------------------------------------------------------------------
*/
