package da;

import java.net.SocketAddress;

public class FacetMessage<I extends Enum<I>, F extends Enum<F>, E extends Enum<E>> {

   public final SocketAddress _from;
   public final I             _interface;
   public final F             _event;
   public final E             _instance;
   public final E             _fromInstance; // Requester instance ID

   public FacetMessage( SocketAddress from, I intrfc, F event, E instance ) {
      _from         = from;
      _interface    = intrfc;
      _event        = event;
      _instance     = instance;
      _fromInstance = null;
   }

   public FacetMessage( SocketAddress from, I intrfc, F event, E instance, E fromInstance ) {
      _from         = from;
      _interface    = intrfc;
      _event        = event;
      _instance     = instance;
      _fromInstance = fromInstance;
   }

   public <T> T getArg1() {
      @SuppressWarnings("unchecked")
      final FacetMessage1<?, ?, ?, T> message = (FacetMessage1<?, ?, ?, T>)this;
      return message._arg1;
   }

   public <T> T getArg2() {
      @SuppressWarnings("unchecked")
      final FacetMessage2<?, ?, ?, ?, T> message = (FacetMessage2<?, ?, ?, ?, T>)this;
      return message._arg2;
   }

   public <T> T getArg3() {
      @SuppressWarnings("unchecked")
      final FacetMessage3<?, ?, ?, ?, ?, T> message = (FacetMessage3<?, ?, ?, ?, ?, T>)this;
      return message._arg3;
   }

   public <T> T getArg4() {
      @SuppressWarnings("unchecked")
      final FacetMessage4<?, ?, ?, ?, ?, ?, T> message = (FacetMessage4<?, ?, ?, ?, ?, ?, T>)this;
      return message._arg4;
   }
}
