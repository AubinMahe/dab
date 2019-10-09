#pragma once

#include <stdio.h>
#include <stdlib.h>

#include <stdexcept>

namespace util {

   template<class A, class S, class E>
   class Automaton {
   public:

      Automaton( A & actor, S initial ) :
         _actor  ( actor   ),
         _current( initial )
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
                  char msg[200];
                  sprintf( msg, "util.Automaton.process|unexpected event: %d from state: %d\n", (int)event, (int)_current );
                  throw std::runtime_error( msg );
               }
               futur = tr->futur;
            }
         }
         {
            Action   key = { _current, 0 };
            Action * tr  = search( _onExits, _onExCount, key );
            if( tr ) {
               (_actor.*(tr->action))();
            }
         }
         _current = futur;
#ifndef NDEBUG
         fprintf( stderr, "util.Automaton.process|new State: %d\n", (int)_current );
#endif
         {
            Action   key = { _current, 0 };
            Action * tr  = search( _onEntries, _onEnCount, key );
            if( tr ) {
               (_actor.*(tr->action))();
            }
         }
      }

   private:

      A &          _actor;
      S            _current;
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
