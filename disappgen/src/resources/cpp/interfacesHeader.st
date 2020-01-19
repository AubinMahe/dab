interfacesHeader( namespace, iface, includes, facets, types ) ::= <<
#pragma once
// interfacesHeader

#include "Interfaces.hpp"

#include \<da/FacetMessagesQueue.hpp>
#include \<io/ByteBuffer.hpp>
#include \<io/DatagramSocket.hpp>

<includes:{include|#include \<<include>.hpp>
}>
#include \<functional>

namespace <namespace> {

   class <iface.name; format="cap">Interface {
   public:

      enum class Facets : byte {
         FIRST,

<facets:{facet|
< if( facet.isRequest )>
         <facet.name; format="ID">,
         <facet.name; format="ID">_RESPONSE,
< else>
         <facet.name; format="ID">,
< endif>
}>
         LAST
      };

      static const char * toString( byte facet );

      union Payload {
<facets:{facet|
< if( facet.isEvent )>< if( facet.field )>
         struct {
            <attributes(facet.field)
>         \} <facet.name>;
< endif>< elseif( facet.isRequest )>
         struct {
            <attributes(facet.arguments.field)
>         \} <facet.name>_request;
         <types.(facet.type)> <facet.name>;
< elseif(  facet.isData )>
         <types.(facet.type)> <facet.name>;
< endif>}>      };

      typedef da::FacetMessage\<Interfaces, Payload> Message;
<facets:{facet|
< if( facet.isEvent )>

      static void <facet.name>( // Event
         < if( facet.field )><signature(facet.field)>,< endif>
         const da::InstanceID & from,
         const da::InstanceID & to,
         io::ByteBuffer       & target );
< elseif( facet.isRequest )>

      static void <facet.name>( // Request
         <signature( facet.arguments.field )>,
         const da::InstanceID & from,
         const da::InstanceID & to,
         io::ByteBuffer       & target );
 
      static void <facet.name>( // Request response, synchronous
         const <types.(facet.type)> & <facet.name>,
         const da::InstanceID & from,
         const da::InstanceID & to,
         io::ByteBuffer &       target );
 
      static void <facet.name>( // Request response, threaded
         io::DatagramSocket &   socket,
         const Message &        message,
         const da::InstanceID & from,
         std::function\<void( <signature( facet.arguments.field, false )>, <types.(facet.type)> &)> asynchronousWork );
< else>
 
      static void <facet.name>( // Data published
         const <types.(facet.type)> & <facet.name>,
         const da::InstanceID & from,
         const da::InstanceID & to,
         io::ByteBuffer &       target );
< endif>
}>
      <iface.name>Interface( void );
      <iface.name>Interface( const <iface.name>Interface & ) = delete;
      <iface.name>Interface & operator = ( const <iface.name>Interface & ) = delete;
      
      bool enqueue(
         const sockaddr_in &    from,
         byte                   event,
         const da::InstanceID & instance,
         const da::InstanceID & fromInstance,
         io::ByteBuffer &       in           );

      da::FacetMessagesQueue\<Interfaces, Payload, <iface.queueSize>\> _queue;
   };
}

>>
