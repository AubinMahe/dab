#pragma once

#include <stdio.h>
#include <types.hpp>
#include <util/Time.hpp>

#define UTIL_LOG_HERE()              fprintf( stderr, "%s:%s|entry\n", util::Time::now(), HPMS_FUNCNAME );
#define UTIL_LOG_MSG( msg )          fprintf( stderr, "%s:%s|%s\n", util::Time::now(), HPMS_FUNCNAME, msg );
#define UTIL_LOG_ARGS( format, ... ) fprintf( stderr, "%s:%s|" format "\n", util::Time::now(), HPMS_FUNCNAME, __VA_ARGS__ );
#define UTIL_LOG_DONE()              fprintf( stderr, "%s:%s|done\n", util::Time::now(), HPMS_FUNCNAME );
