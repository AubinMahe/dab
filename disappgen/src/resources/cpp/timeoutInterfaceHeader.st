timeoutInterfaceHeader( namespace, timeouts) ::= <<
#pragma once
// timeoutInterfaceHeader

#include "Interfaces.hpp"

#include \<da/FacetMessagesQueue.hpp>
#include \<io/ByteBuffer.hpp>

namespace <namespace> {

   class TimeoutInterface {
   public:

      enum class Facets : byte {
         FIRST,

         <timeouts:{timeout|<timeout; format="ID">,
}>
         LAST
      };

      static const char * toString( byte event );

      struct Payload {};

      static void update( Facets facet, const da::InstanceID & instance, io::ByteBuffer & target );

   public:

      TimeoutInterface( void );
      TimeoutInterface( const TimeoutInterface & ) = delete;
      TimeoutInterface & operator = ( const TimeoutInterface & ) = delete;

   public:

      bool enqueue( const sockaddr_in & from, byte event, const da::InstanceID & instance );

      typedef da::FacetMessage\<Interfaces, Payload> Message;

      da::FacetMessagesQueue\<Interfaces, Payload, < length( timeouts )>\> _queue;
   };
}

>>