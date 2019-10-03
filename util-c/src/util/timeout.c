#include <util/timeout.h>

#include <stdio.h>      // perror
#include <string.h>     // memset
#include <sys/time.h>   // gettimeofday
#include <errno.h>

#define MILLIS_PER_SECOND 1000U
#define MICROS_PER_MILLI  1000U
#define MICROS_PER_NANO   1000U
#define NANOS_PER_SECOND  1000000000U

static void * util_timeout_waiting( void * arg ) {
   util_timeout * This = (util_timeout *)arg;
   if( pthread_mutex_lock( &This->mutex )) {
      perror( "pthread_mutex_lock" );
      return NULL;
   }
   int status = pthread_cond_timedwait( &This->cond, &This->mutex, &This->deadline );
   if( status == ETIMEDOUT ) {
      This->action( This->user_context );
   }
   else if( status ){
      perror( "pthread_cond_timedwait" );
   }
   if( pthread_mutex_unlock( &This->mutex )) {
      perror( "pthread_mutex_unlock" );
   }
   return NULL;
}

util_error util_timeout_init( util_timeout * This, unsigned milliseconds, util_timeout_action action, void * user_context ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( pthread_cond_init( &This->cond, NULL )) {
      return UTIL_OS_ERROR;
   }
   if( pthread_mutex_init( &This->mutex, NULL )) {
      return UTIL_OS_ERROR;
   }
   This->delay_sec    = milliseconds / MILLIS_PER_SECOND;
   This->delay_ms     = milliseconds % MILLIS_PER_SECOND;
   This->action       = action;
   This->user_context = user_context;
   return UTIL_NO_ERROR;
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
   This->deadline.tv_sec  = tv.tv_sec + This->delay_sec;
   This->deadline.tv_nsec = MICROS_PER_NANO * ( tv.tv_usec + MICROS_PER_MILLI * This->delay_ms );
   This->deadline.tv_sec  += This->deadline.tv_nsec / NANOS_PER_SECOND;
   This->deadline.tv_nsec %= NANOS_PER_SECOND;
   pthread_t thread;
   if( pthread_create( &thread, NULL, util_timeout_waiting, This )) {
      perror( "pthread_create" );
      return UTIL_OS_ERROR;
   }
   if( pthread_detach( thread )) {
      perror( "pthread_detach" );
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error util_timeout_cancel( util_timeout * This ) {
   if( ! This ) {
      return UTIL_NULL_ARG;
   }
   if( pthread_mutex_lock( &This->mutex )) {
      return UTIL_OS_ERROR;
   }
   if( pthread_cond_signal( &This->cond )) {
      return UTIL_OS_ERROR;
   }
   if( pthread_mutex_unlock( &This->mutex )) {
      perror( "pthread_mutex_unlock" );
   }
   return UTIL_NO_ERROR;
}
