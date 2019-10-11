#include <os/Mutex.hpp>
#include <util/Exceptions.hpp>

#include <stdio.h>

using namespace os;

Mutex::Mutex( bool synchronizedBlock ) :
   _synchronizedBlock( synchronizedBlock )
{
#ifdef _WIN32
      _mutex = ::CreateMutex( 0, FALSE, 0 );
      if( ! _mutex ) {
         throw util::Runtime( UTIL_CTXT, "CreateMutex" );
      }
#else
      if( ::pthread_mutex_init( &_mutex, 0 )) {
         throw util::Runtime( UTIL_CTXT, "pthread_mutex_init" );
      }
#endif
   if( _synchronizedBlock ) {
      take();
   }
}

Mutex:: ~ Mutex( void ) {
#ifdef _WIN32
   if( _synchronizedBlock ) {
      if( ! ::ReleaseMutex( _mutex )) {
         fprintf( stderr, "%s\n",
            util::Runtime( UTIL_CTXT, "ReleaseMutex" ).what());
      }
   }
   if( ! ::CloseHandle( _mutex )) {
      fprintf( stderr, "%s\n",
         util::Runtime( UTIL_CTXT, "CloseHandle" ).what());
   }
#else
   if( _synchronizedBlock ) {
      if( ::pthread_mutex_unlock( &_mutex )) {
         fprintf( stderr, "%s\n",
            util::Runtime( UTIL_CTXT, "pthread_mutex_unlock" ).what());
      }
   }
   if( ::pthread_mutex_destroy( &_mutex )) {
      // a destructor is tagged noexcept by default
      fprintf( stderr, "%s\n",
         util::Runtime( UTIL_CTXT, "pthread_mutex_destroy" ).what());
   }
#endif
}

void Mutex::take( void ) {
#ifdef _WIN32
   if( WAIT_FAILED == ::WaitForSingleObject( _mutex, INFINITE )) {
      throw util::Runtime( UTIL_CTXT, "WaitForSingleObject" );
   }
#else
   if( ::pthread_mutex_lock( &_mutex )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_lock" );
   }
#endif
}

void Mutex::release( void ) {
#ifdef _WIN32
   if( ! ::ReleaseMutex( _mutex )) {
      throw util::Runtime( UTIL_CTXT, "ReleaseMutex" );
   }
#else
   if( ::pthread_mutex_unlock( &_mutex )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_unlock" );
   }
#endif
}
