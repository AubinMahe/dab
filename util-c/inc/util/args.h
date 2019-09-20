#pragma once

#ifdef __cplusplus
extern "C" {
#endif

#include "map.h"

util_error util_args_parse     (       util_map * This, size_t capacity, util_pair * pairs, int argc, char * argv[] );
util_error util_args_get_bool  ( const util_map * This, const char * key, bool *        target );
util_error util_args_get_char  ( const util_map * This, const char * key, char *        target );
util_error util_args_get_byte  ( const util_map * This, const char * key, byte *        target );
util_error util_args_get_short ( const util_map * This, const char * key, short *       target );
util_error util_args_get_ushort( const util_map * This, const char * key, ushort *      target );
util_error util_args_get_int   ( const util_map * This, const char * key, int *         target );
util_error util_args_get_uint  ( const util_map * This, const char * key, uint *        target );
util_error util_args_get_long  ( const util_map * This, const char * key, long *        target );
util_error util_args_get_ulong ( const util_map * This, const char * key, ulong *       target );
util_error util_args_get_int64 ( const util_map * This, const char * key, int64_t *     target );
util_error util_args_get_uint64( const util_map * This, const char * key, uint64_t *    target );
util_error util_args_get_float ( const util_map * This, const char * key, float *       target );
util_error util_args_get_double( const util_map * This, const char * key, double *      target );
util_error util_args_get_string( const util_map * This, const char * key, const char ** target );

#ifdef __cplusplus
}
#endif
