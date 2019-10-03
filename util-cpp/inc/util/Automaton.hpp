#pragma once

#include <initializer_list>
#include <map>
#include <string>
#include <iostream>
#include <sstream>

namespace util {

   template<class A, class S, class E>
   class Automaton {
   public:

      Automaton( A & actor, S initial ) :
         _actor  ( actor   ),
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

      typedef void (A:: * action_t)( void );

      void addOnEntry( S state, action_t onEntryAction ) {
         _onEntries[state] = onEntryAction;
      }

      void addOnExit( S state, action_t onExitAction ) {
         _onExits[state] = onExitAction;
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
         {
            const auto & it = _onExits.find( _current );
            if( it != _onExits.end()) {
               const action_t & action = it->second;
               (_actor.*action)();
            }
         }
         _current = it -> second;
         std::cerr << "Nouvel état : " << _current << std::endl;
         {
            const auto & it = _onEntries.find( _current );
            if( it != _onEntries.end()) {
               const action_t & action = it->second;
               (_actor.*action)();
            }
         }
      }

   private:

      A                         & _actor;
      S                           _current;
      std::map<S, std::map<E, S>> _transitions;
      std::map<S, action_t>       _onEntries;
      std::map<S, action_t>       _onExits;
   };
}
