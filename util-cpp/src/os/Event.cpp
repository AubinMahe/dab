#include <os/Event.hpp>

using namespace os;

Event::Event( void ) {
   ::pthread_mutex_init( &_condLock , 0 );
   ::pthread_cond_init ( &_condition, 0 );
}

Event:: ~ Event( void ) {
   ::pthread_mutex_destroy( &_condLock );
   ::pthread_cond_destroy ( &_condition );
}

void Event::wait( void ) {
   ::pthread_mutex_lock  ( &_condLock );
   _signaled = false;
   while( ! _signaled ) {
      ::pthread_cond_wait( &_condition, &_condLock );
   }
   ::pthread_mutex_unlock( &_condLock );
}

void Event::signal( void ) {
   ::pthread_mutex_lock  ( &_condLock );
   _signaled = true;
   ::pthread_cond_signal ( &_condition );
   ::pthread_mutex_unlock( &_condLock );
}
