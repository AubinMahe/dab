publisherBody( namespace, ifacesNamespace, interface, ifaceID, data, types ) ::= <<
// publisherBody

#include \<<namespace; format="NamespaceToPath">/<interface.name>Publisher.hpp>
#include \<<ifacesNamespace; format="NamespaceToPath">/<interface.name>Interface.hpp>

#include \<util/Log.hpp>

#include \<stdio.h>

using namespace <namespace>;

<interface.name>Publisher::<interface.name>Publisher(
   io::DatagramSocket &   socket,
   const da::InstanceID & instance,
   unsigned               targetsCount,
   da::Targets *          targets      )
 :
   _socket      ( socket              ),
   _instance    ( instance            ),
   _targetsCount( targetsCount        ),
   _out         ( _raw, sizeof( _raw )),
   _targets     ( targets             )
{}

<data:{dataType|
void <interface.name>Publisher::publish<dataType.name; format="cap">( const <types.(dataType.type)> & <dataType.name; format="argument"> ) {
   for( unsigned i = 0; i \< _targetsCount; ++i ) {
      da::Targets * targets = _targets + i;
      for( unsigned j = 0; j \< targets->instancesCount; ++j ) {
         const da::InstanceID & to = targets->instances[j];
         hpms::interfaces::<interface.name>Interface::<dataType.name; format="argument">( <dataType.name; format="argument">, _instance, to, _out );
         _socket.sendTo( _out, targets->process );
         unsigned count = (unsigned)_out.remaining();
         UTIL_LOG_ARGS( "%u byte(s) sent from instance %s to instance %s @ %s",
            count, _instance.toString(), to.toString(), ::inet_ntoa( targets->process.sin_addr ));
      \}
   \}
\}
}; separator="\n\n">
>>
