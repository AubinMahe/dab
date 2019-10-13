#include <util/automaton.h>
#include <stdio.h>

static util_error fire_action( util_automaton * This, const util_automaton_state_action * actions, size_t count ) {
   for( unsigned i = 0; i < count; ++i ) {
      const util_automaton_state_action * action = actions + i;
      if( action->state == This->current ) {
         (action->action)( This->actor );
         return UTIL_NO_ERROR;
      }
   }
   return UTIL_NOT_APPLICABLE;
}

util_error util_automaton_process( util_automaton * This, int event ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   for( unsigned i = 0; i < This->shortcuts_count; ++i ) {
      const util_shortcut * shortcut = This->shortcuts + i;
      if( shortcut->event == event ) {
         fire_action( This, This->on_exit, This->on_exit_count );
         This->current = shortcut->next;
         fire_action( This, This->on_entry, This->on_entry_count );
         return UTIL_NO_ERROR;
      }
   }
   for( unsigned i = 0; i < This->arcs_count; ++i ) {
      const util_transition * arc = This->arcs + i;
      if( arc->current == This->current && arc->event == event ) {
         fire_action( This, This->on_exit, This->on_exit_count );
         This->current = arc->next;
         fire_action( This, This->on_entry, This->on_entry_count );
         return UTIL_NO_ERROR;
      }
   }
   fprintf( stderr, "util_automaton_process|ignored event %d\n", event );
   return UTIL_NOT_FOUND;
}
