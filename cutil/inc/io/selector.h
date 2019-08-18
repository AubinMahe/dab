#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <io/sockets.h>
#include <util/error_codes.h>

util_error io_selector_init  ( fd_set * set,... );
util_error io_selector_select( fd_set * set, unsigned timeout );
util_error io_selector_is_set( fd_set * set, SOCKET sckt );

#ifdef __cplusplus
}
#endif
