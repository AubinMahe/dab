automatonHeader( namespace, component, types ) ::= <<
#pragma once

#include \<util/Automaton.hpp>

#include \<<types.(component.automaton.stateEnum.name); format="NamespaceToPath">.hpp>
#include \<<types.(component.automaton.eventEnum.name); format="NamespaceToPath">.hpp>

namespace <namespace> {

   class <component.name>Component;
   class Automaton : public util::Automaton\<<
      component.name>Component, <
      types.(component.automaton.stateEnum.name)>, <
      types.(component.automaton.eventEnum.name)>\> {
   public:

      Automaton( <component.name>Component & component );

   private:
      Automaton( const Automaton & ) = delete;
      Automaton & operator = ( const Automaton & ) = delete;
   };
}

>>
