dispatcherImplOneInterface() ::= <<
// dispatcherImplOneInterface
private boolean dispatch<iface.name; format="cap">( FacetMessage\<Interfaces, <iface.name>Interface.Facets, ?> msg ) throws IOException {
   util.Log.printf( "event = %s", msg._event );
   switch( msg._event ) {
<events.(iface.name):{facet|<
if( facet.isEvent )>
   case <facet.name; format="ID">:{
< if( facet.field )>
      @SuppressWarnings("unchecked")
      final FacetMessage\<Interfaces, <iface.name>Interface.Facets, <iface.name>Interface.<facet.name; format="cap">\> m =
         (FacetMessage\<Interfaces, <iface.name>Interface.Facets, <iface.name>Interface.<facet.name; format="cap">\>)msg;
<facet.field:{field|       <common/copyMsgOnStack( field )>}>< endif><
 if( facet.threadingPolicyIsDedicated )
>      new Thread(() -> {
         try {
            _component.<facet.name>(<common/args( facet.field )>);
         \}
         catch( Throwable t ) {
            t.printStackTrace();
         \}
      \}).start();
< else
>      _component.<facet.name>(<common/args( facet.field )>);
< endif>
      return true; \}<
elseif( facet.isData )><
elseif( facet.isRequest )>
   case <facet.name; format="ID">:{
      @SuppressWarnings("unchecked")
      final FacetMessage\<Interfaces, <iface.name>Interface.Facets, <iface.name>Interface.<facet.name; format="cap">\> m =
         (FacetMessage\<Interfaces, <iface.name>Interface.Facets, <iface.name>Interface.<facet.name; format="cap">\>)msg;
<facet.arguments.field:{field|       <common/copyMsgOnStack( field )>}><
 if( facet.threadingPolicyIsDedicated )
>      new Thread(() -> {
         try {
            <dispatcherImplOneRequest()>
         \}
         catch( final Throwable t ) {
            t.printStackTrace();
         \}
      \}).start();
< else
>      <dispatcherImplOneRequest()>
< endif>
      return true;\}
   case <facet.name; format="ID">_RESPONSE:
      util.Log.printf( "Unexpected event: %s", msg._event );
      return false;<
endif>}>
   case FIRST:
   case LAST:
   default:
      util.Log.printf( "Unexpected event: %s", msg._event );
      return false;
   }
}
>>
