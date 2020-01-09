dispatcherImplData() ::= <<
// dispatcherImplData
private boolean dispatch( <iface.name>Data event, FacetMessage\<Interfaces, ?, InstanceID> msg ) throws IOException {
   util.Log.printf( "event = %s", event );
   switch( event ) {
<data.(iface):{facet|
   case <facet.name; format="ID">:
< if( facet.threadingPolicyIsDedicated )>
      new Thread(() -> {
         try {
            _component.get<facet.name; format="cap">().set( msg.getArg1());
            _component.<facet.name; format="argument">Published();
         \}
         catch( Throwable t ) {
            t.printStackTrace();
         \}
      \}).start();
< else>
      _component.get<facet.name; format="cap">().set( msg.getArg1());
      _component.<facet.name; format="argument">Published();
< endif>
      return true;
}>   default:
      util.Log.printf( "Unexpected event: %s\n", event );
      return false;
   }
}
>>
