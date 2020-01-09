dispatcherImplementation(
    namespace, component, ifaces, events, eventIDs, usedTypes, offRequests, reqRequests, hasResponse, data, types
) ::= <<
//dispatcherImplementation
#include \<<namespace; format="NamespaceToPath">/<component.name>Dispatcher.hpp>
#include \<<namespace; format="NamespaceToPath">/<component.name>Component.hpp>

#include \<io/sockets.hpp>
#include \<util/Log.hpp>

#include \<stdio.h>

using namespace <namespace>;

<component.name>Dispatcher::<component.name
>Dispatcher(
   bool &                running,
   byte                  instance,
   io::DatagramSocket &  socket,
   <component.name>Component & listener )
 :
   _running ( running  ),
   _instance( instance ),
   _socket  ( socket   ),
< if( hasResponse )>
   _out     ( _outRaw, sizeof( _outRaw )),
< endif>
   _listener( listener )
{
   listener.setDispatcher( *this );
}

void <component.name>Dispatcher::terminate( void ) {
   _running = false;
   ::shutdown( _socket, SHUT_RDWR );
   closesocket( _socket );
}
<ifaces.keys:{ifaceName|


<events.(ifaceName):{facet|< if( facet.isRequest )>
static const byte EVENT_<ifaceName; format="ID">_RESPONSES_<facet.name; format="ID"> = <eventIDs.(ifaceName).(facet.name)>;
< endif>}>
bool <component.name>Dispatcher::dispatch( < if( offRequests.(ifaceName))>sockaddr_in & from, < endif
    ><ifaceName>Event event, io::ByteBuffer & in ) {
   switch( event ) {
<events.(ifaceName):{facet|< if( facet.isData )>< else>
   case <ifaceName>Event::<facet.name; format="ID">:{<
if( facet.isRequest )>
      byte fromInstance = in.getByte();
      <facet.arguments.field:{field|<
       common/byteBufferGet( field )>}>< 
elseif( facet.field )><facet.field:{field|<
       common/byteBufferGet( field )>}><
endif><
if( facet.isRequest )
>      <types.(facet.type)> <facet.name>;
      _listener.<facet.name>( <args( facet.arguments.field )>, <facet.name> );
      _out.clear();
      _out.putByte((byte)Interface::<ifaceName; format="ID">_RESPONSES );
      _out.putByte( EVENT_<ifaceName; format="ID">_RESPONSES_<facet.name; format="ID"> );
      _out.putByte( fromInstance );
      <facet.name>.put( _out );
      _out.flip();
      _socket.sendTo( _out, from );
      UTIL_LOG_ARGS( "response sent to instance %d @ %s from instance %d", fromInstance, inet_ntoa( from.sin_addr ), _instance );<
else>      _listener.<facet.name>( <args( facet.field )> );
< endif>
      break;
      \}
< endif>}>
   default:
      UTIL_LOG_ARGS( "Unknown <ifaceName>'s event: %d", (byte)event );
      return false;
   \}
   return true;
\}
}>
<data:{iface|
<dispatcherImplData()>
}>
<reqRequests:{ifaceName|

bool <component.name>Dispatcher::dispatch( <ifaceName>Response event, io::ByteBuffer & in ) {
   switch( event ) {
<reqRequests.(ifaceName):{facet|
   case <ifaceName>Response::<facet.name; format="ID">:{
      <types.(facet.type)> <facet.name>;
      <facet.name>.get( in );
      _listener.<facet.name>Response( <facet.name> );
      break;
      \}
}>   default:
      UTIL_LOG_ARGS( "Unknown <ifaceName>'s event: %d", (byte)event );
      return false;
   \}
   return true;
\}
}>
< if( component.timeout )>
bool <component.name>Dispatcher::dispatch( LoopbackEvent event ) {
   switch( event ) {
   <component.timeout:{to|case LoopbackEvent::<to.name; format="ID">: _listener.<to.name; format="argument">Elapsed(); break;
}>   default:
      UTIL_LOG_ARGS( "Unknown timeout's event: %d", (byte)event );
      return false;
   \}
   return true;
\}

< endif>
void <component.name>Dispatcher::beforeDispatch( void ) {
   _listener.beforeDispatch();
}
      
bool <component.name>Dispatcher::hasDispatched( byte intrfc, byte event, byte instance, sockaddr_in & from, io::ByteBuffer & in ) {
   if( instance == _instance ) {
      switch( static_cast\<Interface>( intrfc )) {
< if( component.timeout )>
      case Interface::LOOPBACK: return dispatch( static_cast\<LoopbackEvent>( event ));
< endif>
<ifaces.keys:{ifaceName|
      case Interface::<ifaceName; format="ID">: return dispatch( <
        if( offRequests.(ifaceName))>from, < endif>static_cast\<<ifaceName>Event>( event ), in );
}><reqRequests:{ifaceName|
      case Interface::<ifaceName; format="ID">_RESPONSES: return dispatch( static_cast\<<ifaceName>Response>( event ), in );
}><data:{iface|
      case Interface::<iface.name; format="ID">: return dispatch( static_cast\<<iface.name>Data>( event ), in );
}>         default: UTIL_LOG_ARGS( "Unknown interface: %d\n", intrfc ); break;
      }
   }
   else {
      UTIL_LOG_ARGS( "Unknown instance: %d\n", instance );
   }
   return false;
   (void)from;
}

void <component.name>Dispatcher::afterDispatch( bool dispatched ) {
   _listener.afterDispatch( dispatched );
}
< if( component.timeout )>

void <component.name>Dispatcher::loopback( const LoopbackEvent & event ) {
   byte           raw[3];
   io::ByteBuffer out( raw, sizeof( raw ));
   out.putByte((byte)Interface::LOOPBACK );
   out.putByte((byte)event );
   out.putByte( _instance );
   out.flip();
   _socket.sendTo( out, _socket.getLocalAddress());
   UTIL_LOG_ARGS( "%u byte(s) sent from instance %d to instance %d @ %s (loopback)",
      (unsigned)out.remaining(), _instance, _instance, ::inet_ntoa( _socket.getLocalAddress().sin_addr ));
}
< endif>

>>
