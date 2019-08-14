#pragma once

#include <inttypes.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

typedef unsigned char  byte;
typedef unsigned short ushort;
typedef unsigned int   uint;
typedef unsigned long  ulong;

#ifdef _MSC_VER
   typedef          __int64   int64_t;
   typedef unsigned __int64   uint64_t;
#endif
