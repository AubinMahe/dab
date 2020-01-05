package da;

import java.net.SocketAddress;

public class FacetMessage2<I extends Enum<I>, F extends Enum<F>, E extends InstanceID, A1, A2>
   extends
      FacetMessage1<I, F, E, A1>
{
   public final A2 _arg2;

   public FacetMessage2( SocketAddress from, I intrfc, F event, E instance, A1 arg1, A2 arg2 ) {
      super( from, intrfc, event, instance, arg1 );
      _arg2 = arg2;
   }

   public FacetMessage2( SocketAddress from, I intrfc, F event, E instance, E fromInstance, A1 arg1, A2 arg2 ) {
      super( from, intrfc, event, instance, fromInstance, arg1 );
      _arg2 = arg2;
   }
}
