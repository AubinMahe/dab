automaton( package, component, stateFullName, stateShortName, eventFullName, eventShortName ) ::= <<
package <package>;
// automaton

import <stateFullName>;
import <eventFullName>;

public class Automaton extends util.Automaton\<<stateShortName>, <eventShortName>\> {

   public Automaton( <component.name>Component component ) {
      super( <stateShortName>.<component.automaton.initial> );

      <component.automaton.transition:{transition|add( <
         stateShortName>.<transition.from>, <
         eventShortName>.<transition.event>, <
         stateShortName>.<transition.futur> );
}>
      <component.automaton.shortcut:{shortcut|add( <
         eventShortName>.<shortcut.event>, <
         stateShortName>.<shortcut.futur> );
}>
      <component.automaton.onEntryOrOnExit:{elt|add<elt.name.localPart; format="UpperCamelCase">( <
         stateShortName>.<elt.value.state>, component::<
         elt.value.action> );
}>   }
}

>>
