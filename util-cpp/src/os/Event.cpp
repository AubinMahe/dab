#include <os/Event.hpp>
#include <util/Exceptions.hpp>
#include <util/Log.hpp>
#include <types.hpp>

#include <sys/time.h>
#include <inttypes.h>
#include <stdio.h>

using namespace os;

Event::Event( void ) {
#ifdef _WIN32
   _event = ::CreateEvent( 0, FALSE, FALSE, 0 );
   if( ! _event ) {
      throw util::Runtime( UTIL_CTXT, "CreateEvent" );
   }
#else
   if( ::pthread_mutex_init( &_condLock, 0 )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_init" );
   }
   if( ::pthread_cond_init( &_condition, 0 )) {
      throw util::Runtime( UTIL_CTXT, "pthread_cond_init" );
   }
#endif
}

Event:: ~ Event( void ) {
#ifdef _WIN32
   if( ! ::CloseHandle( _event )) {
      UTIL_LOG_MSG( util::Runtime( UTIL_CTXT, "CloseHandle" ).what());
   }
#else
   if( ::pthread_mutex_destroy( &_condLock )) {
      UTIL_LOG_MSG( util::Runtime( UTIL_CTXT, "pthread_mutex_destroy" ).what());
   }
   if( ::pthread_cond_destroy( &_condition )) {
      UTIL_LOG_MSG( util::Runtime( UTIL_CTXT, "pthread_cond_destroy" ).what());
   }
#endif
}

bool Event::wait( const timespec * deadline /* = 0 */ ) {
   bool retVal = true;
#ifdef _WIN32
   if( deadline ) {
      timeval tv;
      if( ::gettimeofday( &tv, NULL )) {
         throw util::Runtime( UTIL_CTXT, "gettimeofday" );
      }
      int64_t now_ms = tv.tv_sec;
      now_ms *= 1000;
      now_ms += tv.tv_usec / 1000;
      int64_t deadline_ms = deadline->tv_sec;
      deadline_ms *= 1000;
      deadline_ms += deadline->tv_nsec / 1000000;
      int64_t timeout = deadline_ms - now_ms;
      if( timeout < 0 ) {
         throw util::Unexpected( UTIL_CTXT, "timeout < 0!");
      }
      DWORD ret = ::WaitForSingleObject( _event, (unsigned)timeout );
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
            break;
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
   if( ::pthread_mutex_lock( &_condLock )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_lock" );
   }
   _signaled = true;
   if( ::pthread_cond_signal( &_condition )) {
      throw util::Runtime( UTIL_CTXT, "pthread_cond_signal" );
   }
   if( ::pthread_mutex_unlock( &_condLock )) {
      throw util::Runtime( UTIL_CTXT, "pthread_mutex_unlock" );
   }
#endif
}
