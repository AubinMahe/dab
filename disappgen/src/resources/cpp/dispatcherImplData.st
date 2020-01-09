dispatcherImplData() ::= <<
// dispatcherImplData -- begin
bool <component.name>Dispatcher::dispatch( <iface.name>Data event, io::ByteBuffer & in ) {
   switch( event ) {
<data.(iface):{dataType|
   case <iface.name>Data::<dataType.name; format="ID">:{
      _listener.get<dataType.name; format="cap">().get( in );
      _listener.<dataType.name>Published();
      return true;\}
}>   default:
      UTIL_LOG_ARGS( "Unexpected event: %d", (byte)event );
      return false;
   }
}
// dispatcherImplData -- end
>>
