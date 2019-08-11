#include <os/Mutex.hpp>
#include <os/StdApiException.hpp>

using namespace os;

Mutex::Mutex( bool synchronizedBlock ) :
   _synchronizedBlock( synchronizedBlock )
{
   if( ::pthread_mutex_init( &_mutex, 0 )) {
      throw StdApiException( "pthread_mutex_init", __FILE__, __LINE__ );
   }
   if( _synchronizedBlock ) {
      take();
   }
}

Mutex:: ~ Mutex( void ) {
   if( _synchronizedBlock ) {
      release();
   }
   ::pthread_mutex_destroy( &_mutex );
}

void Mutex::take( void ) {
   if( ::pthread_mutex_lock( &_mutex )) {
      throw StdApiException( "pthread_mutex_lock", __FILE__, __LINE__ );
   }
}

void Mutex::release( void ) {
   if( ::pthread_mutex_unlock( &_mutex )) {
      throw StdApiException( "pthread_mutex_unlock", __FILE__, __LINE__ );
   }
}
