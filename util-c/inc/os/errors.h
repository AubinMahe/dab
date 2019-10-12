#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <util/error_codes.h>

void os_error_print( const char * call, const char * file, unsigned line, const char * func );

#define UTIL_PRINT_OS_ERROR( C, E, O )\
   os_error_print( C, __FILE__, __LINE__-O, __func__ )

#define OS_CHECK(O) {\
   int ret = O;\
   if( ret ) {\
      os_error_print( #O, __FILE__, __LINE__, __func__ );\
      return UTIL_OS_ERROR;\
   }\
}

#define OS_ERROR_IF(O,V) {\
   if( V == O ) {\
      os_error_print( #O, __FILE__, __LINE__, __func__ );\
      return UTIL_OS_ERROR;\
   }\
}

#define OS_ASSERT(C,A,O) {\
   if( ! (A)) {\
      os_error_print( C, __FILE__, __LINE__-O, __func__ );\
      return UTIL_OS_ERROR;\
   }\
}

#ifdef __cplusplus
}
#endif
