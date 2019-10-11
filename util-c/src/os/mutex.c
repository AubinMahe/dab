#include <os/mutex.h>

util_error os_mutex_init( os_mutex * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   This->mutex = CreateMutex( NULL, FALSE, NULL );
#else
   if( pthread_mutex_init( &This->mutex, 0 )) {
      return UTIL_OS_ERROR;
   }
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_destroy( os_mutex * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   CloseHandle( This->mutex );
#else
   pthread_mutex_destroy( &This->mutex );
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_take( os_mutex * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   if( ! WaitForSingleObject( This->mutex, INFINITE ))
#else
   if( pthread_mutex_lock( &This->mutex ))
#endif
   {
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error os_mutex_release( os_mutex * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   if( ! ReleaseMutex( This->mutex ))
#else
   if( pthread_mutex_unlock( &This->mutex ))
#endif
   {
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}
