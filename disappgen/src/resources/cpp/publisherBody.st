publisherBody( namespace, ifacesNamespace, iface, data, rawSize, types ) ::= <<
// publisherBody

#include \<<namespace; format="NamespaceToPath">/<iface.name>Publisher.hpp>
#include \<<ifacesNamespace; format="NamespaceToPath">/<iface.name>Interface.hpp>

#include \<os/Thread.hpp>
#include \<util/Log.hpp>

#include \<stdio.h>

using namespace <namespace>;

<iface.name>Publisher::<iface.name>Publisher(
   io::DatagramSocket &   socket,
   const da::InstanceID & instance,
   unsigned               targetsCount,
   da::Targets *          targets      )
 :
   _socket      ( socket       ),
   _instance    ( instance     ),
   _targetsCount( targetsCount ),
   _targets     ( targets      )
{}

<data:{facet|
void <iface.name>Publisher::publish<facet.name; format="cap">( const <types.(facet.type)> & <facet.name; format="argument"> ) {
< if( facet.threadingPolicyIsDedicated )>
   os::Thread * thread = new os::Thread( [&]() {
      <publisherBodyMethodBody()>
      delete thread;
   \});
< else>
   <publisherBodyMethodBody()>
< endif>
\}
}; separator="\n\n">
>>
