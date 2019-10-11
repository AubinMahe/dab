#include <os/event.h>
#include <errno.h>

util_error os_event_init( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   This->event = CreateEvent( NULL, FALSE, FALSE, NULL );
#else
   if(   pthread_mutex_init( &This->condLock , 0 )
      || pthread_cond_init ( &This->condition, 0 ))
   {
      return UTIL_OS_ERROR;
   }
#endif
   return UTIL_NO_ERROR;
}

util_error os_event_destroy( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
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
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   if( deadline ) {
      FILETIME utcTime;
      GetSystemTimeAsFileTime( &utcTime );
      ULARGE_INTEGER utcTime_100_nanos;
      utcTime_100_nanos.u.LowPart  = utcTime.dwLowDateTime;
      utcTime_100_nanos.u.HighPart = utcTime.dwHighDateTime;
      long deadline_ms = 10000000 * deadline->tv_sec  + deadline->tv_nsec / 100;
      unsigned long timeout = (unsigned long)((((ULONGLONG)deadline_ms) - utcTime_100_nanos.QuadPart ) / 10000UL );
      WaitForSingleObject( This->event, timeout );
   }
   else {
      WaitForSingleObject( This->event, INFINITE );
   }
#else
   if( pthread_mutex_lock( &This->condLock )) {
      return UTIL_OS_ERROR;
   }
   This->signaled = false;
   while( ! This->signaled ) {
      if( deadline ) {
         int ret = pthread_cond_timedwait( &This->condition, &This->condLock, deadline );
         if( ETIMEDOUT == ret ) {
            retVal = UTIL_OVERFLOW;
         }
         else {
            retVal = UTIL_OS_ERROR;
         }
      }
      else {
         if( pthread_cond_wait( &This->condition, &This->condLock )) {
            retVal = UTIL_OS_ERROR;
         }
      }
   }
   if( pthread_mutex_unlock( &This->condLock )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
}

util_error os_event_signal( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   if( ! SetEvent( This->event )) {
      retVal = UTIL_OS_ERROR;
   }
#else
   if( pthread_mutex_lock( &This->condLock )) {
      return UTIL_OS_ERROR;
   }
   This->signaled = true;
   if( pthread_cond_signal ( &This->condition )) {
      retVal = UTIL_OS_ERROR;
   }
   if( pthread_mutex_unlock( &This->condLock )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
}
