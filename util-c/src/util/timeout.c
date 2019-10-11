#include <os/thread.h>
#include <util/timeout.h>

#include <stdio.h>      // perror
#include <string.h>     // memset
#include <sys/time.h>   // gettimeofday
#include <errno.h>

#define MILLIS_PER_SECOND 1000
#define MICROS_PER_MILLI  1000
#define MICROS_PER_NANO   1000
#define NANOS_PER_SECOND  1000000000

static void * util_timeout_waiting( void * arg ) {
   util_timeout * This = (util_timeout *)arg;
   int status = os_event_wait( &This->event, &This->deadline );
   if( status == UTIL_OVERFLOW ) {
      This->action( This->user_context );
   }
   else if( status ) {
      perror( "pthread_cond_timedwait" );
   }
   return NULL;
}

util_error util_timeout_init( util_timeout * This, unsigned milliseconds, util_timeout_action action, void * user_context ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   memset( This, 0, sizeof( util_timeout ));
   util_error retVal = os_event_init( &This->event );
   if( UTIL_NO_ERROR == retVal ) {
      This->delay_sec    = milliseconds / MILLIS_PER_SECOND;
      This->delay_ms     = milliseconds % MILLIS_PER_SECOND;
      This->action       = action;
      This->user_context = user_context;
   }
   return retVal;
}

util_error util_timeout_destroy( util_timeout * This ) {
   return os_event_destroy( &This->event );
}

util_error util_timeout_start( util_timeout * This ) {
   struct timeval tv;
   if( gettimeofday( &tv, NULL )) {
      perror( "gettimeofday" );
      return UTIL_OS_ERROR;
   }
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
#ifdef _WIN32
   This->deadline.tv_sec   = tv.tv_sec + (long)This->delay_sec;
   This->deadline.tv_nsec  = MICROS_PER_NANO * ( tv.tv_usec + (long)( MICROS_PER_MILLI * This->delay_ms ));
   This->deadline.tv_sec  += (long)( This->deadline.tv_nsec / NANOS_PER_SECOND );
   This->deadline.tv_nsec %= (long)NANOS_PER_SECOND;
#else
   This->deadline.tv_sec  = tv.tv_sec + This->delay_sec;
   This->deadline.tv_nsec = MICROS_PER_NANO * ( tv.tv_usec + MICROS_PER_MILLI * This->delay_ms );
   This->deadline.tv_sec  += This->deadline.tv_nsec / NANOS_PER_SECOND;
   This->deadline.tv_nsec %= NANOS_PER_SECOND;
#endif
   os_thread thread;
   util_error retVal = os_thread_create( &thread, util_timeout_waiting, This );
   if( UTIL_NO_ERROR == retVal ) {
      retVal = os_thread_detach( &thread );
   }
   return retVal;
}

util_error util_timeout_cancel( util_timeout * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   return os_event_signal( &This->event );
}
