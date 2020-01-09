dispatcherImplResponse() ::= <<
// dispatcherImplResponse
private boolean dispatch( <ifaceName>Request event, FacetMessage\<Interfaces, ?, InstanceID> msg ) throws IOException {
   util.Log.printf( "event = %s", event );
   switch( event ) {
<reqRequests.(ifaceName):{facet|
   case <facet.name; format="ID">:
      _component.<facet.name>Response(<common/args( facet.arguments.field )>);
      return true;
}>   default:
      util.Log.printf( "Unexpected event: %s", event );
      return false;
   }
}
>>
