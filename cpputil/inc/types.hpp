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
#endif
