requiredImplementationBody( namespace, ifacesNamespace, iface ) ::= <<
// requiredImplementation

#include \<<namespace; format="NamespaceToPath">/<iface.name>.hpp>
#include \<<ifacesNamespace; format="NamespaceToPath">/<iface.name>Interface.hpp>
#include \<util/Log.hpp>

using namespace <namespace>;

<iface.name>::<iface.name>(
   io::DatagramSocket &   socket,
   const da::InstanceID & instance,
   unsigned               targetsCount,
   da::Targets *          targets      )
 :
   _socket      ( socket ),
   _instance    ( instance ),
   _targetsCount( targetsCount ),
   _out         ( _raw, sizeof( _raw )),
   _targets     ( targets )
{}
<iface.eventOrRequestOrData:{facet|< if( facet.isData )>< else>

void <iface.name>::<facet.name>( <
if( facet.isRequest )><
    signature( facet.arguments.field )><
else><
    signature( facet.field )><
endif> ) {
   UTIL_LOG_HERE();
   for( unsigned i = 0; i \< _targetsCount; ++i ) {
      da::Targets * targets = _targets + i;
      for( unsigned j = 0; j \< targets->instancesCount; ++j ) {
         const da::InstanceID & to = targets->instances[j];
         hpms::interfaces::<iface.name>Interface::<facet.name; format="argument">(<
if( facet.isRequest )><
    args( facet.arguments.field )>< if( facet.arguments.field )>, < endif><
else><
    args( facet.field )>< if( facet.field )>,< endif>< endif> _instance, to, _out );
         unsigned count = (unsigned)_out.remaining();
         _socket.sendTo( _out, targets->process );
         UTIL_LOG_ARGS( "%u byte(s) sent from instance %s to instance %s @ %s",
            count, _instance.toString(), to.toString(), ::inet_ntoa( targets->process.sin_addr ));
      \}
   \}
\}
< endif>
}>

>>
