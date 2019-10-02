#include <util/timeout.h>

#include <pthread.h>
#include <stdio.h>      // perror
#include <string.h>     // memset
#include <sys/time.h>   // gettimeofday
#include <errno.h>

#define UTIL_TIMEOUT_COUNT 20

typedef struct timeout_s {
   pthread_cond_t      cond;
   pthread_mutex_t     mutex;
   struct timespec     deadline;
   util_timeout_action action;
   void *              user_context;
} timeout;

static timeout         timeouts[UTIL_TIMEOUT_COUNT];
static pthread_mutex_t timeouts_mutex = PTHREAD_MUTEX_INITIALIZER;
static bool            init  = true;

static void * util_timeout_waiting( void * arg ) {
   timeout * t = (timeout *)arg;
   if( pthread_mutex_lock( &t->mutex )) {
      perror( "pthread_mutex_lock" );
      return NULL;
   }
   int status = pthread_cond_timedwait( &t->cond, &t->mutex, &t->deadline );
   if( status == ETIMEDOUT ) {
      t->action( t->user_context );
   }
   else if( status ){
      perror( "pthread_cond_timedwait" );
   }
   if( pthread_mutex_unlock( &t->mutex )) {
      perror( "pthread_mutex_unlock" );
   }
   if( pthread_mutex_lock( &timeouts_mutex )) {
      perror( "pthread_mutex_lock" );
   }
   t->action = NULL;
   if( pthread_mutex_unlock( &timeouts_mutex )) {
      perror( "pthread_mutex_unlock" );
   }
   return NULL;
}

util_error util_timeout_start( unsigned milliseconds, util_timeout_action action, void * user_context, util_timeout * id ) {
   struct timeval tv;
   if( gettimeofday( &tv, NULL )) {
      perror( "gettimeofday" );
      return UTIL_OS_ERROR;
   }
   if( ! action || !milliseconds ) {
      return UTIL_NULL_ARG;
   }
   if( pthread_mutex_lock( &timeouts_mutex )) {
      return UTIL_OS_ERROR;
   }
   if( init ) {
      init = false;
      memset( timeouts, 0, sizeof( timeouts ));
      for( unsigned i = 0; i < UTIL_TIMEOUT_COUNT; ++i ) {
         if(   pthread_cond_init ( &timeouts[i].cond , NULL )
            || pthread_mutex_init( &timeouts[i].mutex, NULL ))
         {
            return UTIL_OS_ERROR;
         }
      }
   }
   timeout * t = NULL;
   for( unsigned i = 0; ( t == NULL )&&( i < UTIL_TIMEOUT_COUNT ); ++i ) {
      if( timeouts[i].action == NULL ) {
         t = timeouts + i;
         t->action       = action;
         t->user_context = user_context;
      }
   }
   *id = t;
   if( pthread_mutex_unlock( &timeouts_mutex )) {
      return UTIL_OS_ERROR;
   }
   if( t == NULL ) {
      return UTIL_MEMORY_FULL;
   }
   unsigned sec = milliseconds / 1000U;
   unsigned ms  = milliseconds % 1000U;
   t->deadline.tv_sec  = tv.tv_sec + sec;
   t->deadline.tv_nsec = 1000U * ( tv.tv_usec + 1000U * ms );
   t->deadline.tv_sec  += t->deadline.tv_nsec / 1000000000U;
   t->deadline.tv_nsec %= 1000000000U;
   pthread_t thread;
   if( pthread_create( &thread, NULL, util_timeout_waiting, t )) {
      perror( "pthread_create" );
      return UTIL_OS_ERROR;
   }
   if( pthread_detach( thread )) {
      perror( "pthread_detach" );
      return UTIL_OS_ERROR;
   }
   return UTIL_NO_ERROR;
}

util_error util_timeout_cancel( util_timeout id ) {
   if( ! id ) {
      return UTIL_NULL_ARG;
   }
   timeout * t = (timeout *)id;
   if( pthread_mutex_lock( &t->mutex )) {
      return UTIL_OS_ERROR;
   }
   if( pthread_cond_signal( &t->cond )) {
      return UTIL_OS_ERROR;
   }
   if( pthread_mutex_unlock( &t->mutex )) {
      perror( "pthread_mutex_unlock" );
   }
   return UTIL_NO_ERROR;
}
