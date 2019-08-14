#pragma once

#include <util/error_codes.h>

util_error os_get_error_message( const char * func, const char * file, unsigned line, char *target, unsigned sizeof_target );
