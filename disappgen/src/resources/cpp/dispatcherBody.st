dispatcherBody(
    namespace, ifacesNamespace, component, offered, offRequests, reqRequests, hasResponse, data, types
) ::= <<
// dispatcherBody
#include \<<namespace; format="NamespaceToPath">/<component.name>Dispatcher.hpp>
#include \<<namespace; format="NamespaceToPath">/<component.name>Component.hpp>
<offered:{ifaceName      |#include \<<ifacesNamespace; format="NamespaceToPath">/<ifaceName >Interface.hpp>
}><reqRequests:{ifaceName|#include \<<ifacesNamespace; format="NamespaceToPath">/<ifaceName >Interface.hpp>
}><data:{iface           |#include \<<ifacesNamespace; format="NamespaceToPath">/<iface.name>Interface.hpp>
}>
#include \<io/sockets.hpp>
#include \<util/Log.hpp>

#include \<stdio.h>
#include \<string.h>


using namespace <namespace>;

<component.name>Dispatcher::<component.name
>Dispatcher(
   bool &                 running,
   const da::InstanceID & instance,
   io::DatagramSocket &   socket,
   <component.name>Component & listener )
 :
   _running ( running  ),
< if( component.timeout || hasResponse )>
   _instance( instance ),
< endif>
   _socket  ( socket   ),
< if( hasResponse )>
   _out     ( _outRaw, sizeof( _outRaw )),
< endif>
   _listener( listener )
{
   listener.setDispatcher( *this );
< if( ! ( component.timeout || hasResponse ))>
   (void)instance;
< endif>
}

void <component.name>Dispatcher::terminate( void ) {
   _running = false;
   ::shutdown( _socket, SHUT_RDWR );
   closesocket( _socket );
}

<offered:{ifaceName|
bool <component.name>Dispatcher::dispatch<ifaceName; format="cap">( const <ifacesNamespace>::<ifaceName>Interface::Message & message ) {
   UTIL_LOG_ARGS( "event: %s, from: %s, to: %s",
      ::toString( <ifacesNamespace>::Interfaces::<ifaceName; format="ID">, message._event ),
       message._fromInstance.toString(), message._instance.toString());
   switch( static_cast\<<ifacesNamespace>::<ifaceName>Interface::Facets>( message._event )) {
<offered.(ifaceName):{facet|<
if( facet.isData )><
else>
   case <ifacesNamespace>::<ifaceName>Interface::Facets::<facet.name; format="ID">:{
< if( facet.isEvent )>
      // event
      _listener.<facet.name>(< if( facet.field )>
         <facet.field:{field|message._payload.<facet.name>.<field.name>}; separator=",\n"> < else>< endif>);
< elseif( facet.isRequest )>
      // request
< if( facet.threadingPolicyIsDedicated )>
      <ifacesNamespace>::<ifaceName>Interface::<facet.name>( _socket, message, _instance,
         [&]( <signature( facet.arguments.field )>, <types.(facet.type)> & <facet.name; format="argument"> ) {
            _listener.<facet.name>(<args( facet.arguments.field )>, <facet.name; format="argument"> );\});
< else>
      <types.(facet.type)> <facet.name>;
      _listener.<facet.name>(<facet.arguments.field:{field|message._payload.<facet.name>_request.<field.name>}; separator=",">, <facet.name> );
      <ifacesNamespace>::<ifaceName>Interface::<facet.name>( <facet.name>, _instance, message._fromInstance, _out );
      _socket.sendTo( _out, message._from );
      UTIL_LOG_ARGS( "response sent to instance %s @ %s from instance %s",
         message._fromInstance.toString(), inet_ntoa( message._from.sin_addr ), _instance.toString());
< endif>
< endif>
      return true; \}
< endif>}>   default:
      break;
   \}
   UTIL_LOG_ARGS( "Unexpected event: %d", (byte)message._event );
   return false;
\}

}><data:{iface|
//-- data-begin
bool <component.name>Dispatcher::dispatch<iface.name; format="cap">( const <ifacesNamespace>::<iface.name>Interface::Message & message ) {
   switch( static_cast\<<ifacesNamespace>::<iface.name>Interface::Facets>( message._event )) {
<data.(iface):{dataType|
   case <ifacesNamespace>::<iface.name>Interface::Facets::<dataType.name; format="ID">:{
      _listener.get<dataType.name; format="cap">().set( message._payload.<dataType.name> );
      _listener.<dataType.name>Published();
      return true; \}
}>   default:
      UTIL_LOG_ARGS( "Unexpected event: %s", toString( <ifacesNamespace>::Interfaces::<iface.name; format="ID">, message._event ));
      return false;
   \}
\}
//-- data-end

}><reqRequests:{ifaceName|
bool <component.name>Dispatcher::dispatch<ifaceName; format="cap">( const <ifacesNamespace>::<ifaceName>Interface::Message & message ) {
   UTIL_LOG_ARGS( "event: %s, from: %s, to: %s",
      ::toString( <ifacesNamespace>::Interfaces::<ifaceName; format="ID">, message._event ),
      message._fromInstance.toString(), message._instance.toString());
   switch( static_cast\<<ifacesNamespace>::<ifaceName>Interface::Facets>( message._event )) {
<reqRequests.(ifaceName):{facet|
   case <ifacesNamespace>::<ifaceName>Interface::Facets::<facet.name; format="ID">_RESPONSE:{
      _listener.<facet.name>Response( message._payload.<facet.name> );
      return true; \}
}>   default:
      break;
   \}
   UTIL_LOG_ARGS( "Unexpected event: %d", (byte)message._event );
   return false;
\}

}>< if( component.timeout )
>bool <component.name>Dispatcher::dispatchTimeout( <ifacesNamespace>::TimeoutInterface::Facets event ) {
   UTIL_LOG_ARGS( "timeout event: %s", ::toString( <ifacesNamespace>::Interfaces::TIMEOUT, (byte)event ));
   switch( event ) {
   <component.timeout:{to|case <ifacesNamespace>::TimeoutInterface::Facets::<component.name; format="ID">_<to.name; format="ID"
        >: _listener.<to.name; format="argument">Elapsed(); return true;
}>   default: break;
   }
   UTIL_LOG_ARGS( "Unexpected timeout event: %d", (byte)event );
   return false;
\}

void <component.name>Dispatcher::timeout( <ifacesNamespace>::TimeoutInterface::Facets event ) {
   byte           raw[4];
   io::ByteBuffer out( raw, sizeof( raw ));
   <ifacesNamespace>::TimeoutInterface::update( event, _instance, out );
   _socket.sendTo( out, _socket.getLocalAddress());
   UTIL_LOG_ARGS( "%u byte(s) sent from instance %s to instance %s @ %s (loopback)",
      (unsigned)out.remaining(), _instance.toString(), _instance.toString(), ::inet_ntoa( _socket.getLocalAddress().sin_addr ));
}

< endif>
bool <component.name>Dispatcher::hasDispatched( const da::BaseFacetMessage\<<ifacesNamespace>::Interfaces> & message ) {
   UTIL_LOG_ARGS( "event: %d, from: %s, to: %s",
      message._event, message._fromInstance.toString(), message._instance.toString());
   switch( message._interface ) {
< if( component.timeout )>
   case <ifacesNamespace>::Interfaces::TIMEOUT:
      return dispatchTimeout( static_cast\<<ifacesNamespace>::TimeoutInterface::Facets>( message._event ));
< endif>
<offered:{ifaceName|
   case <ifacesNamespace>::Interfaces::<ifaceName; format="ID">:
      return dispatch<ifaceName; format="cap">((const <ifacesNamespace>::<ifaceName>Interface::Message &)message );
}><reqRequests:{ifaceName|
   case <ifacesNamespace>::Interfaces::<ifaceName; format="ID">:
      return dispatch<ifaceName; format="cap">((const <ifacesNamespace>::<ifaceName>Interface::Message &)message );
}><data:{iface|
   case <ifacesNamespace>::Interfaces::<iface.name; format="ID">:
      return dispatch<iface.name; format="cap">((const <ifacesNamespace>::<iface.name>Interface::Message &)message );
}>   default:
      UTIL_LOG_ARGS( "Unknown interface: %s\n", ::toString( message._interface ));
      break;
   }
   return false;
}

void <component.name>Dispatcher::afterDispatch( void ) {
   _listener.afterDispatch();
}

>>
