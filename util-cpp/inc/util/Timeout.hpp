#pragma once

#include <pthread.h>
#include <time.h>

namespace util {

   class Timeout {
   public:

      Timeout( unsigned milliseconds );
      virtual ~ Timeout() = default;

   public:

      void start( void );

      void cancel( void );

   public:

      virtual void action( void ) = 0;

   private:

      static void * waiting( void * arg );

   private:

      unsigned        _delayMs;
      struct timespec _deadline;
      pthread_cond_t  _cond;
      pthread_mutex_t _mutex;

   private:
      Timeout( const Timeout & ) = delete;
      Timeout & operator = ( const Timeout & ) = delete;
   };

   template<class T>
   class TimeoutCallBack : public util::Timeout {
   public:

      typedef void (T::* method_t )( void );

   private:

      T &      _this;
      method_t _method;

   public:

      TimeoutCallBack( T & t, unsigned milliseconds, method_t method ) :
         util::Timeout( milliseconds ),
         _this  ( t      ),
         _method( method )
      {}

   public:

      virtual void action( void ) {
         (_this.*_method)();
      }
   };
}
