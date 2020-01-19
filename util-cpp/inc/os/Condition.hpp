#pragma once

#include <os/Mutex.hpp>
#ifdef _WIN32
#   include <os/win32.hpp>
#else
#   include <pthread.h>
#   include <errno.h>
#endif
#include <time.h>

namespace os {

   class Condition {
   public:

      Condition( void );
      ~ Condition( void );

   public:

      /**
       * Return true when signaled, false when a deadline has been specified and a timeout occurs
       */
      bool wait( Mutex & mutex, const timespec * deadline = nullptr );
      void signal( void );

   private:
      bool            _signaled;
#ifdef _WIN32
      HANDLE          _event;
#else
      pthread_cond_t  _condition;
#endif
   private:
      Condition( const Condition & ) = delete;
      Condition & operator = ( const Condition & ) = delete;
   };
}
