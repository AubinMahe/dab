#pragma once

#include <os/Event.hpp>
#include <time.h>

#include <functional>

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

   class TimeoutCallback : public util::Timeout {
   public:

      TimeoutCallback( unsigned milliseconds, std::function<void()> action ) :
         util::Timeout( milliseconds ),
         _action      ( action       )
      {}

   public:

      virtual void action( void ) {
         _action();
      }

   private:

      std::function<void()> _action;
   };
}
