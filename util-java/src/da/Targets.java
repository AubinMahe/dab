package da;

import java.net.InetSocketAddress;

public final class Targets<T extends InstanceID> {

   public final InetSocketAddress _process;
   public final T[]              _instances;

   @SafeVarargs
   public Targets( InetSocketAddress process, T ... instances ) {
      _process   = process;
      _instances = instances;
   }
}
