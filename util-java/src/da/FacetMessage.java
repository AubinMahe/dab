package da;

import java.net.SocketAddress;

public class FacetMessage<I extends Enum<I>, F extends Enum<F>, P> {

   public final SocketAddress _from;
   public final I             _interface;
   public final F             _event;
   public final InstanceID    _instance;
   public final InstanceID    _fromInstance; // Requester instance ID
   public final P             _payload;

   public FacetMessage( SocketAddress from, I intrfc, F event, InstanceID instance, InstanceID fromInstance, P payload ) {
      _from         = from;
      _interface    = intrfc;
      _event        = event;
      _instance     = instance;
      _fromInstance = fromInstance;
      _payload      = payload;
   }

   public FacetMessage( SocketAddress from, I intrfc, F event, InstanceID instance, InstanceID fromInstance ) {
      this( from, intrfc, event, instance, fromInstance, null );
   }

   public FacetMessage( FacetMessage<I, F, P> right ) {
      this( right._from, right._interface, right._event, right._instance, right._fromInstance, right._payload );
   }
}
