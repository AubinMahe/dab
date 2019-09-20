#include <io/Selector.hpp>
#include <os/StdApiException.hpp>

#include <sys/time.h>

using namespace io;

Selector::Selector( std::initializer_list<SOCKET> sockets ) {
   FD_ZERO( &_ensemble );
   for( auto sckt : sockets ) {
      FD_SET( sckt, &_ensemble );
   }
}

bool Selector::select( unsigned timeoutValue ) {
   struct timeval timeout;
   if( timeoutValue > 0 ) {
      timeout.tv_sec  = timeoutValue / 1000;
      timeout.tv_usec = 1000 * ( timeoutValue % 1000 );
   }
   int count = ::select( FD_SETSIZE, &_ensemble, 0, 0, ( timeoutValue > 0 ) ? &timeout : 0 );
   if( count < 0 ) {
      throw os::StdApiException( "SetOfSocket.select", __FILE__, __LINE__ );
   }
   return count > 0;
}

bool Selector::isSet( SOCKET sckt ) {
   return FD_ISSET( sckt, &_ensemble );
}
