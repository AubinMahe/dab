#pragma once

#ifdef _WIN32
#   include <os/win32.hpp>
#else
#   include <pthread.h>
#   include <errno.h>
#endif
#include <time.h>

namespace os {

   class Event {
   public:

      Event( void );
      ~ Event( void );

   public:

      /**
       * Return true when signaled, false when a deadline has been specified and a timeout occurs
       */
      bool wait( const timespec * deadline = nullptr );
      void signal( void );

   private:
      bool            _signaled;
#ifdef _WIN32
      HANDLE          _event;
#else
      pthread_mutex_t _condLock;
      pthread_cond_t  _condition;
#endif
   private:
      Event( const Event & ) = delete;
      Event & operator = ( const Event & ) = delete;
   };
}
