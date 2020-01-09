eventsEnumBody( namespace, ifaceName, className, facets, types, isEvent ) ::= <<
#include "<className>.hpp"

#include \<da/FacetMessage4.hpp>
#include \<util/Log.hpp>

using namespace <namespace>;
< if( isEvent )>
<facets:{facet|
< if( facet.isRequest )>

void <className><facet.name; format="cap">(
   <common/signature( facet.arguments.field )>,
   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer       & _out )
< else>

void <className><facet.name; format="cap">(
   < if( facet.field )><common/signature(facet.field)>,
   < endif>   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer       & _out )
< endif>
{
   _out.clear();
   _out.putByte((byte)Interfaces::<className; format="ID"> );
   _out.putByte((byte)<className>::<facet.name; format="ID"> );
   to  .put( _out );
   from.put( _out );
< if( facet.isRequest )>
   <common/byteBufferPut( "", facet.arguments.field )><
else>
   <common/byteBufferPut( "", facet.field )><
endif>
   _out.flip();
\}
}>
< else>
<facets:{facet|
 
// Request response or data published
void <className><facet.name; format="cap">(
   <types.(facet.type)> & <facet.name>,
   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer &       _out )
{
   _out.clear();
   _out.putByte((byte)Interfaces::<className; format="ID"> );
   _out.putByte((byte)<className>::<facet.name; format="ID"> );
   to  .put( _out );
   from.put( _out );
   <facet.name>.put( _out );
   _out.flip();
\}
}>< endif>

bool <className>Enqueue(
   const sockaddr_in &                                               from,
   byte                                                              event,
   const da::InstanceID &                                            instance,
   const da::InstanceID &                                            fromInstance,
   io::ByteBuffer &                                                  in,
   std::deque\<da::FacetMessage\<Interfaces, byte, da::InstanceID> > & mainQueue )
{
   <className> evt = static_cast\<<className>\>( event );
   if( 0 == event || event >= (byte)<className>::LAST ) {
      UTIL_LOG_ARGS( "<className>Enqueue|%d is out of range", event );
   }
   else {
      switch( evt ) {
<facets:{facet|
      case <className>::<facet.name; format="ID">:{
< if( facet.isData )>
         <types.(facet.type)> <facet.name>;
         <facet.name>.get( in );
         mainQueue.< if( facet.isImmediate )>push_front< else>push_back< endif>(
            da::FacetMessage1\<Interfaces, byte, da::InstanceID, <types.(facet.type)>\>(
               from, Interfaces::<(common/concat(ifaceName,"Data")); format="ID">, event, instance, fromInstance, <facet.name> ));
         return <facet.isActivatingOrImmediate>; \}
< elseif( facet.isRequest )><
if( isEvent )>
         <facet.arguments.field:{field|<common/byteBufferGet( field )
>}>         mainQueue.< if( facet.isImmediate )>push_front< else>push_back< endif>( 
               da::FacetMessage< length(facet.arguments.field)>\<Interfaces, byte, da::InstanceID, <
                common/templateArgs( facet.arguments.field )>\>(
                  from, Interfaces::<(common/concat(ifaceName,"Event")); format="ID">, event, instance, fromInstance, <
                facet.arguments.field:{field|<field.name>}; separator=", "> ));
< else>
         <types.(facet.type)> <facet.name>;
         <facet.name>.get( in );
         mainQueue.< if( facet.isImmediate )>push_front< else>push_back< endif>(
            da::FacetMessage1\<Interfaces, byte, da::InstanceID, <types.(facet.type)>\>(
               from, Interfaces::<(common/concat(ifaceName,"Request")); format="ID">, event, instance, fromInstance, <facet.name> ));
< endif>
         return <facet.isActivatingOrImmediate>;\}
< elseif( facet.field )>
         <facet.field:{field|<common/byteBufferGet( field )>}
>            mainQueue.< if( facet.isImmediate )>push_front< else>push_back< endif>(
                da::FacetMessage< length(facet.field)>\<Interfaces, byte, da::InstanceID, <common/templateArgs( facet.field )>\>(
                   from, Interfaces::<(common/concat(ifaceName,"Event")); format="ID">, event, instance, fromInstance, <
                        facet.field:{field|<field.name>}; separator=", "> ));
         return <facet.isActivatingOrImmediate>;\}
< else>
         mainQueue.< if( facet.isImmediate )>push_front< else>push_back< endif>(
            da::FacetMessage\<Interfaces, byte, da::InstanceID>(
               from, Interfaces::<(common/concat(ifaceName,"Event")); format="ID">, event, instance, fromInstance ));
         return <facet.isActivatingOrImmediate>;\}
< endif>
}>      case <className>::LAST: /* unreachable */ break;
      }
   }
   return false;
}

>>
