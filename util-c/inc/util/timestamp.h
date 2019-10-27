#pragma once

#include <inttypes.h>
#include <time.h>

const char * util_timestamp_now_tz( void );

const char * util_timestamp_now( void );

/**
 * Return a string conformant to ISO8601 : 2019-10-27T15:04:59+0100.
 */
const char * util_timestamp_to_string_tz( const struct tm * time );

/**
 * Return a string conformant to ISO8601 : 2019-10-27T15:04:59[+0100].
 * The last part is optional, see boolean withTimeZone argument.
 */
const char * util_timestamp_to_string( const struct tm * time );
