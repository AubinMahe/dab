#include <util/Timeout.hpp>
#include <os/Thread.hpp>
#include <util/Exceptions.hpp>

#include <sys/time.h>   // gettimeofday

using namespace util;

Timeout::Timeout( unsigned milliseconds ) :
   _delayMs( milliseconds ),
   _state  ( INITIALIZED )
{}

void * Timeout::waiting( void * arg ) {
   Timeout * t = (Timeout *)arg;
   if( t->_event.wait( &t->_deadline )) {
      t->_state = CANCELED;
   }
   else {
      t->_state = ELAPSED;
      t->action();
   }
   return NULL;
}

void Timeout::start( void ) {
   _state = RUNNING;
   timeval tv;
   if( gettimeofday( &tv, NULL )) {
      throw Runtime( UTIL_CTXT, "gettimeofday" );
   }
   unsigned sec = _delayMs / 1000U;
   unsigned ms  = _delayMs % 1000U;
   _deadline.tv_sec  = tv.tv_sec + sec;
   _deadline.tv_nsec = 1000U * ((unsigned)tv.tv_usec + 1000U * ms );
   _deadline.tv_sec  += _deadline.tv_nsec / 1000000000;
   _deadline.tv_nsec %= 1000000000;
   os::Thread( waiting, this ).detach();
}

void Timeout::cancel( void ) {
   _event.signal();
}
