#include <util/automaton.h>
#include <stdio.h>

util_error util_automaton_init(
   util_automaton *        This,
   int                     initial,
   const util_transition * arcs,
   unsigned                arcs_count,
   const util_shortcut *   shortcuts,
   unsigned                shortcuts_count )
{
   if( ! This || ! arcs || ! shortcuts ) {
      return UTIL_NULL_ARG;
   }
   This->current         = initial;
   This->arcs            = arcs;
   This->arcs_count      = arcs_count;
   This->shortcuts       = shortcuts;
   This->shortcuts_count = shortcuts_count;
   return UTIL_NO_ERROR;
}

util_error util_automaton_process( util_automaton * This, int event ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   for( unsigned i = 0; i < This->shortcuts_count; ++i ) {
      const util_shortcut * shortcut = This->shortcuts + i;
      if( shortcut->event == event ) {
         This->current = shortcut->next;
         return UTIL_NO_ERROR;
      }
   }
   for( unsigned i = 0; i < This->arcs_count; ++i ) {
      const util_transition * arc = This->arcs + i;
      if( arc->current == This->current && arc->event == event ) {
         This->current = arc->next;
         return UTIL_NO_ERROR;
      }
   }
   fprintf( stderr, "util_automaton_process|ignored event %d\n", event );
   return UTIL_NOT_FOUND;
}
