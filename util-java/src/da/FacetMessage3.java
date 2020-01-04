package da;

import java.net.SocketAddress;

public class FacetMessage3<I extends Enum<I>, F extends Enum<F>, A1, A2, A3> extends FacetMessage2<I, F, A1, A2> {

   public final A3 _arg3;

   public FacetMessage3( SocketAddress from, I intrfc, F event, byte instance, A1 arg1, A2 arg2, A3 arg3 ) {
      super( from, intrfc, event, instance, arg1, arg2 );
      _arg3 = arg3;
   }
}
