requiredImplementationBody( namespace, usedTypes, ifaceName, rawSize, iface, ifaceID ) ::= <<
//requiredImplementation

#include \<<namespace; format="NamespaceToPath">/<iface.name>.hpp>
#include \<util/Log.hpp>

using namespace <namespace>;

<iface.name>::<iface.name>( io::DatagramSocket & socket, byte instance, unsigned targetsCount, da::Targets * targets ) :
   _socket      ( socket ),
   _instance    ( instance ),
   _targetsCount( targetsCount ),
   _out         ( _raw, <rawSize> ),
   _targets     ( targets )
{}
<iface.eventOrRequestOrData:{facet|< if( facet.isData )>< else>

void <iface.name>::<facet.name>( <
if( facet.isRequest )><
    common/signature( facet.arguments.field )><
else><
    common/signature( facet.field )><
endif> ) {
   UTIL_LOG_HERE();
   for( unsigned i = 0; i \< _targetsCount; ++i ) {
      da::Targets * targets = _targets + i;
      for( unsigned j = 0; j \< targets->instancesCount; ++j ) {
         byte to = targets->instances[j];
         _out.clear();
         _out.putByte( <iface.name; format="ID">_ID );
         _out.putByte( <facet.name; format="ID"> );
         _out.putByte( to );
         <
if( facet.isRequest )
        >_out.putByte( _instance );
<        common/byteBufferPut( "" , facet.arguments.field )><
else><   common/byteBufferPut( "" , facet.field )><
endif>
         _out.flip();
         _socket.sendTo( _out, targets->process );
         UTIL_LOG_ARGS( "%u byte(s) sent from instance %d to instance %d @ %s",
            (unsigned)_out.remaining(), _instance, to, ::inet_ntoa( targets->process.sin_addr ));
      \}
   \}
\}
< endif>
}>

>>
