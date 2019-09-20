#pragma once

#include <pthread.h>

namespace os {

   class Event {
   public:

      Event( void );
      ~ Event( void );

   public:

      void wait( void );
      void signal( void );

   private:

      bool            _signaled;
      pthread_mutex_t _condLock;
      pthread_cond_t  _condition;

   private:
      Event( const Event & ) = delete;
      Event & operator = ( const Event & ) = delete;
   };
}
