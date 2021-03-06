#include <util/timeout.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <sys/time.h>

extern int nanosleep( const struct timespec *req, struct timespec *rem );

typedef struct timeout_context_s {
   unsigned delay;
   bool     perf;
   bool     expected;
} timeout_context;

static struct timeval atStart;

util_error action( void * arg ) {
   timeout_context * ctxt = (timeout_context *)arg;
   struct timeval now;
   if( gettimeofday( &now, NULL )) {
      perror( "gettimeofday" );
      return UTIL_OS_ERROR;
   }
   if( ctxt->perf ) {
      long sec  = now.tv_sec - atStart.tv_sec;
      long usec = 0;
      if( atStart.tv_usec > now.tv_usec ) {
         --sec;
         usec = ( 1000000U + now.tv_usec ) - atStart.tv_usec;
      }
      else {
         usec = now.tv_usec - atStart.tv_usec;
      }
      double ms = 1000*(double)sec + ((double)usec) / 1000.0;
      fprintf( stderr, "action t+%4u;%9.4f;%6.4f %%\n", ctxt->delay, ms, (100.0*(ms - ctxt->delay))/(double)ctxt->delay );
   }
   else {
      fprintf( stderr, "%s: action t+%4u\n", ctxt->expected ? "PASS" : "FAIL", ctxt->delay );
   }
   ctxt->delay = 0;
   return UTIL_NO_ERROR;
}

util_error util_timeout_tests( bool perf ) {
   printf( "--- util_timeout ---\n" );
   if( perf ) {
      if( gettimeofday( &atStart, NULL )) {
         perror( "gettimeofday" );
         return UTIL_OS_ERROR;
      }
   }
   timeout_context ctxt___50 = {   50, perf, true  };
   timeout_context ctxt___75 = {   75, perf, true  };
   timeout_context ctxt__100 = {  100, perf, true  };
   timeout_context ctxt__200 = {  200, perf, true  };
   timeout_context ctxt_1500 = { 1500, perf, true  };
   timeout_context ctxt_9000 = { 9000, perf, false };
   util_timeout timeout__100;
   util_timeout timeout_9000;
   util_timeout timeout__200;
   util_timeout timeout___50;
   util_timeout timeout___75;
   util_timeout timeout_1500;
   UTIL_ERROR_CHECK( util_timeout_init( &timeout__100,  100, action, &ctxt__100 ));
   UTIL_ERROR_CHECK( util_timeout_init( &timeout_9000, 9000, action, &ctxt_9000 ));
   UTIL_ERROR_CHECK( util_timeout_init( &timeout__200,  200, action, &ctxt__200 ));
   UTIL_ERROR_CHECK( util_timeout_init( &timeout___50,   50, action, &ctxt___50 ));
   UTIL_ERROR_CHECK( util_timeout_init( &timeout___75,   75, action, &ctxt___75 ));
   UTIL_ERROR_CHECK( util_timeout_init( &timeout_1500, 1500, action, &ctxt_1500 ));
   UTIL_ERROR_CHECK( util_timeout_start( &timeout__100 ));
   UTIL_ERROR_CHECK( util_timeout_start( &timeout_9000 ));
   UTIL_ERROR_CHECK( util_timeout_start( &timeout__200 ));
   UTIL_ERROR_CHECK( util_timeout_start( &timeout___50 ));
   UTIL_ERROR_CHECK( util_timeout_start( &timeout___75 ));
   UTIL_ERROR_CHECK( util_timeout_start( &timeout_1500 ));
   if( perf ) {
#ifdef _WIN32
      Sleep( 12*1000 );
#else
      struct timespec step = { 12, 0 };
      if( nanosleep( &step, NULL )) {
         perror( "nanosleep" );
         return UTIL_OS_ERROR;
      }
#endif
   }
   else {
#ifdef _WIN32
      Sleep( 3*1000 );
#else
      struct timespec step = { 3, 0 };
      if( nanosleep( &step, NULL )) {
         perror( "nanosleep" );
         return UTIL_OS_ERROR;
      }
#endif
      UTIL_ERROR_CHECK( util_timeout_cancel( &timeout_9000 ));
#ifdef _WIN32
      Sleep( 9*1000 );
#else
      step.tv_sec  = 9;
      step.tv_nsec = 0;
      if( nanosleep( &step, NULL )) {
         perror( "nanosleep" );
         return UTIL_OS_ERROR;
      }
#endif
      if( ctxt___50.delay ) {
         fprintf( stderr, "FAIL: action t+%4u\n", ctxt___50.delay );
      }
      if( ctxt___75.delay ) {
         fprintf( stderr, "FAIL: action t+%4u\n", ctxt___75.delay );
      }
      if( ctxt__100.delay ) {
         fprintf( stderr, "FAIL: action t+%4u\n", ctxt__100.delay );
      }
      if( ctxt__200.delay ) {
         fprintf( stderr, "FAIL: action t+%4u\n", ctxt__200.delay );
      }
      if( ctxt_1500.delay ) {
         fprintf( stderr, "FAIL: action t+%4u\n", ctxt_1500.delay );
      }
      if( ctxt_9000.delay ) {
         fprintf( stderr, "PASS: action t+%4u\n", ctxt_9000.delay );
      }
   }
   return UTIL_NO_ERROR;
}
