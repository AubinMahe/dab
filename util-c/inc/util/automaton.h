#pragma once

#include "map.h"

typedef struct util_transition_s {

   int current;
   int event;
   int next;

} util_transition;

typedef struct util_shortcut_s {

   int event;
   int next;

} util_shortcut;

typedef void( * util_automaton_action )( void *);

typedef struct util_automaton_state_action_s {

   int                   state;
   util_automaton_action action;
} util_automaton_state_action;

typedef struct util_automaton_s {

   bool                                debug;
   int                                 current;
   const util_transition *             arcs;
   unsigned                            arcs_count;
   const util_shortcut *               shortcuts;
   unsigned                            shortcuts_count;
   void *                              actor;
   const util_automaton_state_action * on_entry;
   unsigned                            on_entry_count;
   const util_automaton_state_action * on_exit;
   unsigned                            on_exit_count;

} util_automaton;

util_error util_automaton_process( util_automaton * This, int event );
