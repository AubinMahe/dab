#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <errno.h>

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

#define UTIL_CTXT __FILE__, __LINE__

#define UTIL_ERROR_CHECK(O) {\
   util_error err = O;\
   if( UTIL_NO_ERROR != err ) {\
      fprintf( stderr, "%s:%d:%s:%s:%s\n", __FILE__,__LINE__, __func__, #O, util_error_messages[err] );\
      return err;\
   }\
}

#define UTIL_RETURN_ERROR(err) {\
   if( UTIL_NO_ERROR != err ) {\
      fprintf( stderr, "%s:%d:%s:%s\n", __FILE__, __LINE__, __func__, util_error_messages[err] );\
      return err;\
   }\
}

#define UTIL_CHECK_NON_NULL(A) {\
   if( NULL == (A)) {\
      fprintf( stderr, "%s:%d:%s:%s\n", __FILE__, __LINE__, __func__, util_error_messages[UTIL_NULL_ARG] );\
      return UTIL_NULL_ARG;\
   }\
}

#define UTIL_ASSERT(T) {\
   if( ! (T)) {\
      fprintf( stderr, "%s:%d:%s:%s\n", __FILE__, __LINE__, __func__, #T );\
      return UTIL_NOT_APPLICABLE;\
   }\
}

#ifdef __cplusplus
}
#endif
