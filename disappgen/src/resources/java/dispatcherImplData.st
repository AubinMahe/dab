dispatcherImplData() ::= <<
// dispatcherImplData
private boolean dispatch<iface.name; format="cap">( FacetMessage\<Interfaces, <iface.name>Interface.Facets, ?> msg ) throws IOException {
   util.Log.printf( "event = %s", msg._event );
   switch( msg._event ) {
<data.(iface):{facet|
   case <facet.name; format="ID">:
      @SuppressWarnings("unchecked")
      final FacetMessage\<Interfaces, <iface.name>Interface.Facets, <types.(facet.type)>\> m =
         (FacetMessage\<Interfaces, <iface.name>Interface.Facets, <types.(facet.type)>\>)msg;
< if( facet.threadingPolicyIsDedicated )>
      new Thread(() -> {
         try {
            _component.get<facet.name; format="cap">().set( m._payload );
            _component.<facet.name; format="argument">Published();
         \}
         catch( Throwable t ) {
            t.printStackTrace();
         \}
      \}).start();
< else>
      _component.get<facet.name; format="cap">().set( m._payload );
      _component.<facet.name; format="argument">Published();
< endif>
      return true;
}>   default:
      util.Log.printf( "Unexpected event: %s\n", msg._event );
      return false;
   }
}
>>
