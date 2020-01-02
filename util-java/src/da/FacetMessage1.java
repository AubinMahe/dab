package da;

import java.net.SocketAddress;

public class FacetMessage1<I extends Enum<I>, F extends Enum<F>, T> extends FacetMessage<I, F > {

   public final T _arg1;

   public FacetMessage1( SocketAddress from, I intrfc, F event, byte instance, T arg1 ) {
      super( from, intrfc, event, instance );
      _arg1 = arg1;
   }
}
