#include <os/Event.hpp>
#include <util/Exceptions.hpp>

using namespace os;

Event::Event( void ) {
#ifdef _WIN32
   _event = ::CreateEvent( 0, FALSE, FALSE, 0 );
#else
   ::pthread_mutex_init( &_condLock , 0 );
   ::pthread_cond_init ( &_condition, 0 );
#endif
}

Event:: ~ Event( void ) {
#ifdef _WIN32
   ::CloseHandle( _event );
#else
   ::pthread_mutex_destroy( &_condLock );
   ::pthread_cond_destroy ( &_condition );
#endif
}

bool Event::wait( const struct timespec * deadline /* = 0 */ ) {
   bool retVal = true;
#ifdef _WIN32
   if( deadline ) {
      FILETIME utcTime;
      GetSystemTimeAsFileTime( &utcTime );
      ULARGE_INTEGER utcTime_100_nanos;
      utcTime_100_nanos.u.LowPart  = utcTime.dwLowDateTime;
      utcTime_100_nanos.u.HighPart = utcTime.dwHighDateTime;
      long deadline_ms = 10000000 * deadline->tv_sec  + deadline->tv_nsec / 100;
      unsigned long timeout = (unsigned long)((((ULONGLONG)deadline_ms) - utcTime_100_nanos.QuadPart ) / 10000UL );
      DWORD ret = ::WaitForSingleObject( _event, timeout );
      if( WAIT_FAILED == ret ) {
         throw util::Runtime( UTIL_CTXT, "WaitForSingleObject" );
      }
      if( WAIT_TIMEOUT == ret ) {
         retVal = false;
      }
   }
   else {
      if( WAIT_FAILED == ::WaitForSingleObject( _event, INFINITE )) {
         throw util::Runtime( UTIL_CTXT, "WaitForSingleObject" );
      }
   }
#else
   if( ::pthread_mutex_lock( &_condLock )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_lock" );
   }
   _signaled = false;
   while( ! _signaled ) {
      if( deadline ) {
         int ret = pthread_cond_timedwait( &_condition, &_condLock, deadline );
         if( ETIMEDOUT == ret ) {
            retVal = false;
         }
         else if( ! _signaled ) {
            throw util::Runtime( UTIL_CTXT, "pthread_cond_timedwait" );
         }
      }
      else {
         if( pthread_cond_wait( &_condition, &_condLock )) {
            throw util::Runtime( UTIL_CTXT, "pthread_cond_wait" );
         }
      }
   }
   if( ::pthread_mutex_unlock( &_condLock )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_lock" );
   }
#endif
   return retVal;
}

void Event::signal( void ) {
#ifdef _WIN32
   ::PulseEvent( _event );
#else
   ::pthread_mutex_lock  ( &_condLock );
   _signaled = true;
   ::pthread_cond_signal ( &_condition );
   ::pthread_mutex_unlock( &_condLock );
#endif
}
