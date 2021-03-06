#include <os/Thread.hpp>

#include <util/Exceptions.hpp>
#include <util/Log.hpp>

#include <stdio.h>

using namespace os;

ThreadType Thread::self( void ) {
#ifdef _WIN32
   return GetCurrentThread();
#else
   return pthread_self();
#endif
}

void Thread::cancel( ThreadType thread ) {
#ifdef _WIN32
   ExitThread( GetThreadId( thread ));
#else
   pthread_cancel( thread );
#endif
}

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

void * Thread::executor( void * This ) {
   Thread * t = (Thread *)This;
   t->_function();
   return nullptr;
}

Thread::Thread( std::function<void(void)> function ) {
   _function = function;
#ifdef _WIN32
   _thread = CreateThread( 0, 0, (LPTHREAD_START_ROUTINE)Thread::executor, this, 0, 0 );
   if( ! _thread ) {
      throw util::Runtime( UTIL_CTXT, "CreateThread" );
   }
#else
   if( pthread_create( &_thread, 0, Thread::executor, this )) {
      throw util::Runtime( UTIL_CTXT, "pthread_create" );
   }
#endif
}

Thread::~Thread() {
#ifdef _WIN32
   if( ! CloseHandle( _thread )) {
      UTIL_LOG_MSG( util::Runtime( UTIL_CTXT, "CloseHandle" ).what());
   }
#endif
}

void Thread::cancel() {
#ifdef _WIN32
   ExitThread( 0 );
#else
   pthread_cancel( pthread_self());
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
