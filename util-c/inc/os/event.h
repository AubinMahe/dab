#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#ifdef _WIN32
#  include <os/win32.h>
#else
#  include <pthread.h>
#endif
#include <time.h>

#include "../types.h"
#include <util/error_codes.h>

typedef struct os_event_s {
#ifdef _WIN32
   HANDLE          event;
#else
   bool            signaled;
   pthread_mutex_t condLock;
   pthread_cond_t  condition;
#endif
} os_event;

util_error os_event_init   ( os_event * This );
util_error os_event_destroy( os_event * This );
util_error os_event_wait   ( os_event * This, const struct timespec * deadline );
util_error os_event_signal ( os_event * This );

#ifdef __cplusplus
}
#endif
