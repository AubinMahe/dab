#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>

typedef enum util_error_e {
   UTIL_ERROR_FIRST,

   UTIL_NO_ERROR = UTIL_ERROR_FIRST,
   UTIL_NULL_ARG,
   UTIL_MEMORY_FULL,
   UTIL_NOT_FOUND,
   UTIL_PARSE_ERROR,
   UTIL_OS_ERROR,
   UTIL_OVERFLOW,
   UTIL_UNDERFLOW,
   UTIL_NOT_APPLICABLE,

   UTIL_ERROR_LAST
} util_error;

extern const char * util_error_messages[UTIL_ERROR_LAST];

#define UTIL_ERROR_CHECK(O,F,L) {\
   util_error err = O;\
   if( UTIL_NO_ERROR != err ) {\
      fprintf( stderr, "%s:%d:%s:%s\n", F, L, #O, util_error_messages[err] );\
      return err;\
   }\
}

#define UTIL_RETURN_ERROR(err,F,L) {\
   if( UTIL_NO_ERROR != err ) {\
      fprintf( stderr, "%s:%d:%s\n", F, L, util_error_messages[err] );\
      return err;\
   }\
}

#define UTIL_CHECK_NON_NULL(A,F,L) {\
   if( NULL == (A)) {\
      fprintf( stderr, "%s:%d:%s\n", F, L, util_error_messages[UTIL_NULL_ARG] );\
      return UTIL_NULL_ARG;\
   }\
}

#ifdef __cplusplus
}
#endif
