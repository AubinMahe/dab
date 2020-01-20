dispatcherImplResponse() ::= <<
// dispatcherImplResponse
private boolean dispatch<ifaceName; format="cap">( FacetMessage\<Interfaces, <ifaceName>Interface.Facets, ?> msg ) throws IOException {
   util.Log.printf( "event = %s", msg._event );
   switch( msg._event ) {
<reqRequests.(ifaceName):{facet|
   case <facet.name; format="ID">_RESPONSE:
      @SuppressWarnings("unchecked")
      final FacetMessage\<Interfaces, <ifaceName>Interface.Facets, <types.(facet.type)>\> m =
         (FacetMessage\<Interfaces, <ifaceName>Interface.Facets, <types.(facet.type)>\>)msg;
      _component.<facet.name>Response( m._payload );
      return true;
}>   case FIRST:
   case LAST:
   default:
      util.Log.printf( "Unexpected event: %s", msg._event );
      return false;
   }
}
>>
