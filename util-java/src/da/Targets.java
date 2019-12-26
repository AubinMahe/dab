package da;

import java.net.InetSocketAddress;

public final class Targets {

   public final InetSocketAddress _process;
   public final byte[]            _instances;

   public Targets( InetSocketAddress process, byte ... instances ) {
      _process = process;
      _instances = instances;
   }
}
