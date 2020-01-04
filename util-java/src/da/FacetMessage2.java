package da;

import java.net.SocketAddress;

public class FacetMessage2<I extends Enum<I>, F extends Enum<F>, A1, A2> extends FacetMessage1<I, F, A1> {

   public final A2 _arg2;

   public FacetMessage2( SocketAddress from, I intrfc, F event, byte instance, A1 arg1, A2 arg2 ) {
      super( from, intrfc, event, instance, arg1 );
      _arg2 = arg2;
   }
}
