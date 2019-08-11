#pragma once

#include <initializer_list>
#include <map>
#include <string>
#include <iostream>
#include <sstream>

namespace util {

   template<class S, class E>
   class Automaton {
   public:

      /**
       * Ne sert qu'à peupler l'automate.
       */
      struct Arc {

         Arc( S c, E e, S n ) :
            current( c ),
            event  ( e ),
            next   ( n )
         {}

         S current;
         E event;
         S next;
      };

      /**
       * Ne sert qu'à peupler l'automate.
       */
      struct Shortcut {

         Shortcut( E e, S n ) :
            event( e ),
            next ( n )
         {}

         E event;
         S next;
      };

   public:

      Automaton( S initial, std::initializer_list<Arc> arcs, std::initializer_list<Shortcut> shortcuts ) :
         _current( initial )
      {
         for( auto t : arcs ) {
            _automaton[t.current][t.event] = t.next;
         }
         for( auto t : shortcuts ) {
            for( auto p : _automaton ) {
               _automaton[p.first][t.event] = t.next;
            }
         }
      }

   public:

      S getCurrentState( void ) const {
         return _current;
      }

      void process( E event ) {
         const std::map<E, S> & tr = _automaton[_current];
         auto it = tr.find( event );
         if( it == tr.end()) {
            std::stringstream ss;
            ss << "util.Automaton.process|unexpected event: " << event << " from state: " << _current;
            throw std::runtime_error( ss.str());
         }
         _current = it -> second;
         std::cerr << "Nouvel état : " << _current << std::endl;
      }

   private:

      S                           _current;
      std::map<S, std::map<E, S>> _automaton;
   };
}
