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
   else if( UTIL_NO_ERROR != status ) {
      fprintf( stderr, "%s:%d:os_event_wait:%s:%s\n", __FILE__, __LINE__-5, util_error_messages[status], strerror( errno ));
   }
   return NULL;
}

util_error util_timeout_init( util_timeout * This, unsigned milliseconds, util_timeout_action action, void * user_context ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   memset( This, 0, sizeof( util_timeout ));
   UTIL_ERROR_CHECK( os_event_init( &This->event ), __FILE__, __LINE__ );
   This->delay_sec    = milliseconds / MILLIS_PER_SECOND;
   This->delay_ms     = milliseconds % MILLIS_PER_SECOND;
   This->action       = action;
   This->user_context = user_context;
   return UTIL_NO_ERROR;
}

util_error util_timeout_destroy( util_timeout * This ) {
   UTIL_ERROR_CHECK( os_event_destroy( &This->event ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error util_timeout_start( util_timeout * This ) {
   struct timeval tv;
   UTIL_ERROR_CHECK( gettimeofday( &tv, NULL ), __FILE__, __LINE__ );
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   This->deadline.tv_sec   = tv.tv_sec + (long)This->delay_sec;
   This->deadline.tv_nsec  = MICROS_PER_NANO * ( tv.tv_usec + (long)( MICROS_PER_MILLI * This->delay_ms ));
   This->deadline.tv_sec  += (long)( This->deadline.tv_nsec / NANOS_PER_SECOND );
   This->deadline.tv_nsec %= (long)NANOS_PER_SECOND;
   os_thread thread;
   UTIL_ERROR_CHECK( os_thread_create( &thread, util_timeout_waiting, This ), __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( os_thread_detach( &thread ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}

util_error util_timeout_cancel( util_timeout * This ) {
   UTIL_CHECK_NON_NULL( This, __FILE__, __LINE__ );
   UTIL_ERROR_CHECK( os_event_signal( &This->event ), __FILE__, __LINE__ );
   return UTIL_NO_ERROR;
}
