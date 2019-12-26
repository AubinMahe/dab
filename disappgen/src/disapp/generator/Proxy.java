package disapp.generator;

import java.util.Map;
import java.util.SortedSet;

class Proxy implements Comparable<Proxy> {

   private final String                         _module;
   private final String                         _iface;
   private final String                         _from;
   private final Map<String, SortedSet<String>> _to;

   Proxy( String module, String iface, String from, Map<String, SortedSet<String>> to ) {
      _module = module;
      _iface  = iface;
      _from   = from;
      _to     = to;
   }

   public String getModule() {
      return _module;
   }

   public String getInterface() {
      return _iface;
   }

   public String getFrom() {
      return _from;
   }

   public Map<String, SortedSet<String>> getTo() {
      return _to;
   }

   @Override
   public int compareTo( Proxy o ) {
      return toString().compareTo( o.toString());
   }

   @Override
   public String toString() {
      return _module + _iface + _from;
   }
}
