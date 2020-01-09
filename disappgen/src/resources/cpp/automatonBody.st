automatonBody( namespace, component, types ) ::= <<
#include "Automaton.hpp"
#include \<<namespace; format="NamespaceToPath">/<component.name>Component.hpp>

using namespace <namespace>;

Automaton :: Automaton( <component.name>Component & component ) :
   util::Automaton\<<
      component.name>Component, <
      types.(component.automaton.stateEnum.name)>, <
      types.(component.automaton.eventEnum.name)>\>( component, <
         types.(component.automaton.stateEnum.name)>::<component.automaton.initial> )
{
   static Transition transitions[] = {<component.automaton.transition:{tr|

      { <types.(component.automaton.stateEnum.name)>::<tr.from>, <
         types.(component.automaton.eventEnum.name)>::<tr.event>, <
         types.(component.automaton.stateEnum.name)>::<tr.futur> \},
}>   };
   setTransitions( transitions, sizeof(transitions)/sizeof(transitions[0]));

   static Shortcut shortcuts[] = {<component.automaton.shortcut:{shortcut|

      { <types.(component.automaton.eventEnum.name)>::<shortcut.event>, <
         types.(component.automaton.stateEnum.name)>::<shortcut.futur> \},
}>   };
   setShortcuts( shortcuts, sizeof(shortcuts)/sizeof(shortcuts[0]));

   static Action onEntries[] = {<component.automaton.onEntryOrOnExit:{elt|< if( elt.isOnEntry )>
      { <types.(component.automaton.stateEnum.name)>::<elt.value.state>, &<component.name>Component::<elt.value.action> \},< endif>
}>
   };
   setOnEntries( onEntries, sizeof(onEntries)/sizeof(onEntries[0]));

   static Action onExits[] = {<component.automaton.onEntryOrOnExit:{elt|< if( elt.isOnExit )>
      { <types.(component.automaton.stateEnum.name)>::<elt.value.state>, &<component.name>Component::<elt.value.action> \},< endif>
}>
   };
   setOnExits( onExits, sizeof(onExits)/sizeof(onExits[0]));
}

>>
