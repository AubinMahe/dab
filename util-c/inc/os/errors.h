#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <util/error_codes.h>

util_error os_error_print( const char * call, const char * file, unsigned line );

#define OS_CHECK(O,F,L) {\
   int ret = O;\
   if( ret ) {\
      return os_error_print( #O, F, L );\
   }\
}

#define OS_ERROR(O,V,F,L) {\
   if( V == O ) {\
      return os_error_print( #O, F, L );\
   }\
}

#define OS_ASSERT(C,A,F,L) {\
   if( ! (A)) {\
      return os_error_print( C, F, L );\
   }\
}

#ifdef __cplusplus
}
#endif
