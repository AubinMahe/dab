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

typedef struct util_automaton_actions_s {

   int                   state;
   util_automaton_action action;
} util_automaton_actions;

typedef struct util_automaton_s {

   int                            current;
   const util_transition *        arcs;
   unsigned                       arcs_count;
   const util_shortcut *          shortcuts;
   unsigned                       shortcuts_count;
   void *                         actor;
   const util_automaton_actions * actions;
   unsigned                       actions_count;

} util_automaton;

util_error util_automaton_init(
   util_automaton *               This,
   int                            initial,
   const util_transition *        arcs,
   unsigned                       arcs_count,
   const util_shortcut *          shortcuts,
   unsigned                       shortcuts_count,
   void *                         actor,
   const util_automaton_actions * actions,
   unsigned                       actions_count );

util_error util_automaton_process(
   util_automaton * This,
   int              event );
