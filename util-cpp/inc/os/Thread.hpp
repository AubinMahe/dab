#pragma once

#ifdef _WIN32
#  include <os/win32.hpp>
#else
#  include <pthread.h>
#endif

namespace os {

   typedef void * ( * thread_entry_t )( void * user_context );

   class Thread {
   public:

      Thread( thread_entry_t entry, void * user_context );
      ~Thread();

   public:

      void detach( void );
      void join  ( void ** returnedValue = 0 );

   private:
#ifdef _WIN32
      HANDLE    _thread;
#else
      pthread_t _thread;
#endif
   };
}
