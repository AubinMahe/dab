#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#ifdef _WIN32
#  include <os/win32.h>
#else
#  include <pthread.h>
#endif

#include <util/error_codes.h>

typedef struct os_thread_s {
#ifdef _WIN32
   HANDLE    thread;
#else
   pthread_t thread;
#endif
} os_thread;

typedef void * ( * thread_entry_t )( void * user_context );

util_error os_thread_create ( os_thread * This, thread_entry_t entry, void * user_context );
util_error os_thread_destroy( os_thread * This );
util_error os_thread_detach ( os_thread * This );
util_error os_thread_join   ( os_thread * This, void ** returnedValue );

#ifdef __cplusplus
}
#endif
