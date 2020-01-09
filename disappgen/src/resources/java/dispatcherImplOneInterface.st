dispatcherImplOneInterface() ::= <<
// dispatcherImplOneInterface
private boolean dispatch( <iface.name>Event event, FacetMessage\<Interfaces, ?, InstanceID> msg ) throws IOException {
   util.Log.printf( "event = %s", event );
   switch( event ) {
<events.(iface.name):{facet|<
if( facet.isData )><
elseif( facet.isRequest )>
   case <facet.name; format="ID">:{
< if( facet.threadingPolicyIsDedicated )>
      new Thread(() -> {
         try {
            <dispatcherImplOneRequest()>
         \}
         catch( final Throwable t ) {
            t.printStackTrace();
         \}
      \}).start();
< else>
      <dispatcherImplOneRequest()>
< endif>
      return true;\}<
else>
   case <facet.name; format="ID">:
< if( facet.threadingPolicyIsDedicated )>
      new Thread(() -> {
         try {
            _component.<facet.name>(<common/args( facet.field )>);
         \}
         catch( Throwable t ) {
            t.printStackTrace();
         \}
      \}).start();
< else>
      _component.<facet.name>(<common/args( facet.field )>);
< endif>
      return true;<
endif>}>
   default:
      util.Log.printf( "Unexpected event: %s", event );
      return false;
   }
}
>>
