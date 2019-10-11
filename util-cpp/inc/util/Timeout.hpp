#pragma once

#include <os/Event.hpp>
#include <time.h>

namespace util {

   class Timeout {
   public:

      enum State {
         INITIALIZED,
         RUNNING,
         CANCELED,
         ELAPSED
      };

   public:

      Timeout( unsigned milliseconds );
      virtual ~ Timeout() = default;

   public:

      void start( void );

      void cancel( void );

      State getState( void ) const  { return _state; }

   public:

      virtual void action( void ) = 0;

   private:

      static void * waiting( void * arg );

   private:

      unsigned  _delayMs;
      timespec  _deadline;
      os::Event _event;
      State     _state;

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
