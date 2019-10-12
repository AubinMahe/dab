#include <os/event.h>
#include <os/errors.h>
#include <types.h>

#include <errno.h>
#include <sys/time.h>   // gettimeofday

util_error os_event_init( os_event * This ) {
   UTIL_CHECK_NON_NULL( This );
#ifdef _WIN32
   This->event = CreateEvent( NULL, FALSE, FALSE, NULL );
#else
   OS_CHECK( pthread_mutex_init( &This->condLock , 0 ));
   OS_CHECK( pthread_cond_init ( &This->condition, 0 ));
#endif
   return UTIL_NO_ERROR;
}

util_error os_event_destroy( os_event * This ) {
   UTIL_CHECK_NON_NULL( This );
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   if( ! CloseHandle( This->event  )) {
      retVal = UTIL_OS_ERROR;
   }
#else
   if( pthread_mutex_destroy( &This->condLock )) {
      retVal = UTIL_OS_ERROR;
   }
   if( pthread_cond_destroy ( &This->condition )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
}

util_error os_event_wait( os_event * This, const struct timespec * deadline ) {
   UTIL_CHECK_NON_NULL( This );
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   if( deadline ) {
      struct timeval tv;
      OS_CHECK( gettimeofday( &tv, NULL ));
      int64_t now_ms = tv.tv_sec;
      now_ms *= 1000;
      now_ms += tv.tv_usec / 1000;
      int64_t deadline_ms = deadline->tv_sec;
      deadline_ms *= 1000;
      deadline_ms += deadline->tv_nsec / 1000000;
      int64_t timeout = deadline_ms - now_ms;
      DWORD ret = WaitForSingleObject( This->event, (unsigned)timeout );
      OS_ASSERT( "WaitForSingleObject", WAIT_FAILED != ret, 1 );
      if( WAIT_TIMEOUT == ret ) {
         retVal = UTIL_OVERFLOW;
      }
   }
   else {
      OS_ERROR_IF( WaitForSingleObject( This->event, INFINITE ), WAIT_FAILED );
   }
#else
   OS_CHECK( pthread_mutex_lock( &This->condLock ));
   This->signaled = false;
   while( ! This->signaled ) {
      if( deadline ) {
         int ret = pthread_cond_timedwait( &This->condition, &This->condLock, deadline );
         if( ETIMEDOUT == ret ) {
            retVal = UTIL_OVERFLOW;
            break;
         }
         if( ret ) {
            retVal = UTIL_OS_ERROR;
            break;
         }
      }
      else {
         OS_CHECK( pthread_cond_wait( &This->condition, &This->condLock ));
      }
   }
   OS_CHECK( pthread_mutex_unlock( &This->condLock ));
#endif
   return retVal;
}

util_error os_event_signal( os_event * This ) {
   UTIL_CHECK_NON_NULL( This );
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   if( ! SetEvent( This->event )) {
      retVal = UTIL_OS_ERROR;
   }
#else
   OS_CHECK( pthread_mutex_lock( &This->condLock ));
   This->signaled = true;
   OS_CHECK( pthread_cond_signal ( &This->condition ));
   OS_CHECK( pthread_mutex_unlock( &This->condLock ));
#endif
   return retVal;
}
