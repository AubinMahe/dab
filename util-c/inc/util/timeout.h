#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include "../types.h"
#include "error_codes.h"

typedef util_error ( * util_timeout_action )( void * user_context );

typedef void * util_timeout;

util_error util_timeout_start ( unsigned milliseconds, util_timeout_action action, void * user_context, util_timeout * id );
util_error util_timeout_cancel( util_timeout id );

#ifdef __cplusplus
}
#endif
