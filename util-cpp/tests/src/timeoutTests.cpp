#include <util/Timeout.hpp>
#include <util/Exceptions.hpp>

#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <sys/time.h>

extern int nanosleep( const struct timespec *req, struct timespec *rem );

class Timer : public util::Timeout {
public:

   Timer( unsigned milliseconds, bool exp ) :
      util::Timeout( milliseconds ),
      delay( milliseconds ),
      expected( exp )
   {}

public:

   virtual void action( void ) {
      struct timeval now;
      if( gettimeofday( &now, NULL )) {
         throw util::Runtime( UTIL_CTXT, "gettimeofday" );
      }
      if( perf ) {
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
         fprintf( stderr, "action t+%4u;%9.4f;%6.4f %%\n", delay, ms, (100.0*(ms - delay))/(double)delay );
      }
      else {
         fprintf( stderr, "%s: action t+%4u\n", expected ? "PASS" : "FAIL", delay );
      }
   }

public:

   static bool    perf;
   static timeval atStart;

private:

   unsigned delay;
   bool     expected;
};

bool    Timer::perf    = false;
timeval Timer::atStart = { 0, 0 };

void timeoutTests( bool perf ) {
   printf( "--- util::timeout ---\n" );
   Timer::perf = perf;
   if( perf && gettimeofday( &Timer::atStart, NULL )) {
      throw util::Runtime( UTIL_CTXT, "gettimeofday" );
   }
   Timer timeout___50(   50, true );
   Timer timeout___75(   75, true );
   Timer timeout__100(  100, true );
   Timer timeout__200(  200, true );
   Timer timeout_1500( 1500, true );
   Timer timeout_9000( 9000, false );
   timeout__100.start();
   timeout_9000.start();
   timeout__200.start();
   timeout___50.start();
   timeout___75.start();
   timeout_1500.start();
   if( perf ) {
#ifdef _WIN32
      Sleep( 12*1000 );
#else
      timespec step = { 12, 0 };
      if( nanosleep( &step, NULL )) {
         throw util::Runtime( UTIL_CTXT, "nanosleep" );
      }
#endif
   }
   else {
#ifdef _WIN32
      Sleep( 3*1000 );
#else
      timespec step = { 3, 0 };
      if( nanosleep( &step, NULL )) {
         throw util::Runtime( UTIL_CTXT, "nanosleep" );
      }
#endif
      timeout_9000.cancel();
#ifdef _WIN32
      Sleep( 9*1000 );
#else
      step.tv_sec  = 9;
      step.tv_nsec = 0;
      if( nanosleep( &step, NULL )) {
         throw util::Runtime( UTIL_CTXT, "nanosleep" );
      }
#endif
      if( timeout___50.getState() != util::Timeout::ELAPSED ) {
         fprintf( stderr, "FAIL: action t+  50\n" );
      }
      if( timeout___75.getState() != util::Timeout::ELAPSED ) {
         fprintf( stderr, "FAIL: action t+  75\n" );
      }
      if( timeout__100.getState() != util::Timeout::ELAPSED ) {
         fprintf( stderr, "FAIL: action t+ 100\n" );
      }
      if( timeout__200.getState() != util::Timeout::ELAPSED ) {
         fprintf( stderr, "FAIL: action t+ 200\n" );
      }
      if( timeout_1500.getState() != util::Timeout::ELAPSED ) {
         fprintf( stderr, "FAIL: action t+1500\n" );
      }
      if( timeout_9000.getState() != util::Timeout::ELAPSED ) {
         fprintf( stderr, "PASS: action t+9000\n" );
      }
   }
}
