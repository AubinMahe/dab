#include <os/thread.h>
#include <os/errors.h>

util_error os_thread_create ( os_thread * This, thread_entry_t entry, void * user_context ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   This->thread = CreateThread( 0, 0, (LPTHREAD_START_ROUTINE)entry, user_context, 0, 0 );
#else
   OS_CHECK( pthread_create( &This->thread, 0, entry, user_context ), __FILE__, __LINE__ );
#endif
   return retVal;
}

util_error os_thread_destroy( os_thread * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   if( ! CloseHandle( This->thread )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
   (void)This;
}

util_error os_thread_detach ( os_thread * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   util_error retVal = UTIL_NO_ERROR;
#ifndef _WIN32
   OS_CHECK( pthread_detach( This->thread ), __FILE__, __LINE__ );
#endif
   return retVal;
   (void)This;
}

util_error os_thread_join( os_thread * This, void ** returnedValue ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   util_error retVal = UTIL_NO_ERROR;
   if( returnedValue ) {
      *returnedValue = NULL;
   }
#ifdef _WIN32
   OS_CHECK( WAIT_FAILED != WaitForSingleObject( &This->thread, INFINITE ), __FILE__, __LINE__ );
#else
   OS_CHECK( pthread_join( This->thread, returnedValue ), __FILE__, __LINE__ );
#endif
   return retVal;
   (void)This;
}
