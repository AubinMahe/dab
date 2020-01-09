eventsEnumHeader( namespace, ifaceName, includes, className, facets, types, isEvent ) ::= <<
#pragma once

#include \<io/ByteBuffer.hpp>
#include \<da/FacetMessage.hpp>
#include \<da/InstanceID.hpp>
#include "Interfaces.hpp"

#include \<deque>

<includes:{include|#include \<<include>.hpp>
}>
namespace <namespace> {

   enum class <className> : byte {

      <facets:{facet|<facet.name; format="ID"> = <i>,
}>      LAST
   };
< if( isEvent )>
<facets:{facet|
< if( facet.isRequest )>

   void <className><facet.name; format="cap">(
      <common/signature( facet.arguments.field )>,
      const da::InstanceID & from,
      const da::InstanceID & to,
      io::ByteBuffer       & out );
< else>

   void <className><facet.name; format="cap">(
      < if( facet.field )><common/signature(facet.field)>,
      < endif>      const da::InstanceID & from,
      const da::InstanceID & to,
      io::ByteBuffer       & out );
< endif>
}>
< else>
<facets:{facet|
 
   // Request response or data published
   void <className><facet.name; format="cap">(
      <types.(facet.type)> & <facet.name>,
      const da::InstanceID & from,
      const da::InstanceID & to,
      io::ByteBuffer &       out );
}>< endif>

   bool <className>Enqueue(
      const sockaddr_in &                                               from,
      byte                                                              event,
      const da::InstanceID &                                            instance,
      const da::InstanceID &                                            fromInstance,
      io::ByteBuffer &                                                  in,
      std::deque\<da::FacetMessage\<Interfaces, byte, da::InstanceID> > & mainQueue );
}

>>
