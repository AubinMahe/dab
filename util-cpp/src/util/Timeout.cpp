#include <util/Timeout.hpp>

#include <os/StdApiException.hpp>

#include <pthread.h>
#include <stdio.h>      // perror
#include <string.h>     // memset
#include <sys/time.h>   // gettimeofday
#include <errno.h>

using namespace util;

Timeout::Timeout( unsigned milliseconds ) :
   _delayMs( milliseconds              ),
   _cond   ( PTHREAD_COND_INITIALIZER  ),
   _mutex  ( PTHREAD_MUTEX_INITIALIZER )
{}

void * Timeout::waiting( void * arg ) {
   Timeout * t = (Timeout *)arg;
   if( pthread_mutex_lock( &t->_mutex )) {
      throw os::StdApiException( "util::Timeout::waiting|pthread_mutex_lock", __FILE__, __LINE__ );
   }
   int status = pthread_cond_timedwait( &t->_cond, &t->_mutex, &t->_deadline );
   if( status == ETIMEDOUT ) {
      t->action();
   }
   else if( status ) {
      throw os::StdApiException( "util::Timeout::waiting|pthread_cond_timedwait", __FILE__, __LINE__ );
   }
   if( pthread_mutex_unlock( &t->_mutex )) {
      throw os::StdApiException( "util::Timeout::cancel|pthread_mutex_unlock", __FILE__, __LINE__ );
   }
   return NULL;
}

void Timeout::start( void ) {
   struct timeval tv;
   if( gettimeofday( &tv, NULL )) {
      throw os::StdApiException( "util::Timeout::start|gettimeofday", __FILE__, __LINE__ );
   }
   unsigned sec = _delayMs / 1000U;
   unsigned ms  = _delayMs % 1000U;
   _deadline.tv_sec  = tv.tv_sec + sec;
   _deadline.tv_nsec = 1000U * ( tv.tv_usec + 1000U * ms );
   _deadline.tv_sec  += _deadline.tv_nsec / 1000000000U;
   _deadline.tv_nsec %= 1000000000U;
   pthread_t thread;
   if( pthread_create( &thread, NULL, waiting, this )) {
      throw os::StdApiException( "util::Timeout::start|pthread_create", __FILE__, __LINE__ );
   }
   if( pthread_detach( thread )) {
      throw os::StdApiException( "util::Timeout::start|pthread_detach", __FILE__, __LINE__ );
   }
}

void Timeout::cancel( void ) {
   if( pthread_mutex_lock( &_mutex )) {
      throw os::StdApiException( "util::Timeout::cancel|pthread_mutex_lock", __FILE__, __LINE__ );
   }
   if( pthread_cond_signal( &_cond )) {
      throw os::StdApiException( "util::Timeout::cancel|pthread_cond_signal", __FILE__, __LINE__ );
   }
   if( pthread_mutex_unlock( &_mutex )) {
      throw os::StdApiException( "util::Timeout::cancel|pthread_mutex_unlock", __FILE__, __LINE__ );
   }
}
