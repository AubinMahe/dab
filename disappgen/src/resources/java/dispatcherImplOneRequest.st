dispatcherImplOneRequest() ::= <<
final <types.(facet.type)> <facet.name> = new <types.(facet.type)>();
_component.<facet.name>(<common/args( facet.arguments.field )>, <facet.name> );
<iface.name>Interface.<facet.name>( <facet.name>, _component._instanceID, msg._fromInstance, _out );
final int size = _out.remaining();
_channel.send( _out, msg._from );
util.Log.printf( "%d byte(s) sent from instance %s to instance %s @ %s",
   size, _component._instanceID, msg._fromInstance, msg._from );
>>
