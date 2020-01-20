timeoutInterfaceBody( namespace, timeouts) ::= <<
// timeoutInterfaceBody
#include "TimeoutInterface.hpp"

#include \<da/FacetMessage.hpp>
#include \<util/Log.hpp>

using namespace <namespace>;

const char * TimeoutInterface::toString( byte event ) {
   switch( static_cast\<Facets>( event )) {
   <timeouts:{timeout|case Facets::<timeout; format="ID">: return "<timeout; format="ID">";
}>   default:
   case Facets::FIRST:
   case Facets::LAST:
      break;
   }
   return "???";
}

void TimeoutInterface::update( Facets facet, const da::InstanceID & instance, io::ByteBuffer & target ) {
   target.clear();
   target.putByte((byte)<namespace>::Interfaces::TIMEOUT );
   target.putByte((byte)facet );
   instance.put( target );
   instance.put( target );
   target.flip();
}

TimeoutInterface::TimeoutInterface( void ) :
   _queue( Interfaces::TIMEOUT, "Timeout", true )
{}

bool TimeoutInterface::enqueue( const sockaddr_in & from, byte event, const da::InstanceID & instance ) {
   if(( event \<= (byte)Facets::FIRST )||( event >= (byte)Facets::LAST )) {
      UTIL_LOG_ARGS( "event %d is out of range", event );
      return false;
   }
   da::FacetMessage\<Interfaces, Payload> msg( from, Interfaces::TIMEOUT, event, instance, instance );
   return _queue.push_back( msg );
}

>>
