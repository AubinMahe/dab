#include <os/mutex.h>
#include <os/errors.h>

util_error os_mutex_init( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This );
#ifdef _WIN32
   OS_CHECK( NULL != ( This->mutex = CreateMutex( NULL, FALSE, NULL )));
#else
   OS_CHECK( pthread_mutex_init( &This->mutex, 0 ));
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_destroy( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This );
#ifdef _WIN32
   OS_CHECK( ! CloseHandle( This->mutex ));
#else
   OS_CHECK( pthread_mutex_destroy( &This->mutex ));
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_take( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This );
#ifdef _WIN32
   OS_CHECK( WAIT_FAILED != WaitForSingleObject( This->mutex, INFINITE ));
#else
   OS_CHECK( pthread_mutex_lock( &This->mutex ));
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_release( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This );
#ifdef _WIN32
   OS_CHECK( ! ReleaseMutex( This->mutex ));
#else
   OS_CHECK( pthread_mutex_unlock( &This->mutex ));
#endif
   return UTIL_NO_ERROR;
}
