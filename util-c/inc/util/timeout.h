#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <os/event.h>

typedef util_error ( * util_timeout_action )( void * user_context );

typedef struct util_timeout_s {
   os_event            event;
   unsigned            delay_sec;
   unsigned            delay_ms;
   struct timespec     deadline;
   util_timeout_action action;
   void *              user_context;
} util_timeout;

util_error util_timeout_init   ( util_timeout * This, unsigned milliseconds, util_timeout_action action, void * user_context );
util_error util_timeout_destroy( util_timeout * This );
util_error util_timeout_start  ( util_timeout * This );
util_error util_timeout_cancel ( util_timeout * This );

#ifdef __cplusplus
}
#endif
