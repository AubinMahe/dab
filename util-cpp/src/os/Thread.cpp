#include <os/Thread.hpp>
#include <util/Exceptions.hpp>

#include <stdio.h>

using namespace os;

Thread::Thread( thread_entry_t entry, void * user_context ) {
#ifdef _WIN32
   _thread = CreateThread( 0, 0, (LPTHREAD_START_ROUTINE)entry, user_context, 0, 0 );
   if( ! _thread ) {
      throw util::Runtime( UTIL_CTXT, "CreateThread" );
   }
#else
   if( pthread_create( &_thread, 0, entry, user_context )) {
      throw util::Runtime( UTIL_CTXT, "pthread_create" );
   }
#endif
}

Thread::~Thread() {
#ifdef _WIN32
   if( ! CloseHandle( _thread )) {
      fprintf( stderr, "%s\n", util::Runtime( UTIL_CTXT, "CloseHandle" ).what());
   }
#endif
}

void Thread::detach( void ) {
#ifndef _WIN32
   if( pthread_detach( _thread )) {
      throw util::Runtime( UTIL_CTXT, "pthread_detach" );
   }
#endif
}

void Thread::join( void ** returnedValue/* = 0 */) {
   if( returnedValue ) {
      *returnedValue = NULL;
   }
#ifdef _WIN32
   if( WAIT_FAILED == ::WaitForSingleObject( _thread, INFINITE )) {
      throw util::Runtime( UTIL_CTXT, "WaitForSingleObject" );
   }
#else
   if( pthread_join( _thread, returnedValue )) {
      throw util::Runtime( UTIL_CTXT, "pthread_join" );
   }
#endif
}
