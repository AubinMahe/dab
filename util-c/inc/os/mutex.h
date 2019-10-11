#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#ifdef _WIN32
#  include <os/win32.h>
#else
#  include <pthread.h>
#endif

#include "../types.h"
#include <util/error_codes.h>

typedef struct os_mutex_s {

#ifdef _WIN32
   HANDLE          mutex;
#else
   pthread_mutex_t mutex;
#endif

} os_mutex;

util_error os_mutex_init   ( os_mutex * This );
util_error os_mutex_destroy( os_mutex * This );
util_error os_mutex_take   ( os_mutex * This );
util_error os_mutex_release( os_mutex * This );

#ifdef __cplusplus
}
#endif
