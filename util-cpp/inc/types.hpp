#pragma once

#include <stddef.h>
#include <inttypes.h>

typedef unsigned char byte;

#ifdef _WIN32
   typedef unsigned short ushort;
#endif

#ifdef _MSC_VER
   typedef          __int64   int64_t;
   typedef unsigned __int64   uint64_t;
#  define HPMS_FUNCNAME __FUNCSIG__
#else
#  define HPMS_FUNCNAME __PRETTY_FUNCTION__
#endif
