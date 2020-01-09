dataWriterBody( namespace, interface, ifaceID, data, dataID, rawSize, types ) ::= <<
// dataWriterBody
#include \<<namespace; format="NamespaceToPath">/<interface.name>Data.hpp>

#include \<util/Log.hpp>

#include \<stdio.h>

using namespace <namespace>;

static const byte <interface.name; format="ID"> = <ifaceID>;

<data:{dataType|static const byte <dataType.name; format="ID"> = <dataID.(dataType.name)>;
}>
<interface.name>Data::<interface.name>Data( io::DatagramSocket & socket, byte instance, unsigned targetsCount, da::Targets * targets ) :
   _socket      ( socket              ),
   _instance    ( instance            ),
   _targetsCount( targetsCount        ),
   _out         ( _raw, sizeof( _raw )),
   _targets     ( targets             )
{}

<data:{dataType|
void <interface.name>Data::publish<dataType.name; format="cap">( const <types.(dataType.type)> & <dataType.name; format="argument"> ) {
   for( unsigned i = 0; i \< _targetsCount; ++i ) {
      da::Targets * targets = _targets + i;
      for( unsigned j = 0; j \< targets->instancesCount; ++j ) {
         byte to = targets->instances[j];
         _out.clear();
         _out.putByte( <interface.name; format="ID"> );
         _out.putByte( <dataType.name; format="ID"> );
         _out.putByte( to );
         <dataType.name; format="argument">.put( _out );
         _out.flip();
         _socket.sendTo( _out, targets->process );
         UTIL_LOG_ARGS( "%u byte(s) sent from instance %d to instance %d @ %s",
            (unsigned)_out.remaining(), _instance, to, ::inet_ntoa( targets->process.sin_addr ));
      \}
   \}
\}
}; separator="\n\n">

>>
