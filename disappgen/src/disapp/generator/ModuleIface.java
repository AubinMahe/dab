package disapp.generator;

import disapp.generator.model.InterfaceType;

class ModuleIface implements Comparable<ModuleIface> {

   private final String        _module;
   private final InterfaceType _iface;
   private final boolean       _data;
   private final String        _instance;

   ModuleIface( String module, InterfaceType iface, String instance, boolean data ) {
      _module   = module;
      _iface    = iface;
      _data     = data;
      _instance = instance;
   }

   ModuleIface( String module, InterfaceType iface, String instance ) {
      this( module, iface, instance, false );
   }

   @Override
   public int compareTo( ModuleIface o ) {
      return toString().compareTo( o.toString());
   }

   public String getModule() {
      return _module;
   }

   public InterfaceType getIface() {
      return _iface;
   }

   public boolean getData() {
      return _data;
   }

   public String getName() {
      return _iface.getName() + (_data ? "_data" : "" );
   }

   public String getInstance() {
      return _instance;
   }

   @Override
   public String toString() {
      return getName() + " for " + _instance;
   }
}
