package da;

import java.net.SocketAddress;

public class FacetMessage1<I extends Enum<I>, F extends Enum<F>, A1> extends FacetMessage<I, F > {

   public final A1 _arg1;

   public FacetMessage1( SocketAddress from, I intrfc, F event, byte instance, A1 arg1 ) {
      super( from, intrfc, event, instance );
      _arg1 = arg1;
   }
}
