package util;

import java.util.HashMap;
import java.util.Map;

public class Automaton<S, E> {

   private final Map<S, Map< E, S>> _arcs      = new HashMap<>();
   private final Map< E, S>         _shortcuts = new HashMap<>();
   private /* */ S                  _current;

   public Automaton( S initial, Arc<S, E>[] arcs, Shortcut<S, E>[] shortcuts ) {
      _current = initial;
      for( final Arc<S, E> arc : arcs ) {
         Map< E, S> transition = _arcs.get( arc.current );
         if( transition == null ) {
            _arcs.put( arc.current, transition = new HashMap<>());
         }
         transition.put( arc.event, arc.futur );
      }
      for( final Shortcut<S, E> s : shortcuts ) {
         _shortcuts.put( s.event, s.futur );
      }
   }

   public static final class Arc<S, E> {

      public Arc( S c, E e, S f ) {
         current = c;
         event   = e;
         futur   = f;
      }

      private final S current;
      private final E event;
      private final S futur;
   }

   public static final class Shortcut<S, E> {

      public Shortcut( E e, S f ) {
         event = e;
         futur = f;
      }

      private final E event;
      private final S futur;
   }

   public boolean process( E event ) {
      S futur = _shortcuts.get( event );
      if( futur != null ) {
         _current = futur;
         return true;
      }
      final Map<E, S> transition = _arcs.get( _current );
      if( transition != null ) {
         futur = transition.get( event );
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
