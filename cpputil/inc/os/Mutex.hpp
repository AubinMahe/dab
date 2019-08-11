#pragma once

#include <pthread.h>

namespace os {

   class Mutex {
   public:

      Mutex( bool synchronizedBlock = false );
      ~ Mutex( void );

   public:

      void take( void );
      void release( void );

   private:

      bool            _synchronizedBlock;
      pthread_mutex_t _mutex;

   private:
      Mutex( const Mutex & ) = delete;
      Mutex & operator = ( const Mutex & ) = delete;
   };
}
