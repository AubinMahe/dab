#pragma once

#ifdef _WIN32
#   include <os/win32.hpp>
#else
#   include <pthread.h>
#endif


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
#ifdef _WIN32
      HANDLE          _mutex;
#else
      pthread_mutex_t _mutex;
#endif
   private:
      Mutex( const Mutex & ) = delete;
      Mutex & operator = ( const Mutex & ) = delete;
   };
}
