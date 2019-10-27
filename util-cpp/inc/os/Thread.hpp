#pragma once

#ifdef _WIN32
#  include <os/win32.hpp>
#  define ThreadType HANDLE
#else
#  include <pthread.h>
#  define ThreadType pthread_t
#endif

namespace os {

   typedef void * ( * thread_entry_t )( void * user_context );

   class Thread {
   public:

      static ThreadType self( void );

      static void cancel( ThreadType thread );

   public:

      Thread( thread_entry_t entry, void * user_context );
      ~Thread();

   public:

      void cancel( void );
      void detach( void );
      void join  ( void ** returnedValue = 0 );

   private:

      ThreadType _thread;
   };
}
