#include <os/mutex.h>
#include <os/errors.h>

util_error os_mutex_init( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
#ifdef _WIN32
   OS_CHECK( NULL != ( This->mutex = CreateMutex( NULL, FALSE, NULL )), __FILE__, __LINE__ );
#else
   OS_CHECK( pthread_mutex_init( &This->mutex, 0 ), __FILE__, __LINE__ );
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_destroy( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
#ifdef _WIN32
   OS_CHECK( ! CloseHandle( This->mutex ), __FILE__, __LINE__ );
#else
   OS_CHECK( pthread_mutex_destroy( &This->mutex ), __FILE__, __LINE__ );
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_take( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
#ifdef _WIN32
   OS_CHECK( WAIT_FAILED != WaitForSingleObject( This->mutex, INFINITE ), __FILE__, __LINE__ );
#else
   OS_CHECK( pthread_mutex_lock( &This->mutex ), __FILE__, __LINE__ );
#endif
   return UTIL_NO_ERROR;
}

util_error os_mutex_release( os_mutex * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
#ifdef _WIN32
   OS_CHECK( ! ReleaseMutex( This->mutex ), __FILE__, __LINE__ );
#else
   OS_CHECK( pthread_mutex_unlock( &This->mutex ), __FILE__, __LINE__ );
#endif
   return UTIL_NO_ERROR;
}
