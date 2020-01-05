package da;

import java.net.SocketAddress;

public class FacetMessage1<I extends Enum<I>, F extends Enum<F>, E extends Enum<E>, A1>
   extends
      FacetMessage<I, F, E>
{
   public final A1 _arg1;

   public FacetMessage1( SocketAddress from, I intrfc, F event, E instance, A1 arg1 ) {
      super( from, intrfc, event, instance );
      _arg1 = arg1;
   }

   public FacetMessage1( SocketAddress from, I intrfc, F event, E instance, E fromInstance, A1 arg1 ) {
      super( from, intrfc, event, instance, fromInstance );
      _arg1 = arg1;
   }
}
