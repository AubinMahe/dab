#pragma once

#ifdef _WIN32
#  include <os/win32.hpp>
#  define ThreadType HANDLE
#else
#  include <pthread.h>
#  define ThreadType pthread_t
#endif

#include <functional>

namespace os {

   typedef void * ( * thread_entry_t )( void * user_context );

   class Thread {
   public:

      static ThreadType self( void );

      static void cancel( ThreadType thread );

   public:

      Thread( thread_entry_t entry, void * user_context );
      Thread( std::function<void(void)> entry );
      ~Thread();

   public:

      void cancel( void );
      void detach( void );
      void join  ( void ** returnedValue = 0 );

   private:

      static void * executor( void * This );

      std::function<void(void)> _function;
      void *                    _argument;
      ThreadType                _thread;
   };
}
