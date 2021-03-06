#pragma once

#include <os/win32.hpp>

#include <stdio.h>
#include <stdlib.h>

#include "Exceptions.hpp"
#include "Log.hpp"

namespace util {

   template<class A, class S, class E>
   class Automaton {
   public:

      Automaton( A & actor, S initial ) :
         _actor  ( actor   ),
         _current( initial ),
         _debug  ( false   )
      {}

   protected:

      typedef int ( * comparator_t )( const void *, const void * );

      template<class T>
      void sort( T * t, unsigned count ) {
         ::qsort( t, count, sizeof( T ), (comparator_t)T::comparator );
      }

      template<class T>
      T * search( T * t, unsigned count, T & key ) {
         return (T *)::bsearch( &key, t, count, sizeof( T ), (comparator_t)T::comparator );
      }

      struct Transition {
         S from;
         E event;
         S futur;

         static int comparator( const Transition * left, const Transition * right ) {
            int diff = (int)left->from - (int)right->from;
            if( diff == 0 ) {
               diff = (int)left->event - (int)right->event;
            }
            return diff;
         }
      };

      void setTransitions( Transition * transitions, unsigned count ) {
         _transitions = transitions;
         _trCount     = count;
         sort( _transitions, count );
      }

      struct Shortcut {
         E event;
         S futur;

         static int comparator( const Shortcut * left, const Shortcut * right ) {
            int diff = (int)left->event - (int)right->event;
            return diff;
         }
      };

      void setShortcuts( Shortcut * shortcuts, unsigned count ) {
         _shortcuts = shortcuts;
         _shCount   = count;
         sort( _shortcuts, count );
      }

      typedef void (A:: * action_t)( void );

      struct Action {
         S        state;
         action_t action;

         static int comparator( const Action * left, const Action * right ) {
            int diff = (int)left->state - (int)right->state;
            return diff;
         }
      };

      void setOnEntries( Action * onEntries, unsigned count ) {
         _onEntries = onEntries;
         _onEnCount = count;
         sort( _onEntries, count );
      }

      void setOnExits( Action * onExits, unsigned count ) {
         _onExits   = onExits;
         _onExCount = count;
         sort( _onExits, count );
      }

   public:

      S getCurrentState( void ) const {
         return _current;
      }

      void setDebug( bool dbg ) {
         _debug = dbg;
      }

      void process( E event ) {
         S futur;
         {
            Shortcut   skey = { event, S()};
            Shortcut * sh   = search( _shortcuts, _shCount, skey );
            if( sh ) {
               futur = sh->futur;
            }
            else {
               Transition   key = { _current, event, S()};
               Transition * tr  = search( _transitions, _trCount, key );
               if( ! tr ) {
                  throw Unexpected( UTIL_CTXT, "event: %d from state: %s\n", (int)event, toString( _current ));
               }
               futur = tr->futur;
            }
         }
         {
            Action   key = { _current, 0 };
            Action * tr  = search( _onExits, _onExCount, key );
            if( tr ) {
               if( _debug ) {
                  UTIL_LOG_ARGS( "on_exit(%s) fired", toString( _current ));
               }
               (_actor.*(tr->action))();
            }
         }
         _current = futur;
         if( _debug ) {
            UTIL_LOG_ARGS( "new State: %s", toString( _current ));
         }
         {
            Action   key = { _current, 0 };
            Action * tr  = search( _onEntries, _onEnCount, key );
            if( tr ) {
               if( _debug ) {
                  UTIL_LOG_ARGS( "on_entry(%s) fired", toString( _current ));
               }
               (_actor.*(tr->action))();
            }
         }
      }

   private:

      A &          _actor;
      S            _current;
      bool         _debug;
      Transition * _transitions;
      unsigned     _trCount;
      Shortcut *   _shortcuts;
      unsigned     _shCount;
      Action *     _onEntries;
      unsigned     _onEnCount;
      Action *     _onExits;
      unsigned     _onExCount;
   };
}
