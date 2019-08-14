#pragma once

#include "../types.h"
#include <util/error_codes.h>
#ifdef _WIN32
#  define WIN32_LEAN_AND_MEAN
#  include <windows.h>
#else
#include <pthread.h>
#endif

typedef struct os_mutex_tag {

#ifdef _WIN32
   HANDLE          mutex;
#else
   pthread_mutex_t mutex;
#endif

} os_mutex_t;
