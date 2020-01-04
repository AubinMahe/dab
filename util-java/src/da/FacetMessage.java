package da;

import java.net.SocketAddress;

public class FacetMessage<I extends Enum<I>, F extends Enum<F>> {

   public final SocketAddress _from;
   public final I             _interface;
   public final F             _event;
   public final byte          _instance;
   public /* */ byte          _fromInstance; // Requester instance ID

   public FacetMessage( SocketAddress from, I intrfc, F event, byte instance ) {
      _from      = from;
      _interface = intrfc;
      _event     = event;
      _instance  = instance;
   }

   public <T> T getArg1() {
      @SuppressWarnings("unchecked")
      final FacetMessage1<?, ?, T> message = (FacetMessage1<?, ?, T>)this;
      return message._arg1;
   }

   public <T> T getArg2() {
      @SuppressWarnings("unchecked")
      final FacetMessage2<?, ?, ?, T> message = (FacetMessage2<?, ?, ?, T>)this;
      return message._arg2;
   }
}
