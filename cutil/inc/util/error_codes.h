#pragma once

#ifdef __cplusplus
extern "C" {
#endif

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

#ifdef __cplusplus
}
#endif
