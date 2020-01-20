timeoutInterface( package, timeouts ) ::= <<
package <package>;
// timeoutInterface

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import da.FacetMessagesQueue;
import da.InstanceID;

public class TimeoutInterface {

   public enum Facets {
      FIRST,

      <timeouts:{timeout|<timeout; format="ID">,
}>
      LAST;

      public void put( ByteBuffer target ) {
         target.put((byte)ordinal());
      }

      public static Facets valueOf( int value ) {
         if( FIRST.ordinal() \< value && value \< Facets.LAST.ordinal()) {
            return values()[value];
         }
         return null;
      }
   }

   public static FacetMessagesQueue\<Interfaces> _queue = new FacetMessagesQueue\<>( "Timeout", < length( timeouts )>, true );

   public static void update( Facets facet, InstanceID instance, ByteBuffer target ) {
      target.clear();
      Interfaces.TIMEOUT.put( target );
      facet             .put( target );
      instance          .put( target );
      instance          .put( target );
      target.flip();
   }

   public static boolean enqueue(
      SocketAddress from,
      byte          event,
      InstanceID    instance )
   {
      final Facets evt = Facets.valueOf( event );
      if( evt == null ) {
         util.Log.printf( "%d is out of range", event );
      }
      else {
         switch( evt ) {
<timeouts:{timeout|
         case <timeout; format="ID">:{
            _queue.add( new da.FacetMessage\<>( from, Interfaces.TIMEOUT, evt, instance, instance ));
            return true; \}
}>         case FIRST:
         case LAST:
         default:
            util.Log.printf( "Unexpected timeout: %s", evt );
            break;
         }
      }
      return false;
   }
}

>>