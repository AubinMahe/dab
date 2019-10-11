#include <util/Timeout.hpp>
#include <os/Thread.hpp>
#include <util/Exceptions.hpp>

#include <sys/time.h>   // gettimeofday

using namespace util;

Timeout::Timeout( unsigned milliseconds ) :
   _delayMs( milliseconds )
{}

void * Timeout::waiting( void * arg ) {
   Timeout * t = (Timeout *)arg;
   if( ! t->_event.wait( &t->_deadline )) {
      t->action();
   }
   return NULL;
}

void Timeout::start( void ) {
   struct timeval tv;
   if( gettimeofday( &tv, NULL )) {
      throw Runtime( __FILE__,__LINE__,__PRETTY_FUNCTION__, "gettimeofday" );
   }
   unsigned sec = _delayMs / 1000U;
   unsigned ms  = _delayMs % 1000U;
   _deadline.tv_sec  = tv.tv_sec + sec;
   _deadline.tv_nsec = 1000U * ( tv.tv_usec + 1000U * ms );
   _deadline.tv_sec  += _deadline.tv_nsec / 1000000000U;
   _deadline.tv_nsec %= 1000000000U;
   os::Thread thread( waiting, this );
   thread.detach();
}

void Timeout::cancel( void ) {
   _event.signal();
}
