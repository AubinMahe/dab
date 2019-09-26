package util;

import java.util.HashMap;
import java.util.Map;

public class Automaton<S, E> {

   private final Map<S, Map< E, S>> _transitions = new HashMap<>();
   private /* */ S                  _current;

   public Automaton( S initial ) {
      _current = initial;
   }

   protected void add( S from, E event, S futur ) {
      Map<E, S> transition = _transitions.get( from );
      if( transition == null ) {
         _transitions.put( from, transition = new HashMap<>());
      }
      transition.put( event, futur );
   }

   protected void add( E event, S futur ) {
      for( final Map<E, S> p : _transitions.values()) {
         p.put( event, futur );
      }
   }

   public boolean process( E event ) {
      final Map<E, S> transition = _transitions.get( _current );
      if( transition != null ) {
         final S futur = transition.get( event );
         if( futur != null ) {
            _current = futur;
            return true;
         }
      }
      return false;
   }

   public S getCurrentState() {
      return _current;
   }
}
