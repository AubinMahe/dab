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

      Automaton( S initial ) :
         _current( initial )
      {}

   protected:

      void add( S from, E event, S futur ) {
         _transitions[from][event] = futur;
      }

      void add( E event, S futur ) {
         for( auto p : _transitions ) {
            _transitions[p.first][event] = futur;
         }
      }

   public:

      S getCurrentState( void ) const {
         return _current;
      }

      void process( E event ) {
         const std::map<E, S> & tr = _transitions[_current];
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
      std::map<S, std::map<E, S>> _transitions;
   };
}
