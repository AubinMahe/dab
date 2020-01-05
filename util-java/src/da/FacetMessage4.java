package da;

import java.net.SocketAddress;

public class FacetMessage4<I extends Enum<I>, F extends Enum<F>, E extends InstanceID, A1, A2, A3, A4>
   extends
      FacetMessage3<I, F, E, A1, A2, A3>
{
   public final A4 _arg4;

   public FacetMessage4( SocketAddress from, I intrfc, F event, E instance, A1 arg1, A2 arg2, A3 arg3, A4 arg4 ) {
      super( from, intrfc, event, instance, arg1, arg2, arg3 );
      _arg4 = arg4;
   }

   public FacetMessage4( SocketAddress from, I intrfc, F event, E instance, E fromInstance, A1 arg1, A2 arg2, A3 arg3, A4 arg4 ) {
      super( from, intrfc, event, instance, fromInstance, arg1, arg2, arg3 );
      _arg4 = arg4;
   }
}
