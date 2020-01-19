interfacesBody( namespace, iface, facets, types, requestResponseSizes ) ::= <<
// interfacesBody
#include "<iface.name>Interface.hpp"

< if( requestResponseSizes )>
#include \<da/AsynchronousRequestResponse.hpp>
< endif>
#include \<da/FacetMessage.hpp>
#include \<util/Log.hpp>

using namespace <namespace>;

const char * <iface.name>Interface::toString( byte event ) {
   switch( static_cast\<Facets>( event )) {
<facets:{facet|
   case Facets::<facet.name; format="ID">: return "<facet.name; format="ID">";
< if( facet.isRequest )>
   case Facets::<facet.name; format="ID">_RESPONSE: return "<facet.name; format="ID">_RESPONSE";
< endif>
}>   default:
   case Facets::FIRST:
   case Facets::LAST:
      break;
   }
   return "???";
}
<facets:{facet|
< if( facet.isEvent )>

void <iface.name>Interface::<facet.name; format="argument">( // Event
   < if( facet.field )><signature(facet.field)>,< endif>
   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer       & _out )
{
   _out.clear();
   _out.putByte((byte)Interfaces::<iface.name; format="ID"> );
   _out.putByte((byte)Facets::<facet.name; format="ID"> );
   to  .put( _out );
   from.put( _out );
   < if( facet.field )><byteBufferPut( "", facet.field )>< endif
>   _out.flip();
\}
< elseif( facet.isRequest )>

void <iface.name>Interface::<facet.name; format="argument">( // Request
   <signature( facet.arguments.field )>,
   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer       & _out )
{
   _out.clear();
   _out.putByte((byte)Interfaces::<iface.name; format="ID"> );
   _out.putByte((byte)Facets::<facet.name; format="ID"> );
   to  .put( _out );
   from.put( _out );
   <byteBufferPut( "", facet.arguments.field )
>   _out.flip();
\}

void <iface.name>Interface::<facet.name; format="argument">( // Request response, synchronous
   const <types.(facet.type)> & <facet.name>,
   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer &       out )
{
   out.clear();
   out.putByte((byte)Interfaces::<iface.name; format="ID"> );
   out.putByte((byte)Facets::<facet.name; format="ID">_RESPONSE );
   to  .put( out );
   from.put( out );
   <facet.name>.put( out );
   out.flip();
\}

class <facet.name; format="cap">RequestResponse : public da::AsynchronousRequestResponse {
public:

   <facet.name; format="cap">RequestResponse(
      io::DatagramSocket &                  socket,
      const SiteCentralInterface::Message & message,
      const da::InstanceID &                from,
      std::function\<void( <signature( facet.arguments.field, false )>, <types.(facet.type)> & )> asynchronousWork )
    :
       da::AsynchronousRequestResponse( socket, from, message._fromInstance, message._from ),
       _asynchronousWork              ( asynchronousWork                                   )
   {
      <argsToAttribute( facet.arguments.field, concat( concat( "message._payload.", facet.name ), "_request" ))>
      start();
   \}

public:

   virtual void run( void ) {
      hpms::dabtypes::Information informations;
      _asynchronousWork(<args( facet.arguments.field, "_" )>, <facet.name> );
      byte           raw[<requestResponseSizes.(facet)>];
      io::ByteBuffer out( raw, sizeof( raw ));
      <iface.name>Interface::<facet.name>( <facet.name>, _from, _to, out );
      _socket.sendTo( out, _target );
      UTIL_LOG_ARGS( "response sent to instance %s @ %s from instance %s",
         _to.toString(), inet_ntoa( _target.sin_addr ), _from.toString());
   \}

private:

   <attributes( facet.arguments.field, "_" )
>   std::function\<void( <signature( facet.arguments.field, false )>, <types.(facet.type)> & )> _asynchronousWork;
\};

void <iface.name>Interface::<facet.name>( // Request response, threaded
   io::DatagramSocket &   socket,
   const Message &        message,
   const da::InstanceID & from,
   std::function\<void( <signature( facet.arguments.field, false )>, <types.(facet.type)> & )> asynchronousWork )
{
   new <facet.name; format="cap">RequestResponse( socket, message, from, asynchronousWork );
\}
< else>

void <iface.name>Interface::<facet.name; format="argument">( // Data published
   const <types.(facet.type)> & <facet.name>,
   const da::InstanceID & from,
   const da::InstanceID & to,
   io::ByteBuffer &       out )
{
   out.clear();
   out.putByte((byte)Interfaces::<iface.name; format="ID"> );
   out.putByte((byte)Facets::<facet.name; format="ID"> );
   to  .put( out );
   from.put( out );
   <facet.name>.put( out );
   out.flip();
\}
< endif>
}>
<iface.name>Interface::<iface.name>Interface( void ) :
   _queue( Interfaces::<iface.name; format="ID">, "<iface.name>", true )
{}

bool <iface.name>Interface::enqueue(
   const sockaddr_in &    from,
   byte                   event,
   const da::InstanceID & instance,
   const da::InstanceID & fromInstance,
   io::ByteBuffer &       in )
{
   if(( event \<= (byte)Facets::FIRST )||( event >= (byte)Facets::LAST )) {
      UTIL_LOG_ARGS( "event %d is out of range", event );
   }
   else {
      da::FacetMessage\<Interfaces, Payload> msg( from, Interfaces::<iface.name; format="ID">, event, instance, fromInstance );
      Facets evt = static_cast\<Facets>( event );
      switch( evt ) {
<facets:{facet|
< if( facet.isEvent)>
      case Facets::<facet.name; format="ID">:{ // event
<    if( facet.field )>
         <byteBufferGetToMessage( facet.name, facet.field )
>         _queue.< if( facet.isImmediate )>push_front< else>push_back< endif>( msg );
<    else>
         _queue.< if( facet.isImmediate )>push_front< else>push_back< endif>( msg );
<    endif>
         return <facet.isActivatingOrImmediate>; \}
< elseif( facet.isRequest )>
      case Facets::<facet.name; format="ID">:{ // request
         <byteBufferGetToMessage( concat( facet.name, "_request" ), facet.arguments.field )
>         _queue.< if( facet.isImmediate )>push_front< else>push_back< endif>( msg );
         return <facet.isActivatingOrImmediate>; \}
      case Facets::<facet.name; format="ID">_RESPONSE:{ // response
         msg._payload.<facet.name>.get( in );
         _queue.push_back( msg );
         return <facet.isActivatingOrImmediate>; \}
< elseif( facet.isData )>
      case Facets::<facet.name; format="ID">:{ // data
         msg._payload.<facet.name>.get( in );
         _queue.< if( facet.isImmediate )>push_front< else>push_back< endif>( msg );
         return <facet.isActivatingOrImmediate>; \}
< endif>
}>      case Facets::FIRST:
      case Facets::LAST : /* unreachable */ break;
      }
   }
   return false;
}

>>
