#pragma once

#include "map.h"

/**
 * Ne sert qu'à peupler l'automate.
 */
typedef struct util_arc_s {

   int current;
   int event;
   int next;

} util_arc;

/**
 * Ne sert qu'à peupler l'automate.
 */
typedef struct util_shortcut_s {

   int event;
   int next;

} util_shortcut;

typedef struct util_automaton_s {

   int                   current;
   const util_arc *      arcs;
   unsigned              arcs_count;
   const util_shortcut * shortcuts;
   unsigned              shortcuts_count;

} util_automaton;

util_error util_automaton_init   ( util_automaton * This, int initial,
   const util_arc *      arcs     , unsigned arcs_count,
   const util_shortcut * shortcuts, unsigned shortcuts_count );
util_error util_automaton_process( util_automaton * This, int event );
