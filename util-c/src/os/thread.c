#include <os/thread.h>

util_error os_thread_create ( os_thread * This, thread_entry_t entry, void * user_context ) {
   util_error retVal = UTIL_NO_ERROR;
#ifdef _WIN32
   This->thread = CreateThread( 0, 0, (LPTHREAD_START_ROUTINE)entry, user_context, 0, 0 );
#else
   if( pthread_create( &This->thread, 0, entry, user_context )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
}

util_error os_thread_destroy( os_thread * This ) {
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
   util_error retVal = UTIL_NO_ERROR;
#ifndef _WIN32
   if( pthread_detach( This->thread )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
   (void)This;
}

util_error os_thread_join( os_thread * This, void ** returnedValue ) {
   util_error retVal = UTIL_NO_ERROR;
   if( returnedValue ) {
      *returnedValue = NULL;
   }
#ifdef _WIN32
   WaitForSingleObject( &This->thread, INFINITE );
#else
   if( pthread_join( This->thread, returnedValue )) {
      retVal = UTIL_OS_ERROR;
   }
#endif
   return retVal;
   (void)This;
}
