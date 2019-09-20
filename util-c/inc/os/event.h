#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include "../types.h"
#include <util/error_codes.h>
#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <windows.h>
#else
#  include <pthread.h>
#endif

typedef struct os_event_s {
#ifdef _WIN32
   HANDLE          event;
#else
   bool            signaled;
   pthread_mutex_t condLock;
   pthread_cond_t  condition;
#endif
} os_event;

util_error os_event_new   ( os_event * This );
util_error os_event_delete( os_event * This );
util_error os_event_wait  ( os_event * This );
util_error os_event_signal( os_event * This );

#ifdef __cplusplus
}
#endif
