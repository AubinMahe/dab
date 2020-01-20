dispatcherImplTimeout() ::= <<
// dispatcherImplTimeout

private boolean dispatchTimeout( TimeoutInterface.Facets event ) throws IOException {
   switch( event ) {
<component.timeout:{to|
   case <component.name; format="ID">_<to.name; format="ID">:{
      _component.<to.name; format="argument">Elapsed();
      return true;\}
}>   case FIRST:
   case LAST:
   default:
      util.Log.printf( "Unexpected event: %s", event );
      return false;
   }
}

void timeout( TimeoutInterface.Facets event ) throws IOException {
   final ByteBuffer out = ByteBuffer.allocate( 4 );
   Interfaces.TIMEOUT    .put( out );
   event                 .put( out );
   _component._instanceID.put( out );
   _component._instanceID.put( out );
   out.flip();
   _channel.send( out, _channel.getLocalAddress());
}
>>
