#include <os/Condition.hpp>
#include <util/Exceptions.hpp>
#include <util/Log.hpp>
#include <types.hpp>

#include <sys/time.h>
#include <inttypes.h>
#include <stdio.h>

using namespace os;

Condition::Condition( void ) {
#ifdef _WIN32
   _event = ::CreateEvent( 0, FALSE, FALSE, 0 );
   if( ! _event ) {
      throw util::Runtime( UTIL_CTXT, "CreateEvent" );
   }
#else
   if( ::pthread_cond_init( &_condition, 0 )) {
      throw util::Runtime( UTIL_CTXT, "pthread_cond_init" );
   }
#endif
}

Condition:: ~ Condition( void ) {
#ifdef _WIN32
   if( ! ::CloseHandle( _event )) {
      UTIL_LOG_MSG( util::Runtime( UTIL_CTXT, "CloseHandle" ).what());
   }
#else
   if( ::pthread_cond_destroy( &_condition )) {
      UTIL_LOG_MSG( util::Runtime( UTIL_CTXT, "pthread_cond_destroy" ).what());
   }
#endif
}

bool Condition::wait( Mutex & mutex, const timespec * deadline /* = 0 */ ) {
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
   _signaled = false;
   while( ! _signaled ) {
      if( deadline ) {
         int ret = pthread_cond_timedwait( &_condition, &mutex._mutex, deadline );
         if( ETIMEDOUT == ret ) {
            retVal = false;
            break;
         }
         else if( ! _signaled ) {
            throw util::Runtime( UTIL_CTXT, "pthread_cond_timedwait" );
         }
      }
      else {
         if( pthread_cond_wait( &_condition, &mutex._mutex )) {
            throw util::Runtime( UTIL_CTXT, "pthread_cond_wait" );
         }
      }
   }
#endif
   return retVal;
}

void Condition::signal( void ) {
#ifdef _WIN32
   ::PulseEvent( _event );
#else
   _signaled = true;
   if( ::pthread_cond_signal( &_condition )) {
      throw util::Runtime( UTIL_CTXT, "pthread_cond_signal" );
   }
#endif
}
