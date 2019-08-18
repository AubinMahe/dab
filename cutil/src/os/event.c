#include <os/event.h>

util_error os_event_new( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   This->event = CreateEvent( NULL, FALSE, FALSE, NULL );
#else
   if( pthread_mutex_init( &This->condLock, 0 )|| pthread_cond_init( &This->condition, 0 )) {
      return UTIL_OS_ERROR;
   }
#endif
   return UTIL_NO_ERROR;
}

util_error os_event_delete( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   if( ! CloseHandle( This->event  ))
#else
   if( pthread_mutex_destroy( &This->condLock )|| pthread_cond_destroy ( &This->condition ))
#endif
   {
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error os_event_wait( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   WaitForSingleObject( This->event, INFINITE );
#else
   if( pthread_mutex_lock( &This->condLock )) {
      return UTIL_OS_ERROR;
   }
   This->signaled = false;
   while( ! This->signaled ) {
      pthread_cond_wait( &This->condition, &This->condLock );
   }
   if( pthread_mutex_unlock( &This->condLock )) {
      return UTIL_OS_ERROR;
   }
#endif
   return UTIL_NO_ERROR;
}

util_error os_event_signal( os_event * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   if( ! SetEvent( This->event )) {
      return UTIL_OS_ERROR;
   }
#else
   if( pthread_mutex_lock( &This->condLock )) {
      return UTIL_OS_ERROR;
   }
   This->signaled = true;
   pthread_cond_signal ( &This->condition );
   if( pthread_mutex_unlock( &This->condLock )) {
      return UTIL_OS_ERROR;
   }
#endif
   return UTIL_NO_ERROR;
}
