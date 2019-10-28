#pragma once

#include <stdio.h>
#include <util/timestamp.h>

#define UTIL_LOG_HERE()              fprintf( stderr, "%s:%s|entry\n", util_timestamp_now(), __func__ );
#define UTIL_LOG_MSG( msg )          fprintf( stderr, "%s:%s|%s\n", util_timestamp_now(), __func__, msg );
#define UTIL_LOG_ARGS( format, ... ) fprintf( stderr, "%s:%s|" format "\n", util_timestamp_now(), __func__, __VA_ARGS__ );
#define UTIL_LOG_DONE()              fprintf( stderr, "%s:%s|done\n", util_timestamp_now(), __func__ );
