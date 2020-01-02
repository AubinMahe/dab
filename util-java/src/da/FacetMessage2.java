package da;

import java.net.SocketAddress;

public class FacetMessage2<I extends Enum<I>, F extends Enum<F>, U, V> extends FacetMessage1<I, F, U> {

   public final V _arg2;

   public FacetMessage2( SocketAddress from, I intrfc, F event, byte instance, U arg1, V arg2 ) {
      super( from, intrfc, event, instance, arg1 );
      _arg2 = arg2;
   }
}
